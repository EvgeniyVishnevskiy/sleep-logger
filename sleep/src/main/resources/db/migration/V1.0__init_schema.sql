CREATE TABLE IF NOT EXISTS daily_sleep_log (
    id UUID as identity PRIMARY KEY,
    sleep_start TIMESTAMP NOT NULL,
    sleep_end TIMESTAMP NOT NULL,
    sleep_quality INT NOT NULL,
    sleep_duration INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);