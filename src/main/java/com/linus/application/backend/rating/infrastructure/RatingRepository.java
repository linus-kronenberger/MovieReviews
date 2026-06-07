package com.linus.application.backend.rating.infrastructure;

import com.linus.application.backend.movie.infrastructure.Movie;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends CrudRepository<Rating, Long> {
    @Query("SELECT r FROM Rating r JOIN FETCH r.category WHERE r.movie = :movie")
    List<Rating> findByMovie(@Param("movie") Movie movie);
}
