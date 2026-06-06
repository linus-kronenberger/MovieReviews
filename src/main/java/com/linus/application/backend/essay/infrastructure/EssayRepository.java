package com.linus.application.backend.essay.infrastructure;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EssayRepository extends CrudRepository<Essay, Long> {
}
