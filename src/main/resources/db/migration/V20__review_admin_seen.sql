-- Review visibility remains governed by status. This flag tracks administrative reading only.
ALTER TABLE reviews ADD COLUMN admin_reviewed BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_reviews_admin_reviewed ON reviews(admin_reviewed,created_at);
