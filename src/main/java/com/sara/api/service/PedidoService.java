package com.sara.api.service;

import com.sara.api.dto.*;
import com.sara.api.model.*;
import com.sara.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoResponseDTO findById(Long id) {
        return convertToResponseDTO(getPedidoEntity(id));
    }

    public Pedido getPedidoEntity(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
    }

    public List<PedidoResponseDTO> findByUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PedidoResponseDTO save(PedidoRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        updatePedidoFromDTO(pedido, request);

        // Map items
        if (request.getProdutos() != null) {
            for (PedidoProdutoRequestDTO itemDTO : request.getProdutos()) {
                PedidoProduto item = createPedidoProduto(itemDTO, pedido);
                pedido.getProdutos().add(item);
            }
        }

        Pedido saved = pedidoRepository.save(pedido);
        return convertToResponseDTO(saved);
    }

    @Transactional
    public PedidoResponseDTO update(Long id, PedidoRequestDTO request) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        updatePedidoFromDTO(pedido, request);

        // Sincronizar produtos (abordagem simples: limpar e reinserir, ou bater por ID)
        // Para seguir o padrão do usuário de "podendo ser enviada a lista de produtos
        // para serem alterados"
        pedido.getProdutos().clear();
        if (request.getProdutos() != null) {
            for (PedidoProdutoRequestDTO itemDTO : request.getProdutos()) {
                PedidoProduto item = createPedidoProduto(itemDTO, pedido);
                pedido.getProdutos().add(item);
            }
        }

        Pedido updated = pedidoRepository.save(pedido);
        return convertToResponseDTO(updated);
    }

    @Transactional
    public void cancel(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        pedido.setCancelado(true);
        pedidoRepository.save(pedido);
    }

    private void updatePedidoFromDTO(Pedido pedido, PedidoRequestDTO request) {
        pedido.setDesconto(request.getDesconto());
        pedido.setFrete(request.getFrete());
        pedido.setValorTotal(request.getValorTotal());
        pedido.setObservacao(request.getObservacao());
    }

    private PedidoProduto createPedidoProduto(PedidoProdutoRequestDTO dto, Pedido pedido) {
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + dto.getProdutoId()));

        PedidoProduto item = new PedidoProduto();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setValor(dto.getValor());
        item.setQuantidade(dto.getQuantidade());
        item.setDesconto(dto.getDesconto());
        item.setPeso(dto.getPeso());
        return item;
    }

    private PedidoResponseDTO convertToResponseDTO(Pedido pedido) {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(pedido.getId());
        response.setUsuarioId(pedido.getUsuario().getId());
        response.setUsuarioNome(pedido.getUsuario().getNome());
        response.setDesconto(pedido.getDesconto());
        response.setFrete(pedido.getFrete());
        response.setValorTotal(pedido.getValorTotal());
        response.setObservacao(pedido.getObservacao());
        response.setCancelado(pedido.getCancelado());

        response.setProdutos(pedido.getProdutos().stream().map(item -> {
            PedidoProdutoResponseDTO itemDTO = new PedidoProdutoResponseDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProdutoId(item.getProduto().getId());
            itemDTO.setProdutoNome(item.getProduto().getNome());
            itemDTO.setValor(item.getValor());
            itemDTO.setQuantidade(item.getQuantidade());
            itemDTO.setDesconto(item.getDesconto());
            itemDTO.setPeso(item.getPeso());
            return itemDTO;
        }).collect(Collectors.toList()));

        return response;
    }
}
