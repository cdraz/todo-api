-- V1__initial_schema.sql
-- Initial schema for the Todo application

-- Users table
-- Tracks individual users by email
CREATE TABLE users (
    id           BIGSERIAL  PRIMARY KEY,                  -- Auto-incrementing unique user ID
    email        TEXT       NOT NULL UNIQUE,              -- User identifier (no authentication implemented)
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT now()   -- Timestamp for user creation
);

-- Todos table
-- Tracks tasks created by users
CREATE TABLE todos (
    id           BIGSERIAL PRIMARY KEY,                          -- Auto-incrementing todo ID
    title        TEXT NOT NULL,                                  -- Todo description/title
    completed    BOOLEAN NOT NULL DEFAULT false,                 -- Completion status
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT now(),         -- Timestamp for todo creation
    user_id      BIGINT REFERENCES users(id) ON DELETE SET NULL  -- Optional FK to user
);

-- Index to optimize queries filtering todos by user
CREATE INDEX idx_todos_user_id ON todos(user_id);

-- Index to optimize queries looking up users by email
CREATE INDEX idx_users_email ON users(email);
