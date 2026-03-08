ALTER TABLE pedido ALTER COLUMN situacao SET DEFAULT 'PENDENTE';
UPDATE pedido SET situacao = 'PENDENTE' WHERE situacao = 'NOVO';
