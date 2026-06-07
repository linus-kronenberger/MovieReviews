package com.linus.application.backend.rating.infrastructure;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.essay.infrastructure.EssayRepository;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.security.AuthenticatedUser;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;
    private final EssayRepository essayRepository;
    private final RatingCategoryRepository ratingCategoryRepository;
    private final AuthenticatedUser authenticatedUser;

    public RatingService(RatingRepository ratingRepository, EssayRepository essayRepository,
                         RatingCategoryRepository ratingCategoryRepository,
                         AuthenticatedUser authenticatedUser) {
        this.ratingRepository = ratingRepository;
        this.essayRepository = essayRepository;
        this.ratingCategoryRepository = ratingCategoryRepository;
        this.authenticatedUser = authenticatedUser;
    }

    @Transactional
    public Rating saveRating(Movie movie, RatingCategory category, Integer stars, String essayText) {
        User currentUser = authenticatedUser.get().orElseThrow();
        Rating rating = new Rating();
        rating.setMovie(movie);
        rating.setCategory(category);
        rating.setStars(stars);
        rating.setUser(currentUser);
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

    @Transactional
    public void deleteRatingIfRoleAllowed(Set<Rating> ratings) {
        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) return;

        for (Rating rating : ratings) {
            if (!rating.getUser().getId().equals(currentUser.getId())) continue;

            List<Essay> essays = essayRepository.findByRatingIn(List.of(rating));
            essayRepository.deleteAll(essays);
            ratingRepository.delete(rating);
        }
    }

    @Transactional(readOnly = true)
    public List<RatingCategory> getCategoriesByUser(User user) {
        return ratingCategoryRepository.findByUser(user);
    }

    @Transactional
    public RatingCategory createCategory(String name, String description) {
        User currentUser = authenticatedUser.get().orElseThrow();
        RatingCategory category = new RatingCategory();
        category.setCategory(name);
        category.setDescription(description);
        category.setUser(currentUser);
        return ratingCategoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) return;

        RatingCategory category = ratingCategoryRepository.findById(categoryId).orElse(null);
        if (category == null) return;
        if (!category.getUser().getId().equals(currentUser.getId())) return;

        ratingCategoryRepository.delete(category);
    }
}
