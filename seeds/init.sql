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
(2, 'alerts')
ON CONFLICT DO NOTHING;

-- Insert some dummy events for testing pagination/history
INSERT INTO events (channel, event_type, payload) VALUES
('alerts', 'SYSTEM_ALERT', '{"message": "System startup"}'),
('alerts', 'USER_SIGNUP', '{"userId": 1}'),
('notifications', 'MSG_RECV', '{"from": "admin", "text": "Welcome"}')
ON CONFLICT DO NOTHING;
