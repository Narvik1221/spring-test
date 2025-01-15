package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.Site;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    @Transactional
    void deleteAll();
    Optional<Lemma> findBySiteAndLemma(Site site, String lemma);
    List<Lemma> findByLemmaInAndSite_Url(Set<String> lemmaTexts, String siteUrl);

    // Метод для поиска лемм по текстам
    List<Lemma> findByLemmaIn(Set<String> lemmaTexts);
    @Transactional
    void deleteBySite(Site site);
}
