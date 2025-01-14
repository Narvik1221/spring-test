package searchengine.dto;

public class SearchRequest {
    private String query;
    private String site; // URL выбранного сайта или null для всех сайтов

    // Getters и Setters
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}
