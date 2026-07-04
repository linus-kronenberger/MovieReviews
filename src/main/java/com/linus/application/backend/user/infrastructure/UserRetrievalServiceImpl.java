package com.linus.application.backend.user.infrastructure;

import com.linus.application.backend.user.domain.UserRetrievalService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRetrievalServiceImpl implements UserRetrievalService {

  private final UserRepository userRepository;

  public UserRetrievalServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  @Override
  public Optional<List<User>> findAdminUsers() {
    List<User> adminUsers = userRepository.findByRole(Role.ADMIN);
    if(adminUsers != null && adminUsers.size() > 0) {
      return Optional.of(adminUsers);
    } else {
      return Optional.empty();
    }
  }
}
