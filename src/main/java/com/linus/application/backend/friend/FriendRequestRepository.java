package com.linus.application.backend.friend;

import com.linus.application.backend.user.infrastructure.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, String> {

    @Query("SELECT r FROM FriendRequest r JOIN FETCH r.fromUser WHERE r.targetUser = :targetUser")
    List<FriendRequest> findByTargetUser(@Param("targetUser") User targetUser);

    @Query("SELECT r FROM FriendRequest r JOIN FETCH r.targetUser WHERE r.fromUser = :fromUser")
    List<FriendRequest> findByFromUser(@Param("fromUser") User fromUser);

    @Query("SELECT r FROM FriendRequest r WHERE r.fromUser = :fromUser AND r.targetUser = :targetUser")
    Optional<FriendRequest> findByFromUserAndTargetUser(@Param("fromUser") User fromUser, @Param("targetUser") User targetUser);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM FriendRequest r WHERE r.fromUser = :fromUser AND r.targetUser = :targetUser")
    boolean existsByFromUserAndTargetUser(@Param("fromUser") User fromUser, @Param("targetUser") User targetUser);

    void deleteByFromUserOrTargetUser(User fromUser, User targetUser);
}
