package searchengine.service;

public interface IndexingService {
    /**
     * Запуск индексации всех сайтов
     *
     * @return true, если индексация успешно запущена, иначе false
     */
    boolean startIndexing();

    /**
     * Остановка текущей индексации
     *
     * @return true, если индексация успешно остановлена, иначе false
     */
    boolean stopIndexing();

    /**
     * Индексация конкретной страницы
     *
     * @param url    URL страницы для индексации
     * @param siteId ID сайта, к которому принадлежит страница
     */
    void indexPage(String url, int siteId);

    /**
     * Индексация всего сайта. Если сайт уже существует, его данные удаляются и пересоздаются.
     *
     * @param siteUrl  URL сайта для индексации
     * @param siteName Имя сайта
     */
    void indexSite(String siteUrl, String siteName);
}
