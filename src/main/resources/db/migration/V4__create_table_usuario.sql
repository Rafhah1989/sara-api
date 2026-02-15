CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cep VARCHAR(8) NOT NULL,
    endereco VARCHAR(255),
    numero VARCHAR(20),
    cpf_cnpj VARCHAR(14) NOT NULL,
    telefone VARCHAR(15),
    role VARCHAR(20) NOT NULL,
    senha VARCHAR(32) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);
