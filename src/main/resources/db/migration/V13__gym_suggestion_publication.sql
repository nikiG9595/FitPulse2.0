-- Additive proposal metadata and an explicit idempotent link to the published gym.
-- Existing proposals, gyms, reviews, favorites and workout references are preserved.
ALTER TABLE gym_suggestions
    ADD COLUMN phone VARCHAR(50) NULL,
    ADD COLUMN price_info VARCHAR(1000) NULL,
    ADD COLUMN image_url VARCHAR(1000) NULL,
    ADD COLUMN published_gym_id BIGINT NULL,
    ADD CONSTRAINT fk_suggestions_published_gym FOREIGN KEY (published_gym_id) REFERENCES gyms(id) ON DELETE SET NULL;
