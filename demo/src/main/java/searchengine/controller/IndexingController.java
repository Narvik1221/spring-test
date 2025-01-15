package searchengine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.service.IndexingService;

@RestController
@RequestMapping("/api")
public class IndexingController {
    private final IndexingService indexingService;

    public IndexingController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @PostMapping("/index")
    public ResponseEntity<String> indexSite(@RequestParam String url, @RequestParam String name) {
        indexingService.indexSite(url, name);
        return ResponseEntity.ok("Индексация сайта завершена: " + url);
    }
}
