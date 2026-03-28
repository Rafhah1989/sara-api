-- Adição dos campos numero_nota_fiscal e data_faturamento na tabela pedido
ALTER TABLE pedido ADD COLUMN numero_nota_fiscal VARCHAR(255);
ALTER TABLE pedido ADD COLUMN data_faturamento DATE;
