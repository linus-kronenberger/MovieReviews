package com.linus.application.backend.friend;

import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.backend.user.infrastructure.UserRepository;
import com.linus.application.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FriendService {

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final AuthenticatedUser authenticatedUser;

    public FriendService(UserRepository userRepository,
                         FriendRequestRepository friendRequestRepository,
                         FriendshipRepository friendshipRepository,
                         AuthenticatedUser authenticatedUser) {
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.friendshipRepository = friendshipRepository;
        this.authenticatedUser = authenticatedUser;
    }

    public static String generateFriendCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }

    public static String generateUniqueFriendCode(UserRepository userRepository) {
        String code;
        int attempts = 0;
        do {
            code = generateFriendCode();
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("Could not generate unique friend code");
            }
        } while (userRepository.existsByFriendCode(code));
        return code;
    }

    @Transactional
    public String sendFriendRequest(String friendCode) {
        User currentUser = authenticatedUser.get().orElseThrow();

        if (currentUser.getFriendCode().equals(friendCode)) {
            return "friend.error.ownCode";
        }

        Optional<User> targetUser = userRepository.findByFriendCode(friendCode);
        if (targetUser.isEmpty()) {
            return "friend.error.codeInvalid";
        }

        if (friendRequestRepository.existsByFromUserAndTargetUser(currentUser, targetUser.get())) {
            return "friend.error.requestExists";
        }

        if (friendshipRepository.existsByUser1AndUser2(currentUser, targetUser.get()) ||
            friendshipRepository.existsByUser1AndUser2(targetUser.get(), currentUser)) {
            return "friend.error.alreadyFriends";
        }

        FriendRequest request = new FriendRequest(currentUser, targetUser.get());
        friendRequestRepository.save(request);

        Optional<FriendRequest> reciprocal = friendRequestRepository
                .findByFromUserAndTargetUser(targetUser.get(), currentUser);

        if (reciprocal.isPresent()) {
            Friendship friendship = new Friendship(currentUser, targetUser.get());
            friendshipRepository.save(friendship);
            friendRequestRepository.delete(request);
            friendRequestRepository.delete(reciprocal.get());
            return "friend.success.matched";
        }

        return "friend.success.sent";
    }

    @Transactional(readOnly = true)
    public List<String> getFriendIds(User user) {
        List<Friendship> friendships = friendshipRepository.findByUser(user);
        return friendships.stream()
                .flatMap(f -> Stream.of(f.getUser1().getId(), f.getUser2().getId()))
                .filter(id -> !id.equals(user.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FriendRequest> getReceivedRequests(User user) {
        return friendRequestRepository.findByTargetUser(user);
    }

    @Transactional(readOnly = true)
    public List<FriendRequest> getSentRequests(User user) {
        return friendRequestRepository.findByFromUser(user);
    }

    @Transactional(readOnly = true)
    public List<User> getFriends(User user) {
        List<Friendship> friendships = friendshipRepository.findByUser(user);
        return friendships.stream()
                .flatMap(f -> Stream.of(f.getUser1(), f.getUser2()))
                .filter(u -> !u.getId().equals(user.getId()))
                .collect(Collectors.toList());
    }
}
