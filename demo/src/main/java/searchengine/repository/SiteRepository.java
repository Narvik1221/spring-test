package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;

import java.util.Optional;

@Repository // Аннотация для обозначения класса как компонента репозитория
public interface SiteRepository extends JpaRepository<Site, Long> {
    
    // Метод для поиска сайта по URL
    Optional<Site> findByUrl(String url);

    // Метод для поиска сайта по имени (дополнительно, если потребуется)
    Optional<Site> findByName(String name);
}
