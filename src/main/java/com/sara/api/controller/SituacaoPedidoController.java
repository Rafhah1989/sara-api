package com.sara.api.controller;

import com.sara.api.model.SituacaoPedido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/situacoes-pedido")
@Tag(name = "Situações do Pedido", description = "Endpoint para listagem de situações de pedido")
public class SituacaoPedidoController {

    @GetMapping
    @Operation(summary = "Listar situações do pedido", description = "Retorna uma lista de chave/valor com os status de pedido")
    public List<Map<String, String>> listarSituacoes() {
        return Arrays.stream(SituacaoPedido.values())
                .map(situacao -> Map.of(
                        "codigo", situacao.name(),
                        "descricao", situacao.getDescricao()
                ))
                .collect(Collectors.toList());
    }
}
