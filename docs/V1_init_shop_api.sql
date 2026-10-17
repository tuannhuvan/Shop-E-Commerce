-- V1__init_shop_api.sql
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    id CHAR(36) NOT NULL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id CHAR(36) NOT NULL PRIMARY KEY,
    category_id CHAR(36) NOT NULL,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    quantity INT NOT NULL DEFAULT 0,
    description TEXT NULL,
    image_url VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE orders (
    id CHAR(36) NOT NULL PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    cancel_reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE order_items (
    id CHAR(36) NOT NULL PRIMARY KEY,
    order_id CHAR(36) NOT NULL,
    product_id CHAR(36) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO categories (id, name, slug, description) VALUES
('cat001', 'Laptop & Máy Tính', 'laptop-may-tinh', 'Laptop văn phòng và Gaming'),
('cat002', 'Điện Thoại & Tablet', 'dien-thoai-tablet', 'Smartphone và Máy tính bảng');

INSERT INTO customers (id, full_name, email, phone, address) VALUES
('c101', 'Nguyễn Văn An', 'an.nguyen@test.com', '0912345678', '123 Lê Lợi, TP.HCM'),
('c102', 'Trần Thị Bình', 'binh.tran@test.com', '0987654321', '456 Hoàn Kiếm, Hà Nội');

INSERT INTO products (id, category_id, name, price, quantity, description, image_url) VALUES
('p201', 'cat001', 'Laptop Dell XPS 15', 35000000.00, 25, 'Intel Core i9, RAM 32GB', 'https://cdn.test/dell.jpg'),
('p202', 'cat002', 'iPhone 16 Pro Max', 34990000.00, 40, 'Chip A18 Pro 256GB', 'https://cdn.test/iphone.jpg');

INSERT INTO orders (id, customer_id, status, total_amount) VALUES
('o301', 'c101', 'PENDING', 35000000.00),
('o302', 'c102', 'PAID', 34990000.00);

INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, subtotal) VALUES
('i401', 'o301', 'p201', 1, 35000000.00, 35000000.00),
('i402', 'o302', 'p202', 1, 34990000.00, 34990000.00);
