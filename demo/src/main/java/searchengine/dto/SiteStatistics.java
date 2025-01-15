package searchengine.dto;

public class SiteStatistics {
    private final String url;
    private final String name;
    private final long pagesCount;
    private final long lemmasCount;

    public SiteStatistics(String url, String name, long pagesCount, long lemmasCount) {
        this.url = url;
        this.name = name;
        this.pagesCount = pagesCount;
        this.lemmasCount = lemmasCount;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public long getPagesCount() {
        return pagesCount;
    }

    public long getLemmasCount() {
        return lemmasCount;
    }
}
