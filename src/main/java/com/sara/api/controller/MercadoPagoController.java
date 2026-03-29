package com.sara.api.controller;

import com.sara.api.exception.ValidationException;
import com.sara.api.model.Pagamento;
import com.sara.api.repository.PagamentoRepository;
import com.sara.api.service.MercadoPagoService;
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

    @PostMapping("/webhook")
    @Operation(summary = "Webhook do Mercado Pago")
    public ResponseEntity<Void> webhook(
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestParam Map<String, String> allParams) {
        
        System.out.println("--- Webhook Entry ---");
        // Processamento assíncrono
        mercadoPagoService.processarNotificacaoAsync(payload, allParams);

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
}
