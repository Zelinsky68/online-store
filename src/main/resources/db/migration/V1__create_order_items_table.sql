-- Таблица для позиций заказа
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_order_product UNIQUE (order_id, product_id)
);

-- Индексы для производительности
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Обновляем CHECK constraint для статусов заказа
ALTER TABLE orders DROP CONSTRAINT IF EXISTS chk_order_status;
ALTER TABLE orders ADD CONSTRAINT chk_order_status 
    CHECK (status IN ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'));

-- Функция для обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Триггер для order_items
DROP TRIGGER IF EXISTS update_order_items_updated_at ON order_items;
CREATE TRIGGER update_order_items_updated_at 
    BEFORE UPDATE ON order_items 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Функция для обновления total_amount в orders
CREATE OR REPLACE FUNCTION update_order_total_amount()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE orders 
    SET total_amount = (
        SELECT COALESCE(SUM(price * quantity), 0)
        FROM order_items 
        WHERE order_id = COALESCE(NEW.order_id, OLD.order_id)
    )
    WHERE id = COALESCE(NEW.order_id, OLD.order_id);
    RETURN COALESCE(NEW, OLD);
END;
$$ language 'plpgsql';

-- Триггеры для автоматического обновления total_amount
DROP TRIGGER IF EXISTS update_order_total_on_insert ON order_items;
CREATE TRIGGER update_order_total_on_insert
    AFTER INSERT ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_order_total_amount();

DROP TRIGGER IF EXISTS update_order_total_on_update ON order_items;
CREATE TRIGGER update_order_total_on_update
    AFTER UPDATE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_order_total_amount();

DROP TRIGGER IF EXISTS update_order_total_on_delete ON order_items;
CREATE TRIGGER update_order_total_on_delete
    AFTER DELETE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_order_total_amount();
