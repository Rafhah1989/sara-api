CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    desconto DECIMAL(10,2),
    frete DECIMAL(10,2),
    valor_total DECIMAL(10,2) NOT NULL,
    observacao VARCHAR(255),
    cancelado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
