ALTER TABLE usuario ADD COLUMN codigo VARCHAR(4);

-- Atualiza usuários existentes com o padrão: '11' + dois últimos dígitos do ID (com zero à esquerda)
UPDATE usuario SET codigo = '11' || LPAD(MOD(id, 100)::text, 2, '0');

-- Se houverem duplicatas (mais de 100 usuários), o comando abaixo falhará, 
-- o que é o comportamento esperado para garantir a unicidade solicitada.
ALTER TABLE usuario ALTER COLUMN codigo SET NOT NULL;
ALTER TABLE usuario ADD CONSTRAINT uk_usuario_codigo UNIQUE (codigo);
