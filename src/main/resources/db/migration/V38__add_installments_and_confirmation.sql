-- Adicionar permissão de parcelamento ao usuário
ALTER TABLE usuario ADD COLUMN permitir_parcelamento BOOLEAN DEFAULT FALSE NOT NULL;

-- Tabela pagamento (sem pedido_id inicial para seguir a instrução da tabela de relação)
CREATE TABLE pagamento (
    id BIGSERIAL PRIMARY KEY,
    forma_pagamento_id BIGINT NOT NULL,
    data_vencimento DATE,
    pago BOOLEAN NOT NULL DEFAULT FALSE,
    valor DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_pagamento_forma_pagamento FOREIGN KEY (forma_pagamento_id) REFERENCES forma_pagamento(id)
);

-- Tabela pagamento_pedido para relacionar Pagamento com Pedido
CREATE TABLE pagamento_pedido (
    pagamento_id BIGINT NOT NULL,
    pedido_id BIGINT NOT NULL,
    PRIMARY KEY (pagamento_id, pedido_id),
    CONSTRAINT fk_pag_ped_pagamento FOREIGN KEY (pagamento_id) REFERENCES pagamento(id),
    CONSTRAINT fk_pag_ped_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id)
);

-- Tabela opcoes_parcelamento
CREATE TABLE opcao_parcelamento (
    id BIGSERIAL PRIMARY KEY,
    forma_pagamento_id BIGINT NOT NULL,
    qtd_max_parcelas INT NOT NULL,
    dias_vencimento_intervalo INT NOT NULL DEFAULT 30,
    CONSTRAINT fk_opcao_parcelamento_forma_pagamento FOREIGN KEY (forma_pagamento_id) REFERENCES forma_pagamento(id)
);

-- Tabela de relacionamento entre Usuário e Opções de Parcelamento
CREATE TABLE usuario_opcao_parcelamento (
    usuario_id BIGINT NOT NULL,
    opcao_parcelamento_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, opcao_parcelamento_id),
    CONSTRAINT fk_user_opt_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_user_opt_opcao FOREIGN KEY (opcao_parcelamento_id) REFERENCES opcao_parcelamento(id)
);
