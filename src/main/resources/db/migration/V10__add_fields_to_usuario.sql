ALTER TABLE usuario ADD COLUMN bairro VARCHAR(100) NOT NULL DEFAULT 'A designar';
ALTER TABLE usuario ADD COLUMN observacao VARCHAR(255);
ALTER TABLE usuario ADD COLUMN padre VARCHAR(100);
ALTER TABLE usuario ADD COLUMN secretario VARCHAR(100);
ALTER TABLE usuario ADD COLUMN tesoureiro VARCHAR(100);
ALTER TABLE usuario ADD COLUMN forma_pagamento VARCHAR(20);
ALTER TABLE usuario ADD COLUMN desconto DECIMAL(19, 2);
ALTER TABLE usuario ADD COLUMN modalidade_entrega VARCHAR(100);
ALTER TABLE usuario ADD COLUMN setor_id BIGINT;
ALTER TABLE usuario ADD COLUMN tabela_frete_id BIGINT;

ALTER TABLE usuario ADD CONSTRAINT fk_usuario_setor FOREIGN KEY (setor_id) REFERENCES setor (id);
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_tabela_frete FOREIGN KEY (tabela_frete_id) REFERENCES tabela_frete (id);
