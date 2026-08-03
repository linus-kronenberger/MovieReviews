DELETE FROM review
WHERE review_id NOT IN (
    SELECT MIN(review_id)
    FROM review
    GROUP BY user_id, movie_id
);

ALTER TABLE review ADD CONSTRAINT uk_review_user_movie UNIQUE (user_id, movie_id);
