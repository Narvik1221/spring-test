package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    @Transactional
    void deleteAll();
    List<Page> findByContentContainingIgnoreCase(String query);
    List<Page> findByContentContainingIgnoreCaseAndSite_Url(String content, String siteUrl);
    long count(); // Метод для подсчёта общего количества страниц
   @Transactional
    void deleteBySite(Site site);
}
