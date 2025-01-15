package searchengine.service;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;
import searchengine.model.Site;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LemmaServiceImpl implements LemmaService {

    private final LuceneMorphology luceneMorphology;

    public LemmaServiceImpl() {
        try {
            this.luceneMorphology = new RussianLuceneMorphology();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize LuceneMorphology", e);
        }
    }

    @Override
    public Map<String, Integer> lemmatize(String text) {
        Map<String, Integer> lemmas = new HashMap<>();
        String[] words = text.split("\\s+");
        for (String word : words) {
            word = word.toLowerCase().replaceAll("[^а-яё]", ""); // Убираем лишние символы
            if (word.isEmpty() || !luceneMorphology.checkString(word)) {
                continue;
            }
            List<String> normalForms = luceneMorphology.getNormalForms(word);
            for (String normalForm : normalForms) {
                lemmas.put(normalForm, lemmas.getOrDefault(normalForm, 0) + 1);
            }
        }
        return lemmas;
    }

    @Override
    public void saveLemmas(Site site, Map<String, Integer> lemmas) {
        // Временная реализация: вывод лемм в консоль
        System.out.println("Saving lemmas for site: " + site.getName());
        lemmas.forEach((lemma, count) -> 
            System.out.println("Lemma: " + lemma + ", Count: " + count)
        );
    }
}
