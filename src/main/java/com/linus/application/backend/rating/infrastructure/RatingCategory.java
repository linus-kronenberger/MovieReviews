package com.linus.application.backend.rating.infrastructure;

import jakarta.persistence.*;

@Table(name = "rating_category")
@Entity
public class RatingCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column
    private String category;

    @Column
    private String description;
}
