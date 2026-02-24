-- Обновляем CHECK constraint для ролей пользователей
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check 
    CHECK (role IN ('CUSTOMER', 'ADMIN', 'MANAGER', 'MODERATOR'));

-- Увеличиваем длину поля password для хешированных паролей
ALTER TABLE users ALTER COLUMN password TYPE VARCHAR(255);

-- Добавляем индекс на is_active для частых запросов
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

-- Обновляем значения ролей, если есть старые
UPDATE users SET role = 'CUSTOMER' WHERE role = 'USER';
