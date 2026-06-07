package com.linus.application.backend.rating.infrastructure;

import com.linus.application.backend.user.infrastructure.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingCategoryRepository extends CrudRepository<RatingCategory, Long> {
    List<RatingCategory> findByUser(User user);
}
