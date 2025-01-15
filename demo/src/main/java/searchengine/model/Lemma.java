package searchengine.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lemma")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER) // Загружаем связанные объекты немедленно
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "lemma", nullable = false, length = 255)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private int frequency;

    // Конструкторы
    public Lemma() {}

    public Lemma(Site site, String lemma, int frequency) {
        this.site = site;
        this.lemma = lemma;
        this.frequency = frequency;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    // Переопределение equals и hashCode для корректной работы retainAll
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lemma lemma1 = (Lemma) o;
        return id == lemma1.id &&
               frequency == lemma1.frequency &&
               Objects.equals(site, lemma1.site) &&
               Objects.equals(lemma, lemma1.lemma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, site, lemma, frequency);
    }

    // Удобный вывод объекта в строку для отладки
    @Override
    public String toString() {
        return "Lemma{" +
                "id=" + id +
                ", site=" + site +
                ", lemma='" + lemma + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}
