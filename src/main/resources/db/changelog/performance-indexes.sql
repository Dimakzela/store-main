CREATE INDEX IF NOT EXISTS idx_order_customer_id ON "order" (customer_id);

-- 2. Customer name substring search.
--
-- CustomerRepository.searchByNameSubstring issues:
--     LOWER(name) LIKE LOWER('%query%')
--
-- A B-tree cannot serve a leading-wildcard LIKE at all, so a plain index on
-- name would never be used. A trigram GIN index does support infix matching,
-- and is built over the LOWER(name) expression so it matches the query's
-- expression exactly.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_customer_name_lower_trgm ON customer USING GIN (LOWER(name) gin_trgm_ops);
