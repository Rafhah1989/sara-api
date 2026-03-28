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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final FormaPagamentoRepository formaPagamentoRepository;
    private final EmailService emailService;
    
    @Autowired
    @Lazy
    private MercadoPagoService mercadoPagoService;
    
    private final OciObjectStorageService ociService;
    private final PagamentoRepository pagamentoRepository;

    @Transactional(readOnly = true)
    public PedidoResponseDTO findById(Long id) {
        return convertToResponseDTO(findByIdWithDetails(id));
    }

    private Pedido findByIdWithDetails(Long id) {
        Pedido pedido = pedidoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
        // Inicializa a segunda bag (pagamentos) manualmente para evitar MultipleBagFetchException
        if (pedido.getPagamentos() != null) {
            pedido.getPagamentos().size();
        }
        return pedido;
    }

    public Pedido getPedidoEntity(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
    }

    public Pedido getPedidoEntityForPdf(Long id) {
        return pedidoRepository.findByIdWithProdutos(id)
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

    @Transactional(readOnly = true)
    public List<PedidoListResponseDTO> findByUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PedidoListResponseDTO> findAll(
            Long id,
            String clienteNome,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            SituacaoPedido situacao,
            Pageable pageable) {
        return findAll(id, clienteNome, dataInicio, dataFim, situacao, false, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PedidoListResponseDTO> findAll(
            Long id,
            String clienteNome,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            SituacaoPedido situacao,
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

            // Regra: CLIENTE vê apenas seus próprios pedidos
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
                Usuario user = (Usuario) authentication.getPrincipal();
                if (user.getRole() == Role.CLIENTE) {
                    predicates.add(cb.equal(root.get("usuario").get("id"), user.getId()));
                }
            }

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
            if (situacao != null) {
                predicates.add(cb.equal(root.get("situacao"), situacao));
            }

            if (Boolean.FALSE.equals(exibirCancelados)) {
                // Se não for para exibir cancelados, filtra onde cancelado é false ou nulo
                predicates.add(cb.or(cb.isFalse(root.get("cancelado")), cb.isNull(root.get("cancelado"))));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return pedidoRepository.findAll(spec, pageable)
                .map(this::convertToSummaryDTO);
    }

    @Transactional
    public PedidoResponseDTO save(PedidoRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        updatePedidoFromDTO(pedido, request);
        pedido.setSituacao(SituacaoPedido.PENDENTE);

        // Map items
        if (request.getProdutos() != null && !request.getProdutos().isEmpty()) {
            List<Long> produtoIds = request.getProdutos().stream()
                    .map(PedidoProdutoRequestDTO::getProdutoId)
                    .collect(Collectors.toList());
            
            var produtosMapa = produtoRepository.findAllById(produtoIds).stream()
                    .collect(Collectors.toMap(Produto::getId, p -> p));

            for (PedidoProdutoRequestDTO itemDTO : request.getProdutos()) {
                Produto produto = produtosMapa.get(itemDTO.getProdutoId());
                if (produto == null) {
                    throw new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId());
                }
                PedidoProduto item = new PedidoProduto();
                item.setPedido(pedido);
                item.setProduto(produto);
                item.setValor(itemDTO.getValor());
                item.setQuantidade(itemDTO.getQuantidade());
                item.setDesconto(itemDTO.getDesconto());
                item.setPeso(produto.getPeso() != null ? BigDecimal.valueOf(produto.getPeso()) : BigDecimal.ZERO);
                pedido.getProdutos().add(item);
            }
        }

        Pedido saved = pedidoRepository.save(pedido);

        // Garante que pelo menos um pagamento seja gerado se a lista estiver vazia
        if (saved.getPagamentos() == null || saved.getPagamentos().isEmpty()) {
            Pagamento unico = new Pagamento();
            unico.setPedido(saved);
            unico.setValor(saved.getValorTotal());
            unico.setDataVencimento(LocalDate.now());
            unico.setPago(false);
            unico.setPagamentoOnline(Boolean.TRUE.equals(request.getPagamentoOnline()));
            unico.setFormaPagamento(saved.getFormaPagamento());
            pagamentoRepository.save(unico);
            saved.getPagamentos().add(unico);
        }

        // Tenta gerar PIX para cada pagamento marcado como online
        for (Pagamento p : saved.getPagamentos()) {
            gerarPagamentoPixSeNecessario(p);
        }

        // Busca o pedido carregando todos os produtos e detalhes para evitar LazyInitializationException no e-mail assíncrono
        Pedido savedCompleto = findByIdWithAll(saved.getId());

        // Dispara e-mail assíncrono para o novo pedido
        emailService.enviarEmailNovoPedido(savedCompleto);
        
        return convertToResponseDTO(savedCompleto);
    }

    @Transactional
    public PedidoResponseDTO update(Long id, PedidoRequestDTO request) {
        Pedido pedido = findByIdWithDetails(id);

        updatePedidoFromDTO(pedido, request);

        // Sincronizar produtos (abordagem simples: limpar e reinserir, ou bater por ID)
        // Para seguir o padrão do usuário de "podendo ser enviada a lista de produtos
        // para serem alterados"
        pedido.getProdutos().clear();
        if (request.getProdutos() != null && !request.getProdutos().isEmpty()) {
            List<Long> produtoIds = request.getProdutos().stream()
                    .map(PedidoProdutoRequestDTO::getProdutoId)
                    .collect(Collectors.toList());
            
            var produtosMapa = produtoRepository.findAllById(produtoIds).stream()
                    .collect(Collectors.toMap(Produto::getId, p -> p));

            for (PedidoProdutoRequestDTO itemDTO : request.getProdutos()) {
                Produto produto = produtosMapa.get(itemDTO.getProdutoId());
                if (produto == null) {
                    throw new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId());
                }
                PedidoProduto item = new PedidoProduto();
                item.setPedido(pedido);
                item.setProduto(produto);
                item.setValor(itemDTO.getValor());
                item.setQuantidade(itemDTO.getQuantidade());
                item.setDesconto(itemDTO.getDesconto());
                item.setPeso(produto.getPeso() != null ? BigDecimal.valueOf(produto.getPeso()) : BigDecimal.ZERO);
                pedido.getProdutos().add(item);
            }
        }

        Pedido updated = pedidoRepository.saveAndFlush(pedido);
        
        // Tenta gerar PIX para cada pagamento marcado como online
        for (Pagamento p : updated.getPagamentos()) {
            gerarPagamentoPixSeNecessario(p);
        }

        if (Boolean.TRUE.equals(request.getNotificar())) {
            emailService.enviarEmailPedidoAtualizado(updated);
        }

        return convertToResponseDTO(updated);
    }

    @Transactional
    public void cancel(Long id, String motivo) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        pedido.setCancelado(true);
        
        if (motivo != null && !motivo.trim().isEmpty()) {
            String obsAtual = pedido.getObservacao() != null ? pedido.getObservacao() : "";
            String novaObs = obsAtual + (obsAtual.isEmpty() ? "" : "\n") + "[MOTIVO CANCELAMENTO]: " + motivo;
            pedido.setObservacao(novaObs);
        }
        
        pedidoRepository.save(pedido);

        // Busca o pedido carregando todos os produtos e detalhes para evitar LazyInitializationException no e-mail assíncrono
        Pedido pedidoCompleto = findByIdWithAll(id);

        // Captura o usuário autenticado que está realizando o cancelamento
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario responsavel) {
            emailService.enviarEmailPedidoCancelado(pedidoCompleto, responsavel, motivo);
        }
    }

    @Transactional
    public void confirmarPedido(Long id, boolean enviarEmail, boolean ajustarDatas) {
        Pedido pedido = getPedidoEntityWithPagamentos(id);
        pedido.setSituacao(SituacaoPedido.CONFIRMADO);
        
        if (ajustarDatas && pedido.getPagamentos() != null) {
            LocalDate hoje = LocalDate.now();
            // Calcula o gap entre a data do pedido e hoje para deslocar os vencimentos?
            // "considerando a data atual, e não mais a data do pedido"
            // Isso geralmente sugere que se o intervalo era 30 dias após o pedido, 
            // agora deve ser 30 dias após HOJE (a confirmação).
            
            long diasDiferenca = java.time.temporal.ChronoUnit.DAYS.between(pedido.getDataPedido().toLocalDate(), hoje);
            
            if (diasDiferenca > 0) {
                for (Pagamento p : pedido.getPagamentos()) {
                    if (p.getDataVencimento() != null) {
                        p.setDataVencimento(p.getDataVencimento().plusDays(diasDiferenca));
                    }
                }
            }
        }
        
        syncStatusPedido(pedido);
        pedidoRepository.save(pedido);
        
        if (enviarEmail) {
            emailService.enviarEmailPedidoConfirmado(pedido);
        }
    }

    @Transactional
    public void notificarConfirmacao(Long id) {
        Pedido pedido = findByIdWithDetails(id);
        emailService.enviarEmailPedidoConfirmado(pedido);
    }

    private Pedido findByIdWithAll(Long id) {
        return findByIdWithDetails(id);
    }

    private Pedido getPedidoEntityWithPagamentos(Long id) {
        return findByIdWithAll(id);
    }

    @Transactional
    public void alterarSituacao(Long id, SituacaoPedido novaSituacao) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        pedido.setSituacao(novaSituacao);
        syncStatusPedido(pedido);
        pedidoRepository.save(pedido);
    }

    private void updatePedidoFromDTO(Pedido pedido, PedidoRequestDTO request) {
        pedido.setDesconto(request.getDesconto());
        pedido.setFrete(request.getFrete());
        pedido.setValorTotal(request.getValorTotal());
        pedido.setObservacao(request.getObservacao());

        if (request.getSituacao() != null) {
            pedido.setSituacao(request.getSituacao());
        }


        if (request.getFormaPagamentoId() != null) {
            // Clientes não podem alterar a forma de pagamento de um pedido já existente
            if (pedido.getId() != null && pedido.getFormaPagamento() != null) {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof Usuario user) {
                    if (user.getRole() == Role.CLIENTE && !pedido.getFormaPagamento().getId().equals(request.getFormaPagamentoId())) {
                        throw new IllegalArgumentException("Clientes não podem alterar a forma de pagamento de um pedido já existente.");
                    }
                }
            }
            FormaPagamento fp = formaPagamentoRepository.findById(request.getFormaPagamentoId())
                    .orElseThrow(() -> new EntityNotFoundException("Forma de pagamento não encontrada"));
            pedido.setFormaPagamento(fp);
        }


        if (request.getPagamentos() != null) {
            // Limpa a lista atual para que o orphanRemoval do Hibernate cuide das exclusões
            pedido.getPagamentos().clear();
            
            for (PagamentoRequestDTO pDTO : request.getPagamentos()) {
                Pagamento p = new Pagamento();
                p.setValor(pDTO.getValor());
                p.setDataVencimento(pDTO.getDataVencimento());
                p.setPago(Boolean.TRUE.equals(pDTO.getPago()));
                p.setPagamentoOnline(Boolean.TRUE.equals(pDTO.getPagamentoOnline()));
                
                if (pDTO.getFormaPagamentoId() != null) {
                    p.setFormaPagamento(formaPagamentoRepository.getReferenceById(pDTO.getFormaPagamentoId()));
                } else {
                    p.setFormaPagamento(pedido.getFormaPagamento());
                }
                
                p.setPedido(pedido);
                pedido.getPagamentos().add(p);
            }
        }
        
        // Regra: Sempre deve haver ao menos um pagamento
        if (pedido.getPagamentos().isEmpty()) {
            Pagamento p = new Pagamento();
            p.setPedido(pedido);
            p.setValor(pedido.getValorTotal());
            p.setDataVencimento(LocalDate.now());
            p.setPago(false);
            p.setPagamentoOnline(Boolean.TRUE.equals(request.getPagamentoOnline()));
            p.setFormaPagamento(pedido.getFormaPagamento());
            pedido.getPagamentos().add(p);
        }

        syncStatusPedido(pedido);
    }

    public void syncStatusPedido(Pedido pedido) {
        if (pedido.getPagamentos() != null) {
            boolean todasPagas = pedido.getPagamentos().stream()
                    .allMatch(p -> Boolean.TRUE.equals(p.getPago()));
            pedido.setPago(todasPagas);
        }
    }


    private PedidoListResponseDTO convertToSummaryDTO(Pedido pedido) {
        PedidoListResponseDTO response = new PedidoListResponseDTO();
        response.setId(pedido.getId());
        response.setUsuarioId(pedido.getUsuario().getId());
        response.setUsuarioNome(pedido.getUsuario().getNome());
        response.setValorTotal(pedido.getValorTotal());
        response.setCancelado(pedido.getCancelado());
        response.setSituacao(pedido.getSituacao());
        if (pedido.getSituacao() != null) {
            response.setSituacaoDescricao(pedido.getSituacao().getDescricao());
        }
        response.setDataPedido(pedido.getDataPedido());
        response.setPago(pedido.getPago());
        
        // Indica se tem algum pagamento online pendente
        boolean temOnline = pedido.getPagamentos().stream()
                .anyMatch(p -> Boolean.TRUE.equals(p.getPagamentoOnline()));
        response.setPagamentoOnline(temOnline);
        
        response.setNotaFiscalPath(pedido.getNotaFiscalPath());
        response.setNumeroNotaFiscal(pedido.getNumeroNotaFiscal());
        response.setDataFaturamento(pedido.getDataFaturamento());
        
        response.setPagamentos(pedido.getPagamentos().stream().map(p -> {
            PagamentoResponseDTO pDTO = new PagamentoResponseDTO();
            pDTO.setId(p.getId());
            pDTO.setDataVencimento(p.getDataVencimento());
            pDTO.setPago(p.getPago());
            pDTO.setValor(p.getValor());
            pDTO.setPagamentoOnline(p.getPagamentoOnline());
            pDTO.setPixCopiaECola(p.getPixCopiaECola());
            pDTO.setPixQrCode(p.getPixQrCode());
            pDTO.setMercadopagoPagamentoId(p.getMercadopagoPagamentoId());
            pDTO.setDataExpiracaoPix(p.getDataExpiracaoPix());
            if (p.getFormaPagamento() != null) {
                pDTO.setFormaPagamentoId(p.getFormaPagamento().getId());
                pDTO.setFormaPagamentoDescricao(p.getFormaPagamento().getDescricao());
            }
            return pDTO;
        }).collect(java.util.stream.Collectors.toList()));

        return response;
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
        response.setSituacao(pedido.getSituacao());
        if (pedido.getSituacao() != null) {
            response.setSituacaoDescricao(pedido.getSituacao().getDescricao());
        }
        response.setDataPedido(pedido.getDataPedido());
        response.setPago(pedido.getPago());
        response.setNotaFiscalPath(pedido.getNotaFiscalPath());
        response.setNumeroNotaFiscal(pedido.getNumeroNotaFiscal());
        response.setDataFaturamento(pedido.getDataFaturamento());
        
        // Indica se tem algum pagamento online
        boolean temOnline = pedido.getPagamentos().stream()
                .anyMatch(p -> Boolean.TRUE.equals(p.getPagamentoOnline()));
        response.setPagamentoOnline(temOnline);
        
        if (pedido.getFormaPagamento() != null) {
            response.setFormaPagamentoId(pedido.getFormaPagamento().getId());
            response.setFormaPagamentoDescricao(pedido.getFormaPagamento().getDescricao());
        }

        if (pedido.getPagamentos() != null) {
            response.setPagamentos(pedido.getPagamentos().stream()
                .sorted((p1, p2) -> {
                    if (p1.getDataVencimento() == null && p2.getDataVencimento() == null) return p1.getId().compareTo(p2.getId());
                    if (p1.getDataVencimento() == null) return 1;
                    if (p2.getDataVencimento() == null) return -1;
                    int comp = p1.getDataVencimento().compareTo(p2.getDataVencimento());
                    return comp != 0 ? comp : p1.getId().compareTo(p2.getId());
                })
                .map(p -> {
                PagamentoResponseDTO pDTO = new PagamentoResponseDTO();
                pDTO.setId(p.getId());
                pDTO.setDataVencimento(p.getDataVencimento());
                pDTO.setValor(p.getValor());
                pDTO.setPago(p.getPago());
                pDTO.setPagamentoOnline(p.getPagamentoOnline());
                pDTO.setPixCopiaECola(p.getPixCopiaECola());
                pDTO.setPixQrCode(p.getPixQrCode());
                pDTO.setMercadopagoPagamentoId(p.getMercadopagoPagamentoId());
                pDTO.setDataExpiracaoPix(p.getDataExpiracaoPix());
                
                if (p.getFormaPagamento() != null) {
                    pDTO.setFormaPagamentoId(p.getFormaPagamento().getId());
                    pDTO.setFormaPagamentoDescricao(p.getFormaPagamento().getDescricao());
                }
                return pDTO;
            }).collect(Collectors.toList()));
        }

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
            itemDTO.setTemImagem(Boolean.TRUE.equals(item.getProduto().getTemImagem()));
            itemDTO.setTamanho(item.getProduto().getTamanho());
            return itemDTO;
        }).collect(Collectors.toList()));

        return response;
    }

    public void alterarStatusPago(Long id, Boolean pago) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        pedido.setPago(pago);
        syncStatusPedido(pedido);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoResponseDTO gerarPagamentoPixManual(Long id, Long pagamentoId) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        
        if (pagamentoId != null) {
            Pagamento p = pagamentoRepository.findById(pagamentoId)
                    .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
            if (!p.getPedido().getId().equals(id)) {
                throw new IllegalArgumentException("Pagamento não pertence ao pedido informado");
            }
            gerarPagamentoPixSeNecessario(p);
        } else {
            // Compatibilidade: Busca apenas a PRIMEIRA parcela PIX Online Pendente
            pedido.getPagamentos().stream()
                .filter(p -> Boolean.TRUE.equals(p.getPagamentoOnline()) && 
                             !Boolean.TRUE.equals(p.getPago()) &&
                             p.getFormaPagamento() != null && 
                             "PIX".equalsIgnoreCase(p.getFormaPagamento().getDescricao()))
                .findFirst()
                .ifPresent(this::gerarPagamentoPixSeNecessario);
        }
        return convertToResponseDTO(pedido);
    }

    private void gerarPagamentoPixSeNecessario(Pagamento pagamento) {
        Pedido pedido = pagamento.getPedido();
        Usuario usuario = pedido.getUsuario();
        
        // Se for PIX e estiver marcado para pagamento online, e o usuário estiver autorizado
        boolean podePagarOnline = usuario.getMetodoPagamentoAutorizado() == MetodoPagamentoAutorizado.ENTREGA_E_ONLINE ||
                                 usuario.getMetodoPagamentoAutorizado() == MetodoPagamentoAutorizado.APENAS_ONLINE;

        if (Boolean.TRUE.equals(pagamento.getPago())) {
            return; // Não regera se já estiver pago
        }

        if (Boolean.TRUE.equals(pagamento.getPagamentoOnline()) &&
            pagamento.getFormaPagamento() != null && "PIX".equalsIgnoreCase(pagamento.getFormaPagamento().getDescricao())) {
            

            // Para geração MANUAL (clique no botão), sempre geramos um novo se não estiver pago
            try {
                com.mercadopago.resources.payment.Payment mpPayment = mercadoPagoService.criarPagamentoPix(pagamento);
                pagamento.setMercadopagoPagamentoId(mpPayment.getId().toString());
                pagamento.setPago(false);
                    
                    if (mpPayment.getDateOfExpiration() != null) {
                        pagamento.setDataExpiracaoPix(mpPayment.getDateOfExpiration());
                    }
                    
                    if (mpPayment.getPointOfInteraction() != null && 
                        mpPayment.getPointOfInteraction().getTransactionData() != null) {
                        pagamento.setPixCopiaECola(mpPayment.getPointOfInteraction().getTransactionData().getQrCode());
                        pagamento.setPixQrCode(mpPayment.getPointOfInteraction().getTransactionData().getQrCodeBase64());
                    }
                    
                    pagamentoRepository.save(pagamento);
                } catch (Exception e) {
                    System.err.println("Erro ao criar pagamento Mercado Pago para parcela " + pagamento.getId() + ": " + e.getMessage());
                    throw new RuntimeException("Erro ao gerar PIX: " + e.getMessage());
                }
            }
        }

    @Transactional
    public void salvarNotaFiscal(Long id, String numeroNotaFiscal, MultipartFile file, boolean notificar) throws IOException {
        Pedido pedido = getPedidoEntity(id);
        
        if (file != null && !file.isEmpty()) {
            String fileName = String.format("nota_pedido_%d_cliente_%d.pdf", pedido.getId(), pedido.getUsuario().getId());
            ociService.uploadFile(fileName, file.getInputStream(), file.getSize(), file.getContentType());
            pedido.setNotaFiscalPath(fileName);
            pedido.setNumeroNotaFiscal(numeroNotaFiscal);
            pedido.setDataFaturamento(LocalDate.now());
        } else {
            // Se não enviou arquivo, apenas atualiza o número se estiver mudando
            pedido.setNumeroNotaFiscal(numeroNotaFiscal);
        }
        
        pedidoRepository.save(pedido);

        if (notificar) {
            emailService.enviarEmailNotaFiscalDisponivel(pedido);
        }
    }

    public void notificarNotaFiscal(Long id) {
        Pedido pedido = getPedidoEntity(id);
        if (pedido.getNotaFiscalPath() == null) {
            throw new EntityNotFoundException("Este pedido ainda não possui uma Nota Fiscal anexada.");
        }
        emailService.enviarEmailNotaFiscalDisponivel(pedido);
    }

    @Transactional
    public void excluirNotaFiscal(Long id) {
        Pedido pedido = getPedidoEntity(id);
        if (pedido.getNotaFiscalPath() != null) {
            ociService.deleteFile(pedido.getNotaFiscalPath());
            pedido.setNotaFiscalPath(null);
            pedidoRepository.save(pedido);
        }
    }

    public void notificarCobrancaPix(Long id, Long pagamentoId) {
        Pedido pedido = getPedidoEntity(id);
        com.sara.api.model.Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
        
        if (!pagamento.getPedido().getId().equals(id)) {
            throw new IllegalArgumentException("O pagamento não pertence a este pedido");
        }
        
        if (Boolean.TRUE.equals(pagamento.getPago())) {
            throw new IllegalStateException("Esta parcela já consta como paga");
        }
        
        emailService.enviarEmailCobrancaPix(pedido, pagamento);
    }

    public InputStreamResource downloadNotaFiscal(Long id) {
        Pedido pedido = getPedidoEntity(id);
        if (pedido.getNotaFiscalPath() == null) {
            throw new EntityNotFoundException("Nota fiscal não encontrada para este pedido");
        }
        
        java.io.InputStream is = ociService.downloadFile(pedido.getNotaFiscalPath());
        return new InputStreamResource(is);
    }
}
