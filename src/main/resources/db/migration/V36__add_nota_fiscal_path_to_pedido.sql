-- Adição do campo nota_fiscal_path na tabela pedido para armazenar o link/caminho do PDF no OCI
ALTER TABLE pedido ADD COLUMN nota_fiscal_path VARCHAR(255);
