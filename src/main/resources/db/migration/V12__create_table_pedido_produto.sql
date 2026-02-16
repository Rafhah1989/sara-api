CREATE TABLE pedido_produto (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    desconto DECIMAL(10,2),
    peso DECIMAL(10,2),
    CONSTRAINT fk_pp_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_pp_produto FOREIGN KEY (produto_id) REFERENCES produto (id)
);
