package searchengine.service;

import searchengine.dto.StatisticsResponse;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

public interface StatisticsService {
    StatisticsResponse getStatistics();
    List<Lemma> getLemmasBySite(String siteUrl);
    List<Page> getPagesBySite(String siteUrl);
}
