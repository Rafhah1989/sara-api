ALTER TABLE pagamento ADD COLUMN pix_copia_e_cola TEXT;
ALTER TABLE pagamento ADD COLUMN pix_qr_code TEXT;
ALTER TABLE pagamento ADD COLUMN mercadopago_pagamento_id VARCHAR(100);
ALTER TABLE pagamento ADD COLUMN pagamento_online BOOLEAN DEFAULT FALSE;
ALTER TABLE pagamento ADD COLUMN data_expiracao_pix TIMESTAMP WITH TIME ZONE;

ALTER TABLE pedido DROP COLUMN pix_copia_e_cola;
ALTER TABLE pedido DROP COLUMN pix_qr_code;
ALTER TABLE pedido DROP COLUMN mercadopago_pagamento_id;
ALTER TABLE pedido DROP COLUMN pagamento_online;
ALTER TABLE pedido DROP COLUMN data_expiracao_pix;
