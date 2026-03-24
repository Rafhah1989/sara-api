package com.sara.api.controller;

import com.sara.api.dto.PagamentoRequestDTO;
import com.sara.api.dto.PagamentoResponseDTO;
import com.sara.api.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
@Tag(name = "Pagamentos", description = "Gerenciamento de parcelas e pagamentos de pedidos")
public class PagamentoController {

    private final PagamentoService service;

    @GetMapping("/pedido/{id}")
    @Operation(summary = "Lista pagamentos de um pedido", description = "Retorna todas as parcelas/pagamentos vinculados a um pedido.")
    public ResponseEntity<List<PagamentoResponseDTO>> findByPedido(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPagamentosByPedido(id));
    }

    @PostMapping("/pedido/{id}")
    @Operation(summary = "Gerencia pagamentos de um pedido (ADMIN)", description = "Substitui a lista de pagamentos do pedido. A soma deve bater com o total.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> gerenciar(@PathVariable Long id, @RequestBody List<PagamentoRequestDTO> request) {
        service.gerenciarPagamentos(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pedido/{id}/adicionar")
    @Operation(summary = "Adiciona novo pagamento e recalcula (ADMIN)", description = "Adiciona uma nova parcela zerada e redistribui o saldo devedor entre as não pagas.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagamentoResponseDTO>> adicionar(@PathVariable Long id, @RequestBody PagamentoRequestDTO request) {
        return ResponseEntity.ok(service.adicionarPagamentoERecalcular(id, request));
    }

    @DeleteMapping("/{id}/pedido/{pedidoId}")
    @Operation(summary = "Exclui pagamento e recalcula (ADMIN)", description = "Remove uma parcela e redistribui seu valor entre as parcelas restantes.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagamentoResponseDTO>> excluir(@PathVariable Long id, @PathVariable Long pedidoId) {
        return ResponseEntity.ok(service.excluirPagamentoERecalcular(pedidoId, id));
    }
}
