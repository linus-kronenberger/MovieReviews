package com.linus.application.backend.essay.infrastructure;


import com.linus.application.backend.review.infrastructure.Review;
import jakarta.persistence.*;

@Table(name = "essay")
@Entity
public class Essay {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "essay_id")
    private String id;

    @Column(name = "text")
    private String text;

    @Column(name = "version")
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", referencedColumnName = "review_id")
    private Review review;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Review getRating() {
        return review;
    }

    public void setRating(Review review) {
        this.review = review;
    }
}
