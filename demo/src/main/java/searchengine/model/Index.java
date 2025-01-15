package searchengine.model;

import jakarta.persistence.*;

@Entity
@Table(name = "search_index")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemma;

    @Column(name = "rank_value", nullable = false)
    private float rankValue;

    public Index() {}

    public Index(Page page, Lemma lemma, float rankValue) {
        this.page = page;
        this.lemma = lemma;
        this.rankValue = rankValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Lemma getLemma() {
        return lemma;
    }

    public void setLemma(Lemma lemma) {
        this.lemma = lemma;
    }

    public float getRankValue() {
        return rankValue;
    }

    public void setRankValue(float rankValue) {
        this.rankValue = rankValue;
    }
}
