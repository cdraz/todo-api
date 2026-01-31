-- --------------------------------------------------
-- USERS
-- --------------------------------------------------

-- List users (newest first)
SELECT id, email, created_at
FROM public.users
ORDER BY created_at DESC;

-- Find a user by id
SELECT id, email, created_at
FROM public.users
WHERE id = :user_id;

-- Find a user by email (exact)
SELECT id, email, created_at
FROM public.users
WHERE email = :email;

-- Count users
SELECT COUNT(*) AS user_count
FROM public.users;

-- -- Delete a user (will cascade to todos)
-- -- DELETE FROM public.users WHERE id = :user_id;

-- --------------------------------------------------
-- TODOS
-- --------------------------------------------------

-- List all todos (newest first)
SELECT id, title, completed, created_at, user_id
FROM public.todos
ORDER BY created_at DESC;

-- Find a todo by id
SELECT id, title, completed, created_at, user_id
FROM public.todos
WHERE id = :todo_id;

-- Fetch todos for a given userId (newest first)
SELECT id, title, completed, created_at, user_id
FROM public.todos
WHERE user_id = :user_id
ORDER BY created_at DESC;

-- Fetch only incomplete todos for a userId
SELECT id, title, completed, created_at, user_id
FROM public.todos
WHERE user_id = :user_id
  AND completed = false
ORDER BY created_at DESC;

-- Fetch only completed todos for a userId
SELECT id, title, completed, created_at, user_id
FROM public.todos
WHERE user_id = :user_id
  AND completed = true
ORDER BY created_at DESC;

-- -- Delete all todos for a user
-- -- DELETE FROM public.todos WHERE user_id = :user_id;
