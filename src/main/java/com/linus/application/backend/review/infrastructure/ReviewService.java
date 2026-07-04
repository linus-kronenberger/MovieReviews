package com.linus.application.backend.review.infrastructure;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.essay.infrastructure.EssayRepository;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.rating.Rating;
import com.linus.application.backend.rating.RatingCategory;
import com.linus.application.backend.rating.RatingCategoryRepository;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.backend.user.infrastructure.UserRetrievalServiceImpl;
import com.linus.application.security.AuthenticatedUser;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final EssayRepository essayRepository;
    private final RatingCategoryRepository ratingCategoryRepository;
    private final AuthenticatedUser authenticatedUser;
    private final UserRetrievalServiceImpl userRetrievalServiceImpl;

    public ReviewService(ReviewRepository reviewRepository, EssayRepository essayRepository,
                         RatingCategoryRepository ratingCategoryRepository,
                         AuthenticatedUser authenticatedUser, UserRetrievalServiceImpl userRetrievalServiceImpl) {
        this.reviewRepository = reviewRepository;
        this.essayRepository = essayRepository;
        this.ratingCategoryRepository = ratingCategoryRepository;
        this.authenticatedUser = authenticatedUser;
        this.userRetrievalServiceImpl = userRetrievalServiceImpl;
    }

    @Transactional
    public Review saveReview(Movie movie, Map<RatingCategory, Double> ratings, String essayText) {
        User currentUser = authenticatedUser.get().orElseThrow();
        Review review = new Review();
        review.setMovie(movie);
        review.setUser(currentUser);

        AtomicReference<Double> starsSum = new AtomicReference<>((double) 0);
        AtomicReference<Integer> count = new AtomicReference<>((int) 0);


        ratings.forEach((category, rate) -> {
            Rating rating = new Rating();
            rating.setCategory(category);
            rating.setStars(rate);
            rating.setReview(review);
            starsSum.updateAndGet(v -> new Double(v + rate));
            count.updateAndGet(v -> v + 1);
        });

        Double avg = starsSum.get() / count.get();

        review.setAverageRating(avg);


        reviewRepository.save(review);

        if (essayText != null && !essayText.isBlank()) {
            Essay essay = new Essay();
            essay.setText(essayText);
            essay.setRating(review);
            essayRepository.save(essay);
        }

        return review;
    }

    @Transactional(readOnly = true)
    public List<Review> getRatingsByMovie(Movie movie) {
        return reviewRepository.findByMovie(movie);
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
    public RatingCategory createCategory(String name, String description) {
        User currentUser = authenticatedUser.get().orElseThrow();
        RatingCategory category = new RatingCategory();
        category.setCategory(name);
        category.setDescription(description);
        category.setUser(currentUser);
        return ratingCategoryRepository.save(category);
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
        List<User> adminUsers = userRetrievalServiceImpl.findAdminUsers().orElseThrow(RuntimeException::new);
        adminUsers.forEach(u -> {
            List<RatingCategory> userRatingCategories = ratingCategoryRepository.findByUser(u);
            adminCategories.addAll(userRatingCategories);
        });
        return adminCategories;
    }
}
