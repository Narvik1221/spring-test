package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Lemma;

import java.util.List;
import java.util.Optional;
import searchengine.model.Page;
import searchengine.model.Site;
public interface IndexRepository extends JpaRepository<Index, Integer> {
    Optional<Index> findByPageAndLemma(Page page, Lemma lemma);
    @Transactional
    void deleteAll();

    // Метод для поиска индексов по лемме
    List<Index> findByLemma(Lemma lemma);
    @Transactional
    void deleteBySite(Site site);
}
