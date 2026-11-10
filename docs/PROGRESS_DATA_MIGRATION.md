# Progress data migration runbook

The old service stores records in `fitpulse_progress.progress_records`. The monolith now stores the same business data in its own `progress_records` table and adds a foreign key to `users(id)`.

Do not run a transfer against an unknown or production database without a reviewed backup and maintenance plan.

## Required preflight checks

1. Back up both databases and verify that the backups can be restored.
2. Stop writes to the old Progress service or place it in read-only mode.
3. Confirm that both databases use compatible UUID representations (`BINARY(16)` in the supplied migrations).
4. Compare every distinct source `user_id` with `fitpulse.users.id`. Resolve orphan records before import; never create placeholder users.
5. Check for duplicate `(user_id, recorded_at)` rows and invalid measurement ranges.
6. Test the process on restored copies of the databases first.

## Transfer strategy

Export only these columns from the old table: `id`, `user_id`, `weight`, `body_fat_percentage`, `recorded_at`, `note`, `created_at`, and `updated_at`. Import them into a staging table in the destination database. Validate row counts, UUID ownership, constraints, date ordering and representative checksums before inserting into `progress_records` in one reviewed transaction.

The final insert should explicitly name every column and should reject conflicts rather than overwrite destination data. The exact transfer statement must be prepared after inspecting the real MySQL schemas and UUID column metadata. It is intentionally not automated in this repository.

After verification, keep the old database read-only for the agreed retention period. Decommission the service only after users can view, create, edit and delete their migrated records in the monolith.
