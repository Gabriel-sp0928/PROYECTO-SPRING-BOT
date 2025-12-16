-- Recreate schema and seed data (H2-compatible)
-- Drops tables in safe order, creates schema, then performs idempotent seeds.

-- Drop dependent tables first
DROP TABLE IF EXISTS quote_details;
DROP TABLE IF EXISTS sale_details;
DROP TABLE IF EXISTS inventory_movements;
DROP TABLE IF EXISTS quotes;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- Create primary tables
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  enabled BOOLEAN NOT NULL,
  name VARCHAR(255),
  email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1000),
  price DECIMAL(19,2) DEFAULT 0,
  stock INT DEFAULT 0,
  category VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS quotes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  status VARCHAR(50),
  total DECIMAL(19,2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_quote_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS quote_details (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  quote_id BIGINT,
  product_id BIGINT,
  quantity INT DEFAULT 1,
  unit_price DECIMAL(19,2) DEFAULT 0,
  CONSTRAINT fk_qd_quote FOREIGN KEY (quote_id) REFERENCES quotes(id) ON DELETE CASCADE,
  CONSTRAINT fk_qd_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS sales (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  total DECIMAL(19,2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sale_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS sale_details (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sale_id BIGINT NOT NULL,
  product_id BIGINT,
  quantity INT DEFAULT 1,
  unit_price DECIMAL(19,2) DEFAULT 0,
  subtotal DECIMAL(19,2) DEFAULT 0,
  CONSTRAINT fk_sd_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
  CONSTRAINT fk_sd_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS inventory_movements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  quantity INT NOT NULL,
  reason VARCHAR(255),
  type VARCHAR(10) NOT NULL,
  product_id BIGINT NOT NULL,
  CONSTRAINT fk_im_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Idempotent seeds: users removed from SQL because passwords were stored in plain text.
-- User creation (with password hashing) is performed by the Java initializer `DataInitializer`.
-- Keep product seeds below.

MERGE INTO products (name, price, stock, category) KEY(name) VALUES
('Corral',150000.00,20,'Corrales'),
('Cuna Plegable',200000.00,15,'Cunas'),
('Silla Auto',120000.00,25,'Sillas'),
('Cambiador',45000.00,30,'Accesorios'),
('Monitor',95000.00,10,'Electronica'),
('Silla Alta',80000.00,18,'Muebles'),
('Andador',50000.00,12,'Juguetes'),
('Bebe Confort',30000.00,22,'Accesorios'),
('Termometro',8000.00,50,'Salud'),
('Juguete Musical',12000.00,40,'Juguetes'),
('Cochecito',250000.00,8,'Cochecitos'),
('Babero',5000.00,100,'Ropa'),
('Porta Bebe',90000.00,14,'Accesorios'),
('Biberon',7000.00,60,'Alimentacion'),
('Calienta Biberon',15000.00,20,'Electronica'),
('Hamaca',45000.00,11,'Muebles'),
('Protector Cama',6000.00,70,'Ropa'),
('Juego Mesa',22000.00,9,'Juguetes'),
('Almohadon',18000.00,16,'Ropa'),
('Cesta Ropa',10000.00,30,'Hogar');

-- Example safe insert for inventory_movements referencing product by name
INSERT INTO inventory_movements (date, quantity, type, product_id)
SELECT CURRENT_TIMESTAMP, 10, 'IN', p.id
FROM products p
WHERE p.name = 'Cuna Plegable';

-- Deterministic users for referential integrity (IDs 1..20)
-- NOTE: passwords here use the {noop} scheme so migrations can run in demo environments.
-- Production setups should avoid plaintext/noop passwords and rely on an application initializer
-- that inserts bcrypt-hashed credentials. The Java `DataInitializer` will upsert the professional
-- accounts (admin/cliente/logistics) and replace their passwords with bcrypt at startup.
INSERT INTO users (id, username, password, role, enabled, name, email) VALUES
(1,'admin','{noop}admin123','ADMIN',true,'Admin','admin@example.local'),
(2,'user1','{noop}pass1','CLIENT',true,'User 1','user1@example.local'),
(3,'user2','{noop}pass2','CLIENT',true,'User 2','user2@example.local'),
(4,'user3','{noop}pass3','CLIENT',true,'User 3','user3@example.local'),
(5,'user4','{noop}pass4','CLIENT',true,'User 4','user4@example.local'),
(6,'user5','{noop}pass5','CLIENT',true,'User 5','user5@example.local'),
(7,'user6','{noop}pass6','CLIENT',true,'User 6','user6@example.local'),
(8,'user7','{noop}pass7','CLIENT',true,'User 7','user7@example.local'),
(9,'user8','{noop}pass8','CLIENT',true,'User 8','user8@example.local'),
(10,'user9','{noop}pass9','CLIENT',true,'User 9','user9@example.local'),
(11,'user10','{noop}pass10','CLIENT',true,'User 10','user10@example.local'),
(12,'user11','{noop}pass11','CLIENT',true,'User 11','user11@example.local'),
(13,'user12','{noop}pass12','CLIENT',true,'User 12','user12@example.local'),
(14,'user13','{noop}pass13','CLIENT',true,'User 13','user13@example.local'),
(15,'user14','{noop}pass14','CLIENT',true,'User 14','user14@example.local'),
(16,'user15','{noop}pass15','CLIENT',true,'User 15','user15@example.local'),
(17,'user16','{noop}pass16','CLIENT',true,'User 16','user16@example.local'),
(18,'user17','{noop}pass17','CLIENT',true,'User 17','user17@example.local'),
(19,'user18','{noop}pass18','CLIENT',true,'User 18','user18@example.local'),
(20,'user20','{noop}pass20','CLIENT',true,'User 20','user20@example.local');

-- Ensure the ID sequence restarts after the manual inserts so generated ids don't collide
ALTER TABLE users ALTER COLUMN id RESTART WITH 21;

-- Quotes (no explicit IDs)
INSERT INTO quotes (user_id, status, total, created_at) VALUES
(1,'NEW',150000.00,CURRENT_TIMESTAMP),
(2,'PENDING',200000.00,CURRENT_TIMESTAMP),
(3,'APPROVED',120000.00,CURRENT_TIMESTAMP),
(4,'NEW',45000.00,CURRENT_TIMESTAMP),
(5,'PENDING',95000.00,CURRENT_TIMESTAMP),
(6,'APPROVED',80000.00,CURRENT_TIMESTAMP),
(7,'NEW',50000.00,CURRENT_TIMESTAMP),
(8,'PENDING',30000.00,CURRENT_TIMESTAMP),
(9,'APPROVED',8000.00,CURRENT_TIMESTAMP),
(10,'NEW',12000.00,CURRENT_TIMESTAMP),
(11,'PENDING',250000.00,CURRENT_TIMESTAMP),
(12,'APPROVED',5000.00,CURRENT_TIMESTAMP),
(13,'NEW',90000.00,CURRENT_TIMESTAMP),
(14,'PENDING',7000.00,CURRENT_TIMESTAMP),
(15,'APPROVED',15000.00,CURRENT_TIMESTAMP),
(16,'NEW',45000.00,CURRENT_TIMESTAMP),
(17,'PENDING',6000.00,CURRENT_TIMESTAMP),
(18,'APPROVED',22000.00,CURRENT_TIMESTAMP),
(19,'NEW',18000.00,CURRENT_TIMESTAMP),
(20,'PENDING',10000.00,CURRENT_TIMESTAMP);

-- Quote details
INSERT INTO quote_details (quote_id, product_id, quantity, unit_price) VALUES
(1,1,1,150000.00),
(2,2,1,200000.00),
(3,3,1,120000.00),
(4,4,1,45000.00),
(5,5,1,95000.00),
(6,6,1,80000.00),
(7,7,1,50000.00),
(8,8,1,30000.00),
(9,9,1,8000.00),
(10,10,1,12000.00),
(11,11,1,250000.00),
(12,12,1,5000.00),
(13,13,1,90000.00),
(14,14,1,7000.00),
(15,15,1,15000.00),
(16,16,1,45000.00),
(17,17,1,6000.00),
(18,18,1,22000.00),
(19,19,1,18000.00),
(20,20,1,10000.00);

-- Sales
INSERT INTO sales (user_id, total, created_at) VALUES
(1,150000.00,CURRENT_TIMESTAMP),
(2,200000.00,CURRENT_TIMESTAMP),
(3,120000.00,CURRENT_TIMESTAMP),
(4,45000.00,CURRENT_TIMESTAMP),
(5,95000.00,CURRENT_TIMESTAMP),
(6,80000.00,CURRENT_TIMESTAMP),
(7,50000.00,CURRENT_TIMESTAMP),
(8,30000.00,CURRENT_TIMESTAMP),
(9,8000.00,CURRENT_TIMESTAMP),
(10,12000.00,CURRENT_TIMESTAMP),
(11,250000.00,CURRENT_TIMESTAMP),
(12,5000.00,CURRENT_TIMESTAMP),
(13,90000.00,CURRENT_TIMESTAMP),
(14,7000.00,CURRENT_TIMESTAMP),
(15,15000.00,CURRENT_TIMESTAMP),
(16,45000.00,CURRENT_TIMESTAMP),
(17,6000.00,CURRENT_TIMESTAMP),
(18,22000.00,CURRENT_TIMESTAMP),
(19,18000.00,CURRENT_TIMESTAMP),
(20,10000.00,CURRENT_TIMESTAMP);

-- Sale details
INSERT INTO sale_details (sale_id, product_id, quantity, unit_price, subtotal) VALUES
(1,1,1,150000.00,150000.00),
(2,2,1,200000.00,200000.00),
(3,3,1,120000.00,120000.00),
(4,4,1,45000.00,45000.00),
(5,5,1,95000.00,95000.00),
(6,6,1,80000.00,80000.00),
(7,7,1,50000.00,50000.00),
(8,8,1,30000.00,30000.00),
(9,9,1,8000.00,8000.00),
(10,10,1,12000.00,12000.00),
(11,11,1,250000.00,250000.00),
(12,12,1,5000.00,5000.00),
(13,13,1,90000.00,90000.00),
(14,14,1,7000.00,7000.00),
(15,15,1,15000.00,15000.00),
(16,16,1,45000.00,45000.00),
(17,17,1,6000.00,6000.00),
(18,18,1,22000.00,22000.00),
(19,19,1,18000.00,18000.00),
(20,20,1,10000.00,10000.00);
