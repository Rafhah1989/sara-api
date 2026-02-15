CREATE TABLE setor_tabela (
    setor_id BIGINT NOT NULL,
    tabela_frete_id BIGINT NOT NULL,
    PRIMARY KEY (setor_id, tabela_frete_id),
    CONSTRAINT fk_setor FOREIGN KEY (setor_id) REFERENCES setor (id),
    CONSTRAINT fk_tabela_frete FOREIGN KEY (tabela_frete_id) REFERENCES tabela_frete (id)
);
