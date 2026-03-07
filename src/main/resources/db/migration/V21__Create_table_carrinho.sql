CREATE TABLE carrinho (
    id_usuario BIGINT NOT NULL,
    id_produto BIGINT NOT NULL,
    PRIMARY KEY (id_usuario, id_produto),
    CONSTRAINT fk_carrinho_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id),
    CONSTRAINT fk_carrinho_produto FOREIGN KEY (id_produto) REFERENCES produto (id)
);
