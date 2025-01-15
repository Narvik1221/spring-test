package searchengine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.SearchRequest;
import searchengine.dto.SearchResult;
import searchengine.dto.StatisticsResponse;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.service.IndexingService;
import searchengine.service.SearchService;
import searchengine.service.StatisticsService;

import java.util.List;

@Controller
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final IndexingService indexingService;
    private final SearchService searchService;
    private final StatisticsService statisticsService; // Добавлено поле для StatisticsService

    public ApiController(IndexingService indexingService, SearchService searchService, StatisticsService statisticsService) {
        this.indexingService = indexingService;
        this.searchService = searchService;
        this.statisticsService = statisticsService; // Инициализация StatisticsService
    }

    @PostMapping("/startIndexing")
    public ResponseEntity<String> startIndexing() {
        logger.info("Запрос на старт индексации получен");
        if (indexingService.startIndexing()) {
            logger.info("Индексация успешно запущена");
            return ResponseEntity.ok("Индексация запущена");
        } else {
            logger.warn("Индексация уже была запущена");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Индексация уже запущена");
        }
    }

    @PostMapping("/stopIndexing")
    public ResponseEntity<String> stopIndexing() {
        logger.info("Запрос на остановку индексации получен");
        if (indexingService.stopIndexing()) {
            logger.info("Индексация успешно остановлена");
            return ResponseEntity.ok("Индексация остановлена");
        } else {
            logger.warn("Индексация не была запущена");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Индексация не была запущена");
        }
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        logger.info("Запрос на поиск: {}, сайт: {}", request.getQuery(), request.getSite());
    
        if (request.getQuery() == null || request.getQuery().isBlank()) {
            logger.warn("Пустой поисковый запрос");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Поисковый запрос не может быть пустым");
        }
    
        try {
            List<SearchResult> results = searchService.search(request.getQuery(), request.getSite());
            logger.info("Поиск завершён. Найдено результатов: {}", results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Ошибка при выполнении поиска", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при выполнении поиска");
        }
    }

    @GetMapping("/")
    public String index() {
        logger.info("Запрос на отображение index.html");
        return "index";
    }

    // Возвращает общую статистику
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        logger.info("Запрос на получение общей статистики");
        StatisticsResponse response = statisticsService.getStatistics();
        return ResponseEntity.ok(response);
    }

    // Возвращает список лемм для указанного сайта
    @GetMapping("/statistics/lemmas")
    public ResponseEntity<List<Lemma>> getLemmasBySite(@RequestParam String siteUrl) {
        logger.info("Запрос на получение лемм для сайта: {}", siteUrl);
        try {
            List<Lemma> lemmas = statisticsService.getLemmasBySite(siteUrl);
            return ResponseEntity.ok(lemmas);
        } catch (RuntimeException e) {
            logger.error("Ошибка при получении лемм для сайта: {}", siteUrl, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Возвращает список страниц для указанного сайта
    @GetMapping("/statistics/pages")
    public ResponseEntity<List<Page>> getPagesBySite(@RequestParam String siteUrl) {
        logger.info("Запрос на получение страниц для сайта: {}", siteUrl);
        try {
            List<Page> pages = statisticsService.getPagesBySite(siteUrl);
            return ResponseEntity.ok(pages);
        } catch (RuntimeException e) {
            logger.error("Ошибка при получении страниц для сайта: {}", siteUrl, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
