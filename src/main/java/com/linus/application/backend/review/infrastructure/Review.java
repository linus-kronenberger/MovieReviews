package com.linus.application.backend.review.infrastructure;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.rating.Rating;
import com.linus.application.backend.user.infrastructure.User;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "review", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
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

    @OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Essay> essays;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    @Column(name = "avg_rating")
    private double averageRating = 2.5;

    @Column(name = "is_draft")
    private boolean isDraft = true;

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

    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean draft) {
        isDraft = draft;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }
}
