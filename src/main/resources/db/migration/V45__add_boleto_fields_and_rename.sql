-- Renomear coluna de expiração de PIX para uso genérico (Boleto/Pix)
ALTER TABLE pagamento RENAME COLUMN data_expiracao_pix TO data_expiracao;

-- Adicionar campos específicos para boletos bancários
ALTER TABLE pagamento ADD COLUMN boleto_pdf_url TEXT;
ALTER TABLE pagamento ADD COLUMN boleto_linha_digitavel TEXT;
ALTER TABLE pagamento ADD COLUMN boleto_codigo_barras TEXT;
