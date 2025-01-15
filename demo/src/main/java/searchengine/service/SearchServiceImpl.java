package searchengine.service;

import org.springframework.stereotype.Service;
import searchengine.dto.SearchResult;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    public SearchServiceImpl(PageRepository pageRepository, LemmaRepository lemmaRepository, 
                              IndexRepository indexRepository, LemmaService lemmaService) {
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.lemmaService = lemmaService;
    }

    @Override
    public List<SearchResult> search(String query, String site) {
        // 1. Лемматизация поискового запроса
        Map<String, Integer> lemmas = lemmaService.lemmatize(query);
        if (lemmas.isEmpty()) {
            return Collections.emptyList(); // Если не удалось получить леммы
        }

        // 2. Исключение слишком частых лемм
        List<Lemma> filteredLemmas = filterLemmas(lemmas.keySet(), site);
        if (filteredLemmas.isEmpty()) {
            return Collections.emptyList(); // Если нет подходящих лемм
        }

        // 3. Поиск страниц, содержащих леммы
        List<Page> pages = findRelevantPages(filteredLemmas);

        // 4. Расчёт релевантности страниц
        Map<Page, Double> relevanceMap = calculateRelevance(pages, filteredLemmas);

        // 5. Сортировка по убыванию релевантности
        List<Page> sortedPages = relevanceMap.entrySet().stream()
                .sorted(Map.Entry.<Page, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 6. Формирование результатов поиска
        return buildSearchResults(sortedPages, relevanceMap, query);
    }

    private List<Lemma> filterLemmas(Set<String> lemmaTexts, String site) {
        List<Lemma> lemmas;
    
        if (site != null && !site.isBlank()) {
            // Фильтрация лемм по указанному сайту
            lemmas = lemmaRepository.findByLemmaInAndSite_Url(lemmaTexts, site);
        } else {
            // Если сайт не указан, берем все леммы со всех сайтов
            lemmas = lemmaRepository.findByLemmaIn(lemmaTexts);
        }
    
        // Исключение слишком частых лемм
        int totalPages = (int) pageRepository.count();
        return lemmas.stream()
                .filter(lemma -> lemma.getFrequency() < totalPages * 0.7) // 70% - порог частотности
                .sorted(Comparator.comparingInt(Lemma::getFrequency)) // Сортировка по частотности
                .collect(Collectors.toList());
    }
    
    

    private List<Page> findRelevantPages(List<Lemma> lemmas) {
        // Группируем леммы по сайтам
            Map<Long, List<Lemma>> lemmasBySite = lemmas.stream()
        .filter(lemma -> lemma.getSite() != null && lemma.getSite().getId() != null)
        .collect(Collectors.groupingBy(lemma -> lemma.getSite().getId()));

    
    
        List<Page> relevantPages = new ArrayList<>();
    
        // Для каждого сайта находим пересечение страниц
        for (Map.Entry<Long, List<Lemma>> entry : lemmasBySite.entrySet()) {
            List<Page> pages = null;
    
            for (Lemma lemma : entry.getValue()) {
                List<Index> indexes = indexRepository.findByLemma(lemma);
                List<Page> currentPages = indexes.stream().map(Index::getPage).collect(Collectors.toList());
    
                if (pages == null) {
                    pages = currentPages; // Инициализация
                } else {
                    pages.retainAll(currentPages); // Пересечение страниц
                }
    
                if (pages.isEmpty()) break; // Если пересечение пустое, прекращаем обработку
            }
    
            if (pages != null) {
                relevantPages.addAll(pages);
            }
        }
    
        return relevantPages;
    }
    
    

    private Map<Page, Double> calculateRelevance(List<Page> pages, List<Lemma> lemmas) {
        Map<Page, Double> relevanceMap = new HashMap<>();
        double maxRelevance = 0;

        for (Page page : pages) {
            double relevance = 0.0; // Убедитесь, что начальное значение double указано правильно
            for (Lemma lemma : lemmas) {
                Optional<Index> indexOptional = indexRepository.findByPageAndLemma(page, lemma);
                if (indexOptional.isPresent()) {
                    relevance += indexOptional.get().getRankValue();
                }
            }
            relevanceMap.put(page, relevance);
            maxRelevance = Math.max(maxRelevance, relevance);
        }

        // Нормализация относительной релевантности
        for (Map.Entry<Page, Double> entry : relevanceMap.entrySet()) {
            entry.setValue(entry.getValue() / maxRelevance);
        }

        return relevanceMap;
    }

    private List<SearchResult> buildSearchResults(List<Page> pages, Map<Page, Double> relevanceMap, String query) {
        List<SearchResult> results = new ArrayList<>();
        for (Page page : pages) {
            String snippet = generateSnippet(page.getContent(), query);
            results.add(new SearchResult(page.getSite().getUrl(), page.getPath(), page.getSite().getName(),
                    snippet, relevanceMap.get(page)));
        }
        return results;
    }

    private String generateSnippet(String content, String query) {
        // Генерация фрагмента текста с выделением запроса
        int index = content.toLowerCase().indexOf(query.toLowerCase());
        if (index == -1) {
            return "";
        }
        int snippetStart = Math.max(0, index - 30);
        int snippetEnd = Math.min(content.length(), index + query.length() + 30);
        String snippet = content.substring(snippetStart, snippetEnd).replaceAll("\n", " ");
        return snippet.replaceAll("(?i)(" + query + ")", "<b>$1</b>");
    }
}
