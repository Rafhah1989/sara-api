package com.sara.api.service;

import com.sara.api.dto.*;
import com.sara.api.model.*;
import com.sara.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public TabelaFreteResponseDTO sugerirFrete(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + usuarioId));

        TabelaFrete tabela = null;

        // 1. Prioridade: Tabela de frete direta no usuário
        if (usuario.getTabelaFrete() != null && usuario.getTabelaFrete().getAtivo()) {
            tabela = usuario.getTabelaFrete();
        }
        // 2. Fallback: Primeira tabela ativa do setor do usuário
        else if (usuario.getSetor() != null && usuario.getSetor().getTabelasFrete() != null) {
            tabela = usuario.getSetor().getTabelasFrete().stream()
                    .filter(TabelaFrete::getAtivo)
                    .findFirst()
                    .orElse(null);
        }

        if (tabela != null) {
            TabelaFreteResponseDTO dto = new TabelaFreteResponseDTO();
            dto.setId(tabela.getId());
            dto.setDescricao(tabela.getDescricao());
            dto.setValor(tabela.getValor());
            dto.setAtivo(tabela.getAtivo());
            dto.setQuantidadeFaixa(tabela.getQuantidadeFaixa());
            dto.setValorFaixa(tabela.getValorFaixa());
            dto.setMinimoFaixa(tabela.getMinimoFaixa());
            return dto;
        }

        // Return empty/zero DTO if no table found
        TabelaFreteResponseDTO empty = new TabelaFreteResponseDTO();
        empty.setValor(BigDecimal.ZERO);
        return empty;
    }

    public List<PedidoResponseDTO> findByUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Page<PedidoResponseDTO> findAll(
            Long id,
            String clienteNome,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Boolean exibirCancelados,
            Pageable pageable) {

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.ASC, "cancelado")
                            .and(Sort.by(Sort.Direction.DESC, "dataPedido"))
                            .and(Sort.by(Sort.Direction.DESC, "id")));
        }

        Specification<Pedido> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (clienteNome != null && !clienteNome.isEmpty()) {
                predicates
                        .add(cb.like(cb.upper(root.get("usuario").get("nome")), "%" + clienteNome.toUpperCase() + "%"));
            }
            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataPedido"), dataInicio));
            }
            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataPedido"), dataFim));
            }

            if (Boolean.FALSE.equals(exibirCancelados)) {
                // Se não for para exibir cancelados, filtra onde cancelado é false ou nulo
                predicates.add(cb.or(cb.isFalse(root.get("cancelado")), cb.isNull(root.get("cancelado"))));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return pedidoRepository.findAll(spec, pageable)
                .map(this::convertToResponseDTO);
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
        if (produto.getPeso() != null) {
            item.setPeso(BigDecimal.valueOf(produto.getPeso()));
        } else {
            item.setPeso(BigDecimal.ZERO);
        }
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
        response.setDataPedido(pedido.getDataPedido());

        response.setProdutos(pedido.getProdutos().stream().map(item -> {
            PedidoProdutoResponseDTO itemDTO = new PedidoProdutoResponseDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProdutoId(item.getProduto().getId());
            itemDTO.setProdutoNome(item.getProduto().getNome());
            itemDTO.setProdutoCodigo(item.getProduto().getCodigo());
            itemDTO.setValor(item.getValor());
            itemDTO.setQuantidade(item.getQuantidade());
            itemDTO.setDesconto(item.getDesconto());
            itemDTO.setPeso(item.getPeso() != null ? item.getPeso().doubleValue() : 0.0);
            itemDTO.setImagem(item.getProduto().getImagem());
            itemDTO.setTamanho(item.getProduto().getTamanho());
            return itemDTO;
        }).collect(Collectors.toList()));

        return response;
    }
}
