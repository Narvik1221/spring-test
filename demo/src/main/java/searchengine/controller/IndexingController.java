package searchengine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.service.IndexingService;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IndexingController {
    private final IndexingService indexingService;

    public IndexingController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @PostMapping("/index")
    public ResponseEntity<String> indexSite(@RequestBody Map<String, String> requestBody) {
        String url = requestBody.get("url");
        String name = requestBody.get("name");
    
        if (url == null || url.isEmpty() || name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body("URL или имя сайта не указаны.");
        }
    
        try {
            indexingService.indexSite(url, name);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Не удалось проиндексировать сайт: " + e.getMessage());
        }
    
        return ResponseEntity.ok("Индексация сайта завершена: " + url);
    }
}
