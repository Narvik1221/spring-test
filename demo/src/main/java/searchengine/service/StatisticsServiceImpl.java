package searchengine.service;

import org.springframework.stereotype.Service;
import searchengine.dto.SiteStatistics;
import searchengine.dto.StatisticsResponse;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    public StatisticsServiceImpl(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
    }

    @Override
    public StatisticsResponse getStatistics() {
        // Формируем статистику по каждому сайту
        List<SiteStatistics> siteStats = siteRepository.findAll().stream()
                .map(site -> {
                    long pagesCount = pageRepository.countBySite(site);
                    long lemmasCount = lemmaRepository.countBySite(site);
                    return new SiteStatistics(site.getUrl(), site.getName(), pagesCount, lemmasCount);
                })
                .collect(Collectors.toList());

        // Общая статистика
        long totalPages = pageRepository.count();
        long totalLemmas = lemmaRepository.count();

        return new StatisticsResponse(totalPages, totalLemmas, siteStats);
    }

    @Override
    public List<Lemma> getLemmasBySite(String siteUrl) {
        Site site = siteRepository.findByUrl(siteUrl)
                .orElseThrow(() -> new RuntimeException("Сайт не найден: " + siteUrl));
        return lemmaRepository.findAllBySite(site);
    }

    @Override
    public List<Page> getPagesBySite(String siteUrl) {
        Site site = siteRepository.findByUrl(siteUrl)
                .orElseThrow(() -> new RuntimeException("Сайт не найден: " + siteUrl));
        return pageRepository.findAllBySite(site);
    }
}
