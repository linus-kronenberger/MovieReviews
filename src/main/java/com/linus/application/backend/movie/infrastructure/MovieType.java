package com.linus.application.backend.movie.infrastructure;

public enum MovieType {
    FILM("Film"),
    SERIE("Serie");

    private final String displayName;

    MovieType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
