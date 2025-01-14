package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    // Метод для поиска по содержимому страницы
    List<Page> findByContentContainingIgnoreCase(String query);
    List<Page> findByContentContainingIgnoreCaseAndSite_Url(String content, String siteUrl);

}
