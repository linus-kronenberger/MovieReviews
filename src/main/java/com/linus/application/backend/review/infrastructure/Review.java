package com.linus.application.backend.review.infrastructure;

import com.linus.application.backend.movie.infrastructure.Movie;

import com.linus.application.backend.rating.Rating;
import com.linus.application.backend.rating.RatingCategory;
import com.linus.application.backend.user.infrastructure.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@Table(name = "review")
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id")
    private String id;

    @JoinColumn(name = "movie_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "avg_rating")
    private double averageRating = 2.5;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {this.averageRating = averageRating;}
}
