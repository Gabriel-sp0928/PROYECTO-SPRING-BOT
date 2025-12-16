-- Ensure sales table exists (create minimal sales table used by application)
CREATE TABLE IF NOT EXISTS sales (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  total DECIMAL(19,2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sale_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Add sale_details table for persisting sale line items

CREATE TABLE IF NOT EXISTS sale_details (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sale_id BIGINT NOT NULL,
  product_id BIGINT,
  quantity INT DEFAULT 1,
  unit_price DECIMAL(19,2) DEFAULT 0,
  subtotal DECIMAL(19,2) DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT fk_sd_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
  CONSTRAINT fk_sd_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);
