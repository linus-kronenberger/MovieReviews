package com.linus.application.backend.review.infrastructure;

import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.user.infrastructure.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends CrudRepository<Review, String> {
    @Query("SELECT r FROM Review r WHERE r.movie = :movie")
    List<Review> findByMovie(@Param("movie") Movie movie);
    Review findByUser(User user);
    void deleteByUser(User user);
}
