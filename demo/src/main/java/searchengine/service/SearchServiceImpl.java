package searchengine.service;

import org.springframework.stereotype.Service;
import searchengine.dto.SearchResult;
import searchengine.model.Page;
import searchengine.repository.PageRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {
    private final PageRepository pageRepository;

    public SearchServiceImpl(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Override
    public List<SearchResult> search(String query, String site) {
        List<Page> pages;

        if (site != null && !site.isBlank()) {
            // Поиск с фильтрацией по URL сайта
            pages = pageRepository.findByContentContainingIgnoreCaseAndSite_Url(query, site);
        } else {
            // Поиск по всем сайтам
            pages = pageRepository.findByContentContainingIgnoreCase(query);
        }

        List<SearchResult> results = new ArrayList<>();
        for (Page page : pages) {
            String snippet = generateSnippet(page.getContent(), query);
            results.add(new SearchResult(page.getSite().getUrl(), page.getPath(), page.getSite().getName(), snippet));
        }

        return results;
    }

    private String generateSnippet(String content, String query) {
        // Генерация фрагмента текста, содержащего запрос
        int index = content.toLowerCase().indexOf(query.toLowerCase());
        if (index == -1) {
            return "";
        }
        int snippetStart = Math.max(0, index - 30);
        int snippetEnd = Math.min(content.length(), index + query.length() + 30);
        return content.substring(snippetStart, snippetEnd).replaceAll("\n", " ");
    }
}
