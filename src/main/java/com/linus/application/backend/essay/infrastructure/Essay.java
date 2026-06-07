package com.linus.application.backend.essay.infrastructure;


import com.linus.application.backend.rating.infrastructure.Rating;
import jakarta.persistence.*;

@Table(name = "essay")
@Entity
public class Essay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "essay_id")
    private Long id;

    @Column(name = "text")
    private String text;

    @Column(name = "version")
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rating_id", referencedColumnName = "rating_id")
    private Rating rating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }
}
