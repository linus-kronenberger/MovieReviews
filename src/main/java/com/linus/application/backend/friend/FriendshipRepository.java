package com.linus.application.backend.friend;

import com.linus.application.backend.user.infrastructure.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    @Query("SELECT f FROM Friendship f JOIN FETCH f.user1 JOIN FETCH f.user2 WHERE f.user1 = :user OR f.user2 = :user")
    List<Friendship> findByUser(@Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f WHERE (f.user1 = :user1 AND f.user2 = :user2) OR (f.user1 = :user2 AND f.user2 = :user1)")
    boolean existsByUser1AndUser2(@Param("user1") User user1, @Param("user2") User user2);
}
