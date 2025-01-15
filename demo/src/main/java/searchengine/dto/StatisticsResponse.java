package searchengine.dto;

import java.util.List;

public class StatisticsResponse {
    private final long totalPages;
    private final long totalLemmas;
    private final List<SiteStatistics> siteStats;

    public StatisticsResponse(long totalPages, long totalLemmas, List<SiteStatistics> siteStats) {
        this.totalPages = totalPages;
        this.totalLemmas = totalLemmas;
        this.siteStats = siteStats;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public long getTotalLemmas() {
        return totalLemmas;
    }

    public List<SiteStatistics> getSiteStats() {
        return siteStats;
    }
}
