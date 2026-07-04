package com.linus.application.backend.friend;

import com.linus.application.backend.user.infrastructure.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table(name = "friend_request")
@Entity
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id")
    private String id;

    @JoinColumn(name = "from_user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User fromUser;

    @JoinColumn(name = "target_user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User targetUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public FriendRequest() {}

    public FriendRequest(User fromUser, User targetUser) {
        this.fromUser = fromUser;
        this.targetUser = targetUser;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }
    public User getTargetUser() { return targetUser; }
    public void setTargetUser(User targetUser) { this.targetUser = targetUser; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
