ALTER TABLE produto ADD COLUMN tem_imagem BOOLEAN DEFAULT FALSE;
UPDATE produto SET tem_imagem = TRUE WHERE imagem IS NOT NULL AND imagem <> '';
