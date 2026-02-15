CREATE TABLE produto (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tamanho INTEGER,
    ativo BOOLEAN DEFAULT TRUE,
    imagem TEXT
);
