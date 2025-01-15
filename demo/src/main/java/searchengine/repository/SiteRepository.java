package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    // Метод для поиска сайта по URL
    Optional<Site> findByUrl(String url);

    // Метод для удаления сайта по URL
    @Transactional
    void deleteByUrl(String url);
}
