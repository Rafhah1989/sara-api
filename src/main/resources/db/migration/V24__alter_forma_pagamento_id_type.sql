ALTER TABLE forma_pagamento ALTER COLUMN id TYPE BIGINT;
ALTER TABLE usuario ALTER COLUMN forma_pagamento_id TYPE BIGINT;
ALTER TABLE pedido ALTER COLUMN forma_pagamento_id TYPE BIGINT;
