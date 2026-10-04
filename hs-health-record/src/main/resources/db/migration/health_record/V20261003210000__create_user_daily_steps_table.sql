-- =========================================================================
-- Health Record & Workout Module Database Migration: User Daily Steps
-- Naming: V20261003210000__create_user_daily_steps_table.sql
-- =========================================================================

CREATE TABLE IF NOT EXISTS user_daily_steps (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    step_date DATE NOT NULL,
    total_steps INTEGER NOT NULL DEFAULT 0,
    distance_meters DOUBLE PRECISION DEFAULT 0,
    calories_burned INTEGER DEFAULT 0,
    active_minutes INTEGER DEFAULT 0,
    target_steps INTEGER NOT NULL DEFAULT 6000,
    hourly_breakdown_json TEXT,
    device_source VARCHAR(50),
    synced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_user_step_date UNIQUE (user_id, step_date)
);

CREATE INDEX IF NOT EXISTS idx_user_daily_steps_user_date ON user_daily_steps (user_id, step_date);
