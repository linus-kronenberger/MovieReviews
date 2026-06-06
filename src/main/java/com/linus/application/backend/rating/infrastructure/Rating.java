package com.linus.application.backend.rating.infrastructure;

import com.linus.application.backend.movie.infrastructure.Movie;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Table(name = "rating")
@Entity
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long id;

    @JoinColumn(name = "movie_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;

    @JoinColumn(name = "category_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private RatingCategory category;

    @Column(name = "stars")
    @Max(10)
    @Min(1)
    private Integer stars;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public RatingCategory getCategory() {
        return category;
    }

    public void setCategory(RatingCategory category) {
        this.category = category;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }
}
