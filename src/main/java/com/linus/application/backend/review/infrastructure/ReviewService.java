package com.linus.application.backend.review.infrastructure;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.essay.infrastructure.EssayRepository;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.rating.Rating;
import com.linus.application.backend.rating.RatingCategory;
import com.linus.application.backend.rating.RatingCategoryRepository;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.backend.user.domain.UserRetrievalService;
import com.linus.application.security.AuthenticatedUser;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final EssayRepository essayRepository;
    private final RatingCategoryRepository ratingCategoryRepository;
    private final AuthenticatedUser authenticatedUser;
    private final UserRetrievalService userRetrievalService;

    public ReviewService(ReviewRepository reviewRepository, EssayRepository essayRepository,
                         RatingCategoryRepository ratingCategoryRepository,
                         AuthenticatedUser authenticatedUser, UserRetrievalService userRetrievalService) {
        this.reviewRepository = reviewRepository;
        this.essayRepository = essayRepository;
        this.ratingCategoryRepository = ratingCategoryRepository;
        this.authenticatedUser = authenticatedUser;
        this.userRetrievalService = userRetrievalService;
    }

    @Transactional
    public Review saveDraft(Movie movie, Map<RatingCategory, Double> ratingValues, String essayText) {
        User currentUser = authenticatedUser.get().orElseThrow();

        Review review = reviewRepository.findByUserAndMovie(currentUser, movie)
                .orElseGet(() -> {
                    Review r = new Review();
                    r.setMovie(movie);
                    r.setUser(currentUser);
                    r.setDraft(true);
                    return r;
                });

        review.getRatings().clear();

        double starsSum = 0.0;
        int count = 0;

        for (Map.Entry<RatingCategory, Double> entry : ratingValues.entrySet()) {
            Rating rating = new Rating();
            rating.setCategory(entry.getKey());
            rating.setStars(entry.getValue());
            rating.setReview(review);
            review.getRatings().add(rating);
            starsSum += entry.getValue();
            count++;
        }

        review.setAverageRating(count > 0 ? starsSum / count : 0.0);

        List<Essay> existingEssays = essayRepository.findByReviewIn(List.of(review));
        if (!existingEssays.isEmpty()) {
            Essay essay = existingEssays.get(0);
            if (essayText != null && !essayText.isBlank()) {
                essay.setText(essayText);
                essayRepository.save(essay);
            } else {
                essayRepository.delete(essay);
            }
        } else if (essayText != null && !essayText.isBlank()) {
            Essay essay = new Essay();
            essay.setText(essayText);
            essay.setRating(review);
            essayRepository.save(essay);
        }

        return reviewRepository.save(review);
    }

    @Transactional
    public void publishReview(Movie movie) {
        User currentUser = authenticatedUser.get().orElseThrow();
        reviewRepository.findByUserAndMovie(currentUser, movie)
                .ifPresent(review -> {
                    review.setDraft(false);
                    reviewRepository.save(review);
                });
    }

    @Transactional(readOnly = true)
    public Optional<Review> findDraftByMovie(Movie movie) {
        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) return Optional.empty();
        return reviewRepository.findByUserAndMovie(currentUser, movie);
    }

    @Transactional(readOnly = true)
    public Map<RatingCategory, Double> getDraftRatings(Review review) {
        Map<RatingCategory, Double> result = new java.util.HashMap<>();
        if (review.getRatings() != null) {
            for (Rating rating : review.getRatings()) {
                result.put(rating.getCategory(), rating.getStars());
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public String getDraftEssay(Review review) {
        List<Essay> essays = essayRepository.findByReviewIn(List.of(review));
        return essays.isEmpty() ? "" : essays.get(0).getText();
    }

    @Transactional(readOnly = true)
    public List<Review> getPublishedRatingsByMovie(Movie movie) {
        return reviewRepository.findPublishedByMovie(movie);
    }

    @Transactional(readOnly = true)
    public List<Essay> getEssaysByRatings(List<Review> reviews) {
        return essayRepository.findByReviewIn(reviews);
    }

    @Transactional
    public void deleteRatingIfRoleAllowed(Set<Review> reviews) {
        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) return;

        for (Review review : reviews) {
            if (!review.getUser().getId().equals(currentUser.getId())) continue;

            List<Essay> essays = essayRepository.findByReviewIn(List.of(review));
            essayRepository.deleteAll(essays);
            reviewRepository.delete(review);
        }
    }

    @Transactional(readOnly = true)
    public List<RatingCategory> getCategoriesByUser(User user) {
        return ratingCategoryRepository.findByUser(user);
    }

    @Transactional
    public void createCategory(String name, String description) {
        User currentUser = authenticatedUser.get().orElseThrow();
        RatingCategory category = new RatingCategory();
        category.setCategory(name);
        category.setDescription(description);
        category.setUser(currentUser);
      ratingCategoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(String categoryId) {
        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) return;

        RatingCategory category = ratingCategoryRepository.findById(categoryId).orElse(null);
        if (category == null) return;
        if (!category.getUser().getId().equals(currentUser.getId())) return;

        ratingCategoryRepository.delete(category);
    }

    public List<RatingCategory> getAdminCategories() {
        List<RatingCategory> adminCategories = new ArrayList<>();
        List<User> adminUsers = userRetrievalService.findAdminUsers().orElseThrow(RuntimeException::new);
        adminUsers.forEach(u -> {
            List<RatingCategory> userRatingCategories = ratingCategoryRepository.findByUser(u);
            adminCategories.addAll(userRatingCategories);
        });
        return adminCategories;
    }
}
