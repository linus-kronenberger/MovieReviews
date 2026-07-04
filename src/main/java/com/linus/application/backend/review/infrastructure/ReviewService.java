package com.linus.application.backend.review.infrastructure;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.essay.infrastructure.EssayRepository;
import com.linus.application.backend.friend.FriendService;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.rating.Rating;
import com.linus.application.backend.rating.RatingCategory;
import com.linus.application.backend.rating.RatingCategoryRepository;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.backend.user.domain.UserRetrievalService;
import com.linus.application.security.AuthenticatedUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final EssayRepository essayRepository;
    private final RatingCategoryRepository ratingCategoryRepository;
    private final AuthenticatedUser authenticatedUser;
    private final UserRetrievalService userRetrievalService;
    private final FriendService friendService;

    public ReviewService(ReviewRepository reviewRepository, EssayRepository essayRepository,
                         RatingCategoryRepository ratingCategoryRepository,
                         AuthenticatedUser authenticatedUser, UserRetrievalService userRetrievalService,
                         FriendService friendService) {
        this.reviewRepository = reviewRepository;
        this.essayRepository = essayRepository;
        this.ratingCategoryRepository = ratingCategoryRepository;
        this.authenticatedUser = authenticatedUser;
        this.userRetrievalService = userRetrievalService;
        this.friendService = friendService;
    }

    @Transactional
    public void saveReview(Movie movie, Map<RatingCategory, Double> ratings, String essayText) {
        User currentUser = authenticatedUser.get().orElseThrow();
        Review review = new Review();
        review.setMovie(movie);
        review.setUser(currentUser);

        double starsSum = 0.0;
        int count = 0;

        for (Map.Entry<RatingCategory, Double> entry : ratings.entrySet()) {
            Rating rating = new Rating();
            rating.setCategory(entry.getKey());
            rating.setStars(entry.getValue());
            rating.setReview(review);
            starsSum += entry.getValue();
            count++;
        }

        double avg = count > 0 ? starsSum / count : 0.0;

        review.setAverageRating(avg);


        reviewRepository.save(review);

        if (essayText != null && !essayText.isBlank()) {
            Essay essay = new Essay();
            essay.setText(essayText);
            essay.setRating(review);
            essayRepository.save(essay);
        }
    }

    @Transactional(readOnly = true)
    public List<Review> getRatingsByMovie(Movie movie) {
        return reviewRepository.findByMovie(movie);
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
