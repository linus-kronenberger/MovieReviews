package com.linus.application.backend.rating.infrastructure;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.essay.infrastructure.EssayRepository;
import com.linus.application.backend.movie.infrastructure.Movie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;
    private final EssayRepository essayRepository;
    private final RatingCategoryRepository ratingCategoryRepository;

    public RatingService(RatingRepository ratingRepository, EssayRepository essayRepository,
                         RatingCategoryRepository ratingCategoryRepository) {
        this.ratingRepository = ratingRepository;
        this.essayRepository = essayRepository;
        this.ratingCategoryRepository = ratingCategoryRepository;
    }

    @Transactional
    public Rating saveRating(Movie movie, RatingCategory category, Integer stars, String essayText) {
        Rating rating = new Rating();
        rating.setMovie(movie);
        rating.setCategory(category);
        rating.setStars(stars);
        rating = ratingRepository.save(rating);

        if (essayText != null && !essayText.isBlank()) {
            Essay essay = new Essay();
            essay.setText(essayText);
            essay.setRating(rating);
            essayRepository.save(essay);
        }

        return rating;
    }

    @Transactional(readOnly = true)
    public List<Rating> getRatingsByMovie(Movie movie) {
        return ratingRepository.findByMovie(movie);
    }

    @Transactional(readOnly = true)
    public List<Essay> getEssaysByRatings(List<Rating> ratings) {
        return essayRepository.findByRatingIn(ratings);
    }

    public List<RatingCategory> getAllCategories() {
        return (List<RatingCategory>) ratingCategoryRepository.findAll();
    }
}
