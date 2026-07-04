package com.linus.application.backend.user.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRole(Role role);

    Optional<User> findByFriendCode(String friendCode);

    boolean existsByFriendCode(String friendCode);
}
