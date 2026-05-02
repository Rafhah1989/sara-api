-- Migration to add 'numero' field to 'pedido' table
ALTER TABLE pedido ADD COLUMN numero VARCHAR(7);

-- For existing records, use the ID as the numero
UPDATE pedido SET numero = CAST(id AS VARCHAR);

-- Make it mandatory and unique
ALTER TABLE pedido ALTER COLUMN numero SET NOT NULL;
ALTER TABLE pedido ADD CONSTRAINT uk_pedido_numero UNIQUE (numero);
