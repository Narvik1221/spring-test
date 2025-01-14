package searchengine.service;

import searchengine.dto.SearchResult;

import java.util.List;

public interface SearchService {
    List<SearchResult> search(String query, String site);
}
