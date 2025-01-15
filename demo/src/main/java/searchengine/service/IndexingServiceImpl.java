package searchengine.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class IndexingServiceImpl implements IndexingService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;
    private final Set<String> visitedUrls = new HashSet<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> indexingTask;
    private volatile boolean stopFlag = false;

    public IndexingServiceImpl(SiteRepository siteRepository, PageRepository pageRepository, 
                               LemmaRepository lemmaRepository, IndexRepository indexRepository,
                               LemmaService lemmaService) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.lemmaService = lemmaService;
    }

    @Override
    public boolean startIndexing() {
        clearTables();
        stopFlag = false;

        indexingTask = executorService.submit(() -> {
            try {
                Iterable<Site> sites = siteRepository.findAll();

                for (Site site : sites) {
                    if (stopFlag) {
                        break;
                    }

                    site.setStatus(StatusType.INDEXING);
                    site.setStatusTime(LocalDateTime.now());
                    siteRepository.save(site);

                    try {
                        indexSite(site.getUrl(), site);
                        site.setStatus(StatusType.INDEXED);
                    } catch (Exception e) {
                        site.setStatus(StatusType.FAILED);
                        site.setLastError(e.getMessage());
                    }

                    site.setStatusTime(LocalDateTime.now());
                    siteRepository.save(site);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return true;
    }

    private void indexSite(String baseUrl, Site site) throws IOException {
        visitedUrls.clear();
        crawlPage(baseUrl, site);
    }

    private void crawlPage(String url, Site site) throws IOException {
        if (stopFlag) return;
        if (visitedUrls.contains(url)) return;

        visitedUrls.add(url);

        Document document = Jsoup.connect(url).get();
        int statusCode = Jsoup.connect(url).execute().statusCode();
        String content = document.html();
        String plainText = Jsoup.parse(content).text(); // Извлечение текста без HTML

        Page page = new Page();
        page.setSite(site);
        page.setPath(url.replace(site.getUrl(), ""));
        page.setCode(statusCode);
        page.setContent(content);
        pageRepository.save(page);

        Map<String, Integer> lemmas = lemmaService.lemmatize(plainText);
        saveLemmasAndIndexes(lemmas, site, page);

        Elements links = document.select("a[href]");
        for (var link : links) {
            String nextUrl = link.absUrl("href");
            if (nextUrl.startsWith(site.getUrl())) {
                crawlPage(nextUrl, site);
            }
        }
    }

    private void saveLemmasAndIndexes(Map<String, Integer> lemmas, Site site, Page page) {
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaText = entry.getKey();
            int frequency = entry.getValue(); // Частота на текущей странице
        
            // Ищем лемму в базе данных
            Lemma lemma = lemmaRepository.findBySiteAndLemma(site, lemmaText).orElse(null);
        
            if (lemma == null) {
                // Если лемма не найдена, создаем новую
                lemma = new Lemma();
                lemma.setSite(site);
                lemma.setLemma(lemmaText);
                lemma.setFrequency(frequency); // Устанавливаем частоту для новой леммы
                lemmaRepository.save(lemma);
            } else {
                // Если лемма найдена, увеличиваем её частотность
                lemma.setFrequency(lemma.getFrequency() + frequency);
                lemmaRepository.save(lemma);
            }
        
            // Сохраняем индекс
            Index index = new Index();
            index.setPage(page);
            index.setLemma(lemma);
            index.setRankValue(frequency);
            indexRepository.save(index);
        }
    }
    
    @Override
    public boolean stopIndexing() {
        if (indexingTask == null || indexingTask.isDone()) {
            return false;
        }

        stopFlag = true;

        try {
            indexingTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void indexPage(String url, int siteId) {
        try {
            Site site = siteRepository.findById((long) siteId).orElseThrow(() -> 
            new IllegalArgumentException("Site with ID " + siteId + " not found"));

            crawlPage(url, site);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearTables() {
        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        System.out.println("Все таблицы очищены перед началом индексации.");
    }
}
