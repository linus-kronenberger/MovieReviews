package com.linus.application.backend.rating;

import com.linus.application.backend.user.infrastructure.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingCategoryRepository extends CrudRepository<RatingCategory, String> {
    List<RatingCategory> findByUser(User user);
    List<RatingCategory> findByUserIn(List<User> users);
}
