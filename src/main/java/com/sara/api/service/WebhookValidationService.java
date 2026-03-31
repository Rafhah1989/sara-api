package com.sara.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;

@Service
public class WebhookValidationService {

    @Value("${MP_WEBHOOK_SECRET:}")
    private String secretKey;

    /**
     * Valida se a assinatura enviada pelo Mercado Pago é autêntica (V2).
     * 
     * @param xSignature O cabeçalho X-Signature (ex: v1=hash,ts=timestamp)
     * @param requestId O cabeçalho X-Request-Id
     * @param dataId O ID do recurso (pagamento/recurso) que vem no payload (data.id)
     * @return true se a assinatura for válida
     */
    public boolean isSignatureValid(String xSignature, String requestId, String dataId) {
        if (xSignature == null || requestId == null || dataId == null || secretKey.isEmpty()) {
            return false;
        }

        try {
            // 1. Extrair ts e v1 do header x-signature
            String ts = "";
            String v1 = "";
            String[] parts = xSignature.split(",");
            for (String part : parts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    if (keyValue[0].trim().equals("ts")) ts = keyValue[1].trim();
                    if (keyValue[0].trim().equals("v1")) v1 = keyValue[1].trim();
                }
            }

            if (ts.isEmpty() || v1.isEmpty()) {
                return false;
            }

            // 2. Construir o manifest (formato oficial MP V2)
            // IMPORTANTE: id:[resource_id];request-id:[x-request-id];ts:[ts];
            String manifest = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts + ";";

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hashBytes = sha256_HMAC.doFinal(manifest.getBytes());
            String calculatedHash = HexFormat.of().formatHex(hashBytes);
            
            return calculatedHash.equals(v1);
        } catch (Exception e) {
            System.err.println("Erro na validação de assinatura: " + e.getMessage());
            return false;
        }
    }

}
