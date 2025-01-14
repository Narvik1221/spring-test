package searchengine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.SiteRequest;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
@RestController
@RequestMapping("/api")
public class SiteController {

    private static final Logger logger = LoggerFactory.getLogger(SiteController.class);

    private final SiteRepository siteRepository;

    public SiteController(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @PostMapping("/addSite")
    public ResponseEntity<String> addSite(@RequestBody SiteRequest siteRequest) {
        logger.info("Получен запрос на добавление сайта: {}", siteRequest.getUrl());

        String url = siteRequest.getUrl();
        Optional<Site> existingSite = siteRepository.findByUrl(url);

        if (existingSite.isPresent()) {
            logger.warn("Сайт с URL {} уже существует", url);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Сайт с таким URL уже существует");
        }

        // Создание и сохранение нового сайта
        Site newSite = new Site();
        newSite.setUrl(siteRequest.getUrl());
        newSite.setName(siteRequest.getName());
        newSite.setStatus(StatusType.INDEXING); // Начальный статус
        newSite.setStatusTime(LocalDateTime.now());

        siteRepository.save(newSite);

        logger.info("Сайт {} успешно добавлен", siteRequest.getUrl());
        return ResponseEntity.ok("Сайт успешно добавлен");
    }

    @GetMapping("/sites")
    public ResponseEntity<List<Site>> getAllSites() {
        List<Site> sites = siteRepository.findAll();
        return ResponseEntity.ok(sites);
    }

}
