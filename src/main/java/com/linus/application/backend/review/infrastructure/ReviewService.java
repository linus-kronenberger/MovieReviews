package com.linus.application.backend.review.infrastructure;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.essay.infrastructure.EssayRepository;
import com.linus.application.backend.friend.FriendService;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.rating.Rating;
import com.linus.application.backend.rating.RatingCategory;
import com.linus.application.backend.rating.RatingCategoryRepository;
import com.linus.application.backend.rating.RatingRepository;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.backend.user.domain.UserRetrievalService;
import com.linus.application.security.AuthenticatedUser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final EssayRepository essayRepository;
    private final RatingCategoryRepository ratingCategoryRepository;
    private final RatingRepository ratingRepository;
    private final AuthenticatedUser authenticatedUser;
    private final UserRetrievalService userRetrievalService;
    private final FriendService friendService;

    public ReviewService(ReviewRepository reviewRepository, EssayRepository essayRepository,
                         RatingCategoryRepository ratingCategoryRepository,
                        RatingRepository ratingRepository,
                         AuthenticatedUser authenticatedUser, UserRetrievalService userRetrievalService,
                         FriendService friendService) {
        this.reviewRepository = reviewRepository;
        this.essayRepository = essayRepository;
        this.ratingCategoryRepository = ratingCategoryRepository;
        this.ratingRepository = ratingRepository;
        this.authenticatedUser = authenticatedUser;
        this.userRetrievalService = userRetrievalService;
        this.friendService = friendService;
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

        reviewRepository.save(review);

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
    public List<Review> getRatingsByMovieForFriends(Movie movie) {
        User currentUser = authenticatedUser.get().orElseThrow();
        List<String> userIds = friendService.getFriendIds(currentUser);
        userIds.add(currentUser.getId());
        return reviewRepository.findByMovieAndUserIdIn(movie, userIds);
    }

    @Transactional(readOnly = true)
    public List<Review> getAllReviewsForUserAndFriends() {
        User currentUser = authenticatedUser.get().orElseThrow();
        List<User> users = new ArrayList<>(friendService.getFriends(currentUser));
        users.add(currentUser);
        return reviewRepository.findByUserIn(users);
    }

    @Transactional(readOnly = true)
    public List<Review> getTopReviews(User user, int limit) {
        return reviewRepository.findByUserIn(List.of(user)).stream()
                .sorted(Comparator.comparingDouble(Review::getAverageRating).reversed())
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getRatingsBreakdown(Review review) {
        List<Rating> ratings = ratingRepository.findByReviewIn(List.of(review));
        Map<String, Double> breakdown = new HashMap<>();
        for (Rating r : ratings) {
            breakdown.put(r.getCategory().getCategory(), r.getStars());
        }
        return breakdown;
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
            ratingRepository.deleteByReviewIn(List.of(review));
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

    @Transactional(readOnly = true)
    public List<RatingCategory> getCustomCategoriesForFriendsAndSelf() {
        User currentUser = authenticatedUser.get().orElseThrow();
        List<User> friends = friendService.getFriends(currentUser);

        List<User> users = new ArrayList<>(friends);
        users.add(currentUser);

        List<RatingCategory> allCategories = ratingCategoryRepository.findByUserIn(users);

        Set<String> adminUserIds = userRetrievalService.findAdminUsers()
                .orElse(List.of())
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return allCategories.stream()
                .filter(cat -> !adminUserIds.contains(cat.getUser().getId()))
                .collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public List<RatingCategory> getCustomCategoriesForFriendsAndSelf() {
        User currentUser = authenticatedUser.get().orElseThrow();
        List<User> friends = friendService.getFriends(currentUser);

        List<User> users = new ArrayList<>(friends);
        users.add(currentUser);

        List<RatingCategory> allCategories = ratingCategoryRepository.findByUserIn(users);

        Set<String> adminUserIds = userRetrievalService.findAdminUsers()
                .orElse(List.of())
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return allCategories.stream()
                .filter(cat -> !adminUserIds.contains(cat.getUser().getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, double[]> getThreeAveragesForReviews(List<Review> reviews) {
        List<Rating> allRatings = ratingRepository.findByReviewIn(reviews);
        Set<String> adminCategoryIds = getAdminCategories().stream()
                .map(RatingCategory::getId)
                .collect(Collectors.toSet());

        Map<String, List<Rating>> ratingsByReview = allRatings.stream()
                .collect(Collectors.groupingBy(r -> r.getReview().getId()));

        Map<String, double[]> result = new HashMap<>();
        for (Review review : reviews) {
            String reviewId = review.getId();
            List<Rating> reviewRatings = ratingsByReview.getOrDefault(reviewId, List.of());

            if (reviewRatings.isEmpty()) {
                result.put(reviewId, new double[]{Double.NaN, Double.NaN, review.getAverageRating()});
            } else {
                double stdSum = 0, custSum = 0;
                int stdCount = 0, custCount = 0;
                for (Rating r : reviewRatings) {
                    if (adminCategoryIds.contains(r.getCategory().getId())) {
                        stdSum += r.getStars();
                        stdCount++;
                    } else {
                        custSum += r.getStars();
                        custCount++;
                    }
                }
                double stdAvg = stdCount > 0 ? stdSum / stdCount : 0;
                double custAvg = custCount > 0 ? custSum / custCount : 0;
                double overallAvg = (stdCount + custCount) > 0 ? (stdSum + custSum) / (stdCount + custCount) : 0;
                result.put(reviewId, new double[]{stdAvg, custAvg, overallAvg});
            }
        }
        return result;
    }

    public boolean reviewForUserExists() {
        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) return false;
        return reviewRepository.findByUser(currentUser) != null;
    }

    @Transactional
    public void deleteReviewByUser() {
        User  currentUser = authenticatedUser.get().orElseThrow();
        reviewRepository.deleteByUser(currentUser);
    }
}
