package com.linus.application.backend.essay.infrastructure;

import com.linus.application.backend.review.infrastructure.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EssayRepository extends CrudRepository<Essay, Long> {
    List<Essay> findByReviewIn(List<Review> reviews);
}