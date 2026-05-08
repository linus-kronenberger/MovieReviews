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

}
