-- file: seeds/init.sql

CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    channel VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_events_channel_id ON events (channel, id);

CREATE TABLE IF NOT EXISTS user_subscriptions (
    user_id INTEGER NOT NULL,
    channel VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT user_subscriptions_pk PRIMARY KEY (user_id, channel)
);

-- Insert dummy users data (users are implicit in this system, but we seed some subscriptions)
INSERT INTO user_subscriptions (user_id, channel) VALUES
(1, 'alerts'),
(1, 'notifications'),
(1, 'history-channel'),
(2, 'alerts'),
(2, 'history-channel')
ON CONFLICT DO NOTHING;

-- Insert some dummy events for testing pagination/history
INSERT INTO events (channel, event_type, payload) VALUES
('alerts', 'SYSTEM_ALERT', '{"message": "System startup"}'),
('alerts', 'USER_SIGNUP', '{"userId": 1}'),
('notifications', 'MSG_RECV', '{"from": "admin", "text": "Welcome"}')
ON CONFLICT DO NOTHING;

-- Insert 10 events for history-channel to test pagination (Req 12)
INSERT INTO events (channel, event_type, payload) VALUES
('history-channel', 'HISTORY_EVENT', '{"index": 1, "message": "History event 1"}'),
('history-channel', 'HISTORY_EVENT', '{"index": 2, "message": "History event 2"}'),
('history-channel', 'HISTORY_EVENT', '{"index": 3, "message": "History event 3"}'),
('history-channel', 'HISTORY_EVENT', '{"index": 4, "message": "History event 4"}'),
('history-channel', 'HISTORY_EVENT', '{"index": 5, "message": "History event 5"}'),
('history-channel', 'HISTORY_EVENT', '{"index": 6, "message": "History event 6"}'),
('history-channel', 'HISTORY_EVENT', '{"index": 7, "message": "History event 7"}'),
('history-channel', 'HISTORY_EVENT', '{"index": 8, "message": "History event 8"}'),
('history-channel', 'HISTORY_EVENT', '{"index": 9, "message": "History event 9"}'),
('history-channel', 'HISTORY_EVENT', '{"index": 10, "message": "History event 10"}')
ON CONFLICT DO NOTHING;
