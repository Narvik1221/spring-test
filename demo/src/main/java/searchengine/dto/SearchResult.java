package searchengine.dto;

public class SearchResult {
    private String url;
    private String title;
    private String snippet;
    private double relevance; // Новое поле для относительной релевантности

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

    public SearchResult(String baseUrl, String path, String title, String snippet, Double relevance) {
        this.url = baseUrl + path; // Формирование полного URL
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance != null ? relevance : 0.0; // Обработка null для relevance
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

    public double getRelevance() {
        return relevance;
    }

    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }
}
