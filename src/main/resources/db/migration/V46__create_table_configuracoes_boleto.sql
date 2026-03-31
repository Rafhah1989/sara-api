CREATE TABLE configuracoes_boleto (
    id BIGINT PRIMARY KEY,
    multa_tipo VARCHAR(20) NOT NULL DEFAULT 'percentage',
    multa_valor DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    juros_tipo VARCHAR(20) NOT NULL DEFAULT 'percentage',
    juros_valor DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    desconto_tipo VARCHAR(20) NOT NULL DEFAULT 'percentage',
    desconto_valor DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    desconto_dias_antecedencia INTEGER NOT NULL DEFAULT 0
);

-- Insere a configuração padrão única (Singleton)
INSERT INTO configuracoes_boleto (id, multa_tipo, multa_valor, juros_tipo, juros_valor, desconto_tipo, desconto_valor, desconto_dias_antecedencia)
VALUES (1, 'percentage', 2.00, 'percentage', 1.00, 'percentage', 0.00, 0);
