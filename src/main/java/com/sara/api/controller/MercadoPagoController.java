package com.sara.api.controller;

import com.sara.api.exception.ValidationException;
import com.sara.api.model.Pagamento;
import com.sara.api.model.WebhookEvent;
import com.sara.api.repository.PagamentoRepository;
import com.sara.api.repository.WebhookEventRepository;
import com.sara.api.service.MercadoPagoService;
import com.sara.api.service.WebhookValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago")
@RequiredArgsConstructor
@Tag(name = "Mercado Pago", description = "Endpoints para integração com Mercado Pago")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;
    private final PagamentoRepository pagamentoRepository;
    private final WebhookValidationService webhookValidationService;
    private final WebhookEventRepository webhookEventRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook")
    @Operation(summary = "Webhook do Mercado Pago")
    public ResponseEntity<Void> webhook(
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestBody String body,
            @RequestParam Map<String, String> allParams) {
        
        System.out.println(">>> Webhook - ID: " + requestId);

        // 1. Controle de Idempotência
        if (requestId != null && webhookEventRepository.existsByRequestId(requestId)) {
            System.out.println(">>> [IDEMPOTENCIA] Requisição duplicada ignorada: " + requestId);
            return ResponseEntity.ok().build();
        }

        // 2. Extrair dataId do payload para validação da assinatura
        String dataId = null;
        try {
            Map<String, Object> tempPayload = objectMapper.readValue(body, Map.class);
            if (tempPayload.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) tempPayload.get("data");
                if (data.containsKey("id")) {
                    dataId = String.valueOf(data.get("id"));
                }
            }
        } catch (Exception e) {
            System.err.println(">>> Erro ao pré-parsear payload para extrair dataId: " + e.getMessage());
        }

        // 3. Validação de Assinatura (Manifest: id;request-id;ts;)
        if (!webhookValidationService.isSignatureValid(signature, requestId, dataId)) {
            System.err.println(">>> [SEGURANCA] Assinatura do webhook INVALIDA! ID: " + requestId + " DataID: " + dataId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 4. Registrar o ID para evitar reprocessamento
        if (requestId != null) {
            webhookEventRepository.save(new WebhookEvent(requestId));
        }

        try {
            // Reutiliza o payload para o processamento assíncrono
            Map<String, Object> payload = objectMapper.readValue(body, Map.class);
            mercadoPagoService.processarNotificacaoAsync(payload, allParams);
        } catch (Exception e) {
            System.err.println(">>> Erro ao parsear JSON definitivo do webhook: " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/verificar-pagamento/{idPagamento}")
    @Operation(summary = "Verificação manual de pagamento", description = "Consulta o status do pagamento no Mercado Pago e atualiza a parcela")
    public ResponseEntity<Map<String, Object>> verificarManual(@PathVariable Long idPagamento) {
        Pagamento pagamento = pagamentoRepository.findById(idPagamento)
                .orElseThrow(() -> new ValidationException("Pagamento não encontrado", HttpStatus.NOT_FOUND));

        if (pagamento.getMercadopagoPagamentoId() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Vencimento não possui ID de pagamento associado");
            return ResponseEntity.badRequest().body(error);
        }

        String status = mercadoPagoService.verificarStatusPagamento(pagamento.getMercadopagoPagamentoId());
        
        // Recarrega para ver o status atual
        pagamento = pagamentoRepository.findById(idPagamento).get();
        
        Map<String, Object> response = new HashMap<>();
        response.put("idPagamento", idPagamento);
        response.put("status", status);
        response.put("pago", pagamento.getPago());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{mpPaymentId}")
    @Operation(summary = "Verificar status no Mercado Pago (Teste)", description = "Consulta direta ao Mercado Pago pelo ID da transação (apenas para testes via Postman)")
    public ResponseEntity<Map<String, Object>> buscarStatusMp(@PathVariable String mpPaymentId) {
        try {
            var payment = mercadoPagoService.buscarPagamento(mpPaymentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mpId", payment.getId());
            response.put("status", payment.getStatus());
            response.put("statusDetail", payment.getStatusDetail());
            response.put("transactionAmount", payment.getTransactionAmount());
            response.put("dateApproved", payment.getDateApproved());
            response.put("paymentMethodId", payment.getPaymentMethodId());
            response.put("externalReference", payment.getExternalReference());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erro ao consultar Mercado Pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
