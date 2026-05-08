package com.linus.application.backend.essay.infrastructure;


import com.linus.application.backend.rating.infrastructure.Rating;
import jakarta.persistence.*;

@Table(name = "essay")
@Entity
public class Essay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "essay_id")
    private int id;

    @Column(name = "text")
    private String text;

    @Column(name = "version")
    private int version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rating_id", referencedColumnName = "rating_id")
    private Rating rating;
}
