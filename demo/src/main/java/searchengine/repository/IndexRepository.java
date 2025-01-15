package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

public interface IndexRepository extends JpaRepository<Index, Integer> {
    Optional<Index> findByPageAndLemma(Page page, Lemma lemma);

    @Transactional
    void deleteAll();

    // Метод для поиска индексов по лемме
    List<Index> findByLemma(Lemma lemma);

    @Transactional
    void deleteByPage_Site(Site site); // Используем связь через Page
}
