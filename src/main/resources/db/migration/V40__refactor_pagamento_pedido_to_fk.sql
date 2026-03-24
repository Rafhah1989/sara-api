-- Adiciona a coluna pedido_id na tabela pagamento
ALTER TABLE pagamento ADD COLUMN pedido_id BIGINT;

-- Migra os dados da tabela de relação para a nova coluna
UPDATE pagamento p
SET pedido_id = pp.pedido_id
FROM pagamento_pedido pp
WHERE p.id = pp.pagamento_id;

-- Adiciona a constraint de chave estrangeira
ALTER TABLE pagamento ADD CONSTRAINT fk_pagamento_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id);

-- Remove a tabela de relacionamento antiga
DROP TABLE pagamento_pedido;
