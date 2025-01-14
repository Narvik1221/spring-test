package searchengine.dto;

public class SearchResult {
    private String url;
    private String title;
    private String snippet;

    public SearchResult(String baseUrl, String path, String title, String snippet) {
        this.url = baseUrl + path; // Формирование полного URL
        this.title = title;
        this.snippet = snippet;
    }

    public SearchResult(String url, String title, String snippet) {
        this.url = url;
        this.title = title;
        this.snippet = snippet;
    }

    // Getters и Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
