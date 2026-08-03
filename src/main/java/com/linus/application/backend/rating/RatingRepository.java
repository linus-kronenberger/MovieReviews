package com.linus.application.backend.rating;

import com.linus.application.backend.review.infrastructure.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends CrudRepository<Rating, String> {
    void deleteByReviewIn(List<Review> reviews);
    List<Rating> findByReviewIn(List<Review> reviews);
    List<Rating> findByCategoryIn(List<RatingCategory> categories);
    List<Rating> findByCategory(RatingCategory category);
}
