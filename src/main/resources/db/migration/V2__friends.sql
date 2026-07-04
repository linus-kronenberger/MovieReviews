ALTER TABLE app_user ADD COLUMN friend_code VARCHAR(8);
CREATE UNIQUE INDEX IF NOT EXISTS idx_app_user_friend_code ON app_user(friend_code);

CREATE TABLE IF NOT EXISTS friend_request (
    request_id VARCHAR(255) PRIMARY KEY,
    from_user_id VARCHAR(255) REFERENCES app_user(user_id),
    target_user_id VARCHAR(255) REFERENCES app_user(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS friendship (
    friendship_id VARCHAR(255) PRIMARY KEY,
    user1_id VARCHAR(255) REFERENCES app_user(user_id),
    user2_id VARCHAR(255) REFERENCES app_user(user_id)
);
