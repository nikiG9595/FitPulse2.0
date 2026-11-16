-- Preserve provenance when the existing source FK is cleared by ON DELETE SET NULL.
-- A legacy row without a source cannot safely be classified from its name or exercises.
ALTER TABLE user_programs ADD COLUMN origin VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN';
UPDATE user_programs SET origin='CATALOG' WHERE source_template_id IS NOT NULL;
CREATE INDEX idx_user_programs_origin_listed ON user_programs(origin,listed);
