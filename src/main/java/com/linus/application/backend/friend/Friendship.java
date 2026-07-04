package com.linus.application.backend.friend;

import com.linus.application.backend.user.infrastructure.User;
import jakarta.persistence.*;

@Table(name = "friendship")
@Entity
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "friendship_id")
    private String id;

    @JoinColumn(name = "user1_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user1;

    @JoinColumn(name = "user2_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user2;

    public Friendship() {}

    public Friendship(User user1, User user2) {
        if (user1.getId().compareTo(user2.getId()) < 0) {
            this.user1 = user1;
            this.user2 = user2;
        } else {
            this.user1 = user2;
            this.user2 = user1;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public User getUser1() { return user1; }
    public void setUser1(User user1) { this.user1 = user1; }
    public User getUser2() { return user2; }
    public void setUser2(User user2) { this.user2 = user2; }
}
