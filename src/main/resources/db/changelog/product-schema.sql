CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE order_product (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT pk_order_product PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_product_order FOREIGN KEY (order_id) REFERENCES "order" (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_product_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE INDEX IF NOT EXISTS idx_order_product_product_id ON order_product (product_id);
