package searchengine.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.service.IndexingService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class IndexingServiceImpl implements IndexingService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final Set<String> visitedUrls = new HashSet<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> indexingTask;
    private volatile boolean stopFlag = false;

    public IndexingServiceImpl(SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    @Override
    public boolean startIndexing() {
        stopFlag = false; // Сбрасываем флаг перед началом новой индексации

        indexingTask = executorService.submit(() -> {
            try {
                Iterable<Site> sites = siteRepository.findAll();

                for (Site site : sites) {
                    if (stopFlag) {
                        break; // Останавливаем процесс, если флаг установлен
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
                // Логгирование ошибки
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
        if (stopFlag) return; // Останавливаем процесс, если установлен флаг

        if (visitedUrls.contains(url)) return;

        visitedUrls.add(url);

        Document document = Jsoup.connect(url).get();
        int statusCode = Jsoup.connect(url).execute().statusCode();
        String content = document.html();

        Page page = new Page();
        page.setSite(site);
        page.setPath(url.replace(site.getUrl(), ""));
        page.setCode(statusCode);
        page.setContent(content);
        pageRepository.save(page);

        Elements links = document.select("a[href]");
        for (var link : links) {
            String nextUrl = link.absUrl("href");
            if (nextUrl.startsWith(site.getUrl())) {
                crawlPage(nextUrl, site);
            }
        }
    }

    @Override
    public boolean stopIndexing() {
        if (indexingTask == null || indexingTask.isDone()) {
            return false; // Индексация уже завершена или не была запущена
        }

        stopFlag = true; // Устанавливаем флаг остановки

        try {
            indexingTask.get(); // Ожидаем завершения текущего потока индексации
        } catch (Exception e) {
            // Логгирование ошибки
            e.printStackTrace();
        }

        return true;
    }
}
