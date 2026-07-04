CREATE TABLE IF NOT EXISTS movie (
    movie_id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    imdb_id VARCHAR(255),
    type VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS app_user (
    user_id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    password_hash VARCHAR(255),
    role VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS rating_category (
    category_id VARCHAR(255) PRIMARY KEY,
    category VARCHAR(255),
    description TEXT,
    user_id VARCHAR(255) REFERENCES app_user(user_id)
);

CREATE TABLE IF NOT EXISTS review (
    review_id VARCHAR(255) PRIMARY KEY,
    movie_id VARCHAR(255) REFERENCES movie(movie_id),
    user_id VARCHAR(255) REFERENCES app_user(user_id),
    avg_rating DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    is_draft BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS essay (
    essay_id VARCHAR(255) PRIMARY KEY,
    text TEXT,
    version INTEGER,
    review_id VARCHAR(255) REFERENCES review(review_id)
);

CREATE TABLE IF NOT EXISTS rating (
    rating_id VARCHAR(255) PRIMARY KEY,
    review_id VARCHAR(255) REFERENCES review(review_id),
    category_id VARCHAR(255) REFERENCES rating_category(category_id),
    stars DOUBLE PRECISION
);
