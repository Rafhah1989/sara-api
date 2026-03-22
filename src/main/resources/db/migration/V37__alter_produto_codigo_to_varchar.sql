-- V37__alter_produto_codigo_to_varchar.sql
-- Alteração do código do produto de numérico para alfanumérico (VARCHAR)

ALTER TABLE produto ALTER COLUMN codigo TYPE VARCHAR(6);
