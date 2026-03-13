package com.sara.api.controller;

import com.sara.api.exception.ValidationException;
import com.sara.api.model.Pedido;
import com.sara.api.repository.PedidoRepository;
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
    private final PedidoRepository pedidoRepository;

    @PostMapping("/webhook")
    @Operation(summary = "Webhook do Mercado Pago", description = "Recebe notificações de alteração de status de pagamento (JSON ou Form)")
    public ResponseEntity<Void> webhook(
            @RequestBody(required = false) String body,
            @RequestParam Map<String, String> allParams) {
        
        System.out.println("Webhook entry! Params: " + allParams);
        
        // Dispara o processamento assíncrono para liberar o MP imediatamente
        mercadoPagoService.processarNotificacaoAsync(body, allParams);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/verificar-pagamento/{idPedido}")
    @Operation(summary = "Verificação manual de pagamento", description = "Consulta o status do pagamento no Mercado Pago e atualiza o pedido")
    public ResponseEntity<Map<String, Object>> verificarManual(@PathVariable Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ValidationException("Pedido não encontrado", HttpStatus.NOT_FOUND));

        if (pedido.getMercadopagoPagamentoId() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Pedido não possui ID de pagamento associado");
            return ResponseEntity.badRequest().body(error);
        }

        String status = mercadoPagoService.verificarStatusPagamento(pedido.getMercadopagoPagamentoId());
        
        // Recarrega para ver o status atual
        pedido = pedidoRepository.findById(idPedido).get();
        
        Map<String, Object> response = new HashMap<>();
        response.put("idPedido", idPedido);
        response.put("status", status);
        response.put("pago", pedido.getPago());
        
        return ResponseEntity.ok(response);
    }
}
