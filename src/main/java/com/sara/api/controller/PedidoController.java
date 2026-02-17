package com.sara.api.controller;

import com.sara.api.dto.PedidoRequestDTO;
import com.sara.api.dto.PedidoResponseDTO;
import com.sara.api.model.Pedido;
import com.sara.api.service.PedidoPdfService;
import com.sara.api.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos e vendas")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoPdfService pedidoPdfService;

    @GetMapping("/{id}")
    @Operation(summary = "Busca pedido por ID", description = "Retorna os detalhes do pedido e seus produtos")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(pedidoService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar pedidos com filtros e paginação", description = "Retorna uma página de pedidos filtrada por cliente, ID ou período")
    public ResponseEntity<Page<PedidoResponseDTO>> listar(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "clienteNome", required = false) String clienteNome,
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(value = "exibirCancelados", required = false, defaultValue = "false") Boolean exibirCancelados,
            Pageable pageable) {

        return ResponseEntity
                .ok(pedidoService.findAll(id, clienteNome, dataInicio, dataFim, exibirCancelados, pageable));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Busca pedidos por usuário", description = "Retorna a lista de todos os pedidos de um determinado usuário")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorUsuario(@PathVariable("usuarioId") Long usuarioId) {
        return ResponseEntity.ok(pedidoService.findByUsuario(usuarioId));
    }

    @GetMapping("/sugestao-frete/{usuarioId}")
    @Operation(summary = "Sugerir valor de frete", description = "Retorna o valor de frete padrão baseado no setor/tabela do usuário")
    public ResponseEntity<BigDecimal> sugerirFrete(@PathVariable("usuarioId") Long usuarioId) {
        return ResponseEntity.ok(pedidoService.sugerirFrete(usuarioId));
    }

    @PostMapping
    @Operation(summary = "Inclui um novo pedido", description = "Cria um novo pedido com a lista de produtos relacionada")
    public ResponseEntity<PedidoResponseDTO> incluir(@RequestBody PedidoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.save(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Altera um pedido existente", description = "Atualiza os dados do pedido e sincroniza a lista de produtos")
    public ResponseEntity<PedidoResponseDTO> alterar(@PathVariable("id") Long id,
            @RequestBody PedidoRequestDTO request) {
        return ResponseEntity.ok(pedidoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela um pedido por ID", description = "Realiza a exclusão lógica do pedido (seta cancelado = true)")
    public ResponseEntity<Void> cancelar(@PathVariable("id") Long id) {
        pedidoService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Gera PDF do pedido", description = "Gera e retorna um arquivo PDF formatado com os dados do pedido")
    public ResponseEntity<byte[]> gerarPdf(@PathVariable("id") Long id) {
        Pedido pedido = pedidoService.getPedidoEntity(id);
        byte[] pdfBytes = pedidoPdfService.generatePedidoPdf(pedido);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "pedido_" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
