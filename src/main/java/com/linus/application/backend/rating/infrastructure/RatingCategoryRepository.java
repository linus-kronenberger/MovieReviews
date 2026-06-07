package com.linus.application.backend.rating.infrastructure;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingCategoryRepository extends CrudRepository<RatingCategory, Long> {
}
