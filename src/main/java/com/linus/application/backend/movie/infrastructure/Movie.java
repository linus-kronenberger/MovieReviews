package com.linus.application.backend.movie.infrastructure;

import jakarta.persistence.*;

@Entity
@Table(name = "movie")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "movie_id")
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "imdb_id")
    private String imdbId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MovieType type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public MovieType getType() {
        return type;
    }

    public void setType(MovieType type) {
        this.type = type;
    }
}
