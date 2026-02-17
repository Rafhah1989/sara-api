ALTER TABLE pedido ADD COLUMN data_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE produto ADD COLUMN codigo BIGINT;

-- Sincronizar códigos com IDs para registros existentes
UPDATE produto SET codigo = id WHERE codigo IS NULL;
