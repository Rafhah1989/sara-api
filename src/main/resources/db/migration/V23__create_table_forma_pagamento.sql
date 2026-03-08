CREATE TABLE forma_pagamento (
    id SERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL,
    desconto DOUBLE PRECISION,
    icone_font_awesome VARCHAR(100)
);

INSERT INTO forma_pagamento (descricao, desconto, icone_font_awesome) VALUES
('PIX', 5, 'fa-brands fa-pix'),
('Dinheiro', 5, 'fa-solid fa-money-bill'),
('Boleto', 0, 'fa-solid fa-ticket-simple');

ALTER TABLE usuario ADD COLUMN forma_pagamento_id INTEGER;
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_forma_pagamento FOREIGN KEY (forma_pagamento_id) REFERENCES forma_pagamento (id);

ALTER TABLE pedido ADD COLUMN forma_pagamento_id INTEGER;
ALTER TABLE pedido ADD CONSTRAINT fk_pedido_forma_pagamento FOREIGN KEY (forma_pagamento_id) REFERENCES forma_pagamento (id);
