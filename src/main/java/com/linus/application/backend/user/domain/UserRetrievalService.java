package com.linus.application.backend.user.domain;

import com.linus.application.backend.user.infrastructure.User;
import java.util.List;
import java.util.Optional;

/**
 * This service is responsible for providing allowed user information
 * for the presentation layer of the application.
 */
public interface UserRetrievalService {
  /**
   * This method is there in order to find the admin users.
   */
  Optional<List<User>> findAdminUsers();
}
