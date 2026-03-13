ALTER TABLE pedido ADD COLUMN pago BOOLEAN DEFAULT FALSE;
ALTER TABLE pedido ADD COLUMN pix_copia_e_cola TEXT;
ALTER TABLE pedido ADD COLUMN pix_qr_code TEXT;
ALTER TABLE pedido ADD COLUMN mercadopago_pagamento_id VARCHAR(100);
