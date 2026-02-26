-- =============================================
-- V3: Добавление недостающих таблиц для интернет-магазина
-- =============================================

-- 1. ТАБЛИЦА КАТЕГОРИЙ (categories)
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id BIGINT REFERENCES categories(id) ON DELETE CASCADE,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для categories
CREATE INDEX IF NOT EXISTS idx_categories_parent ON categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_active ON categories(is_active);

-- 2. СВЯЗЬ ТОВАРОВ С КАТЕГОРИЯМИ (product_categories)
CREATE TABLE IF NOT EXISTS product_categories (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, category_id)
);

-- Индексы для product_categories
CREATE INDEX IF NOT EXISTS idx_product_categories_category ON product_categories(category_id);

-- 3. ТАБЛИЦА ПЛАТЕЖЕЙ (payments)
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    payment_number VARCHAR(50) UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL, -- CARD, CASH, ONLINE, etc.
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для payments
CREATE INDEX IF NOT EXISTS idx_payments_order ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(payment_status);
CREATE INDEX IF NOT EXISTS idx_payments_date ON payments(payment_date);

-- 4. ТАБЛИЦА ДОСТАВКИ (deliveries)
CREATE TABLE IF NOT EXISTS deliveries (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    delivery_number VARCHAR(50) UNIQUE,
    delivery_method VARCHAR(50) NOT NULL, -- COURIER, PICKUP, POST
    delivery_status VARCHAR(20) DEFAULT 'PENDING',
    tracking_number VARCHAR(100),
    recipient_name VARCHAR(200) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    delivery_notes TEXT,
    estimated_date DATE,
    actual_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для deliveries
CREATE INDEX IF NOT EXISTS idx_deliveries_order ON deliveries(order_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_tracking ON deliveries(tracking_number);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(delivery_status);

-- 5. ТАБЛИЦА ОТЗЫВОВ (reviews)
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(200),
    comment TEXT,
    is_approved BOOLEAN DEFAULT false,
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, user_id)
);

-- Индексы для reviews
CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews(rating);
CREATE INDEX IF NOT EXISTS idx_reviews_approved ON reviews(is_approved);

-- 6. ТАБЛИЦА ИСТОРИИ СТАТУСОВ ЗАКАЗОВ (order_status_history)
CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для order_status_history
CREATE INDEX IF NOT EXISTS idx_order_history_order ON order_status_history(order_id);
CREATE INDEX IF NOT EXISTS idx_order_history_date ON order_status_history(changed_at DESC);

-- 7. ТАБЛИЦА ИЗБРАННОГО (wishlist)
CREATE TABLE IF NOT EXISTS wishlist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
);

-- Индексы для wishlist
CREATE INDEX IF NOT EXISTS idx_wishlist_user ON wishlist(user_id);
CREATE INDEX IF NOT EXISTS idx_wishlist_product ON wishlist(product_id);

-- 8. ТАБЛИЦА ПРОМОКОДОВ (promocodes)
CREATE TABLE IF NOT EXISTS promocodes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(20) NOT NULL, -- PERCENTAGE, FIXED
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(10,2),
    max_discount_amount DECIMAL(10,2),
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    max_uses INTEGER,
    used_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для promocodes
CREATE INDEX IF NOT EXISTS idx_promocodes_code ON promocodes(code);
CREATE INDEX IF NOT EXISTS idx_promocodes_valid ON promocodes(valid_from, valid_to);
CREATE INDEX IF NOT EXISTS idx_promocodes_active ON promocodes(is_active);

-- 9. ТАБЛИЦА ПРИМЕНЕННЫХ ПРОМОКОДОВ (order_promocodes)
CREATE TABLE IF NOT EXISTS order_promocodes (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    promocode_id BIGINT NOT NULL REFERENCES promocodes(id) ON DELETE CASCADE,
    discount_amount DECIMAL(10,2) NOT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(order_id)
);

-- Индексы для order_promocodes
CREATE INDEX IF NOT EXISTS idx_order_promocodes_order ON order_promocodes(order_id);

-- =============================================
-- ДОБАВЛЯЕМ ТРИГГЕРЫ ДЛЯ АВТОМАТИЧЕСКОГО ОБНОВЛЕНИЯ
-- =============================================

-- Функция для обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Триггеры для всех новых таблиц
DROP TRIGGER IF EXISTS update_categories_updated_at ON categories;
CREATE TRIGGER update_categories_updated_at 
    BEFORE UPDATE ON categories 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_payments_updated_at ON payments;
CREATE TRIGGER update_payments_updated_at 
    BEFORE UPDATE ON payments 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_deliveries_updated_at ON deliveries;
CREATE TRIGGER update_deliveries_updated_at 
    BEFORE UPDATE ON deliveries 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_reviews_updated_at ON reviews;
CREATE TRIGGER update_reviews_updated_at 
    BEFORE UPDATE ON reviews 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_promocodes_updated_at ON promocodes;
CREATE TRIGGER update_promocodes_updated_at 
    BEFORE UPDATE ON promocodes 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- ДОБАВЛЯЕМ ТЕСТОВЫЕ ДАННЫЕ
-- =============================================

-- Добавляем тестовые категории
INSERT INTO categories (name, description, sort_order) VALUES
('Электроника', 'Все виды электронных устройств', 1),
('Смартфоны', 'Мобильные телефоны и аксессуары', 2),
('Ноутбуки', 'Портативные компьютеры', 3),
('Аксессуары', 'Чехлы, защитные стекла и другое', 4)
ON CONFLICT (name) DO NOTHING;

-- Добавляем тестовые промокоды
INSERT INTO promocodes (code, discount_type, discount_value, valid_from, valid_to, max_uses) VALUES
('WELCOME10', 'PERCENTAGE', 10, NOW(), NOW() + INTERVAL '30 days', 100),
('SALE2026', 'PERCENTAGE', 15, NOW(), NOW() + INTERVAL '60 days', 200),
('SUMMER', 'FIXED', 1000, NOW(), NOW() + INTERVAL '90 days', 50)
ON CONFLICT (code) DO NOTHING;
