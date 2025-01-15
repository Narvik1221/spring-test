package searchengine.service;

import searchengine.model.Site;

import java.util.Map;

public interface LemmaService {
    Map<String, Integer> lemmatize(String text);
    void saveLemmas(Site site, Map<String, Integer> lemmas);
}
