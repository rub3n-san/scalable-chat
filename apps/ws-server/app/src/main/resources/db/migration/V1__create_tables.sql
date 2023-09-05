CREATE TABLE t_user (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       last_login TIMESTAMP
);

CREATE TABLE t_channel (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_member (
                       id SERIAL PRIMARY KEY,
                       user_id BIGINT REFERENCES t_user(id) NOT NULL,
                       channel_id BIGINT REFERENCES t_channel(id) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE t_chat (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT REFERENCES t_user(id) NOT NULL,
                         channel_id BIGINT REFERENCES t_channel(id) NOT NULL,
                         document_id TEXT NOT NULL, --reference to mongodb
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);