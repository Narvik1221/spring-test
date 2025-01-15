package searchengine.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.*;
import searchengine.repository.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    public synchronized boolean startIndexing() {
        if (indexingTask != null && !indexingTask.isDone()) {
            System.out.println("Индексация уже выполняется.");
            return false;
        }
    
        clearTables();
        stopFlag = false;
    
        indexingTask = executorService.submit(() -> {
            try {
                Iterable<Site> sites = siteRepository.findAll();
    
                for (Site site : sites) {
                    if (stopFlag) {
                        System.out.println("Индексация остановлена.");
                        return;
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
        if (stopFlag) return; // Проверка перед сохранением страницы
    
        int statusCode = Jsoup.connect(url).execute().statusCode();
        String content = document.html();
        String plainText = Jsoup.parse(content).text();
    
        Page page = new Page();
        page.setSite(site);
        page.setPath(url.replace(site.getUrl(), ""));
        page.setCode(statusCode);
        page.setContent(content);
        pageRepository.save(page);
    
        if (stopFlag) return; // Проверка перед сохранением лемм и индексов
    
        Map<String, Integer> lemmas = lemmaService.lemmatize(plainText);
        saveLemmasAndIndexes(lemmas, site, page);
    
        Elements links = document.select("a[href]");
        for (var link : links) {
            if (stopFlag) return; // Проверка перед обработкой следующего URL
            String nextUrl = link.absUrl("href");
            if (nextUrl.startsWith(site.getUrl())) {
                crawlPage(nextUrl, site);
            }
        }
    }
    
    private void saveLemmasAndIndexes(Map<String, Integer> lemmas, Site site, Page page) {
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaText = entry.getKey();
            int frequency = entry.getValue();

            Lemma lemma = lemmaRepository.findBySiteAndLemma(site, lemmaText).orElse(null);

            if (lemma == null) {
                lemma = new Lemma();
                lemma.setSite(site);
                lemma.setLemma(lemmaText);
                lemma.setFrequency(frequency);
                lemmaRepository.save(lemma);
            } else {
                lemma.setFrequency(lemma.getFrequency() + frequency);
                lemmaRepository.save(lemma);
            }

            Index index = new Index();
            index.setPage(page);
            index.setLemma(lemma);
            index.setRankValue(frequency);
            indexRepository.save(index);
        }
    }

    @Override
    public synchronized boolean stopIndexing() {
        if (indexingTask == null || indexingTask.isDone()) {
            System.out.println("Индексация уже остановлена или не запускалась.");
            return false;
        }

        System.out.println("Попытка остановки индексации...");
        stopFlag = true;

        try {
            // Ожидание завершения текущей задачи индексации
            indexingTask.get(); // Блокирует поток до завершения задачи

            // Принудительное завершение ExecutorService
            executorService.shutdownNow();

            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Принудительное завершение индексации.");
                executorService.shutdownNow();
            }

            System.out.println("Индексация успешно остановлена.");
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при остановке индексации: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            stopFlag = false; // Сбрасываем флаг после завершения процесса
        }
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

    @Transactional
    @Override
    public void indexSite(String siteUrl, String siteName) {
        Site site = siteRepository.findByUrl(siteUrl).orElse(null);
    
        if (site == null) {
            // Создаём новый сайт и сохраняем его в базе
            site = new Site();
            site.setUrl(siteUrl);
            site.setName(siteName);
            site.setStatus(StatusType.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        } else {
            // Удаляем существующую индексацию для выбранного сайта
            indexRepository.deleteByPage_Site(site);
            lemmaRepository.deleteBySite(site);
            pageRepository.deleteBySite(site);
            site.setStatus(StatusType.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }
    
        try {
            // Запускаем индексацию только для выбранного сайта
            indexSite(siteUrl, site);
            site.setStatus(StatusType.INDEXED);
        } catch (Exception e) {
            // Обрабатываем ошибку индексации
            site.setStatus(StatusType.FAILED);
            site.setLastError(e.getMessage());
        }
    
        // Обновляем статус сайта в базе
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }
    private void clearTables() {
        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        System.out.println("Все таблицы очищены перед началом индексации.");
    }
}
