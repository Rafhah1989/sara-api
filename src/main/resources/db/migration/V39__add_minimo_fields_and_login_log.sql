-- Adicionar campos de valor mínimo
ALTER TABLE forma_pagamento ADD COLUMN valor_minimo DECIMAL(19, 2) DEFAULT 0;
ALTER TABLE opcao_parcelamento ADD COLUMN valor_minimo_parcela DECIMAL(19, 2) DEFAULT 0;

-- Criar tabela de log de login
CREATE TABLE login_log (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT,
    data_hora TIMESTAMP NOT NULL,
    user_agent VARCHAR(500),
    status VARCHAR(20) NOT NULL, -- SUCESSO, FALHA
    CONSTRAINT fk_login_log_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);
