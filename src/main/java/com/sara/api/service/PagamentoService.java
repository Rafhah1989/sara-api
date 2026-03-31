package com.sara.api.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import com.sara.api.dto.PagamentoRequestDTO;
import com.sara.api.dto.PagamentoResponseDTO;
import com.sara.api.model.FormaPagamento;
import com.sara.api.model.Pagamento;
import com.sara.api.model.Pedido;
import com.sara.api.repository.FormaPagamentoRepository;
import com.sara.api.repository.PagamentoRepository;
import com.sara.api.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository repository;
    private final PedidoRepository pedidoRepository;
    private final FormaPagamentoRepository formaPagamentoRepository;

    public List<PagamentoResponseDTO> getPagamentosByPedido(Long pedidoId) {
        return repository.findByPedidoId(pedidoId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Transactional
    public void gerenciarPagamentos(Long pedidoId, List<PagamentoRequestDTO> dtos) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));

        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException("Ao menos um pagamento deve ser obrigatório.");
        }

        // Validação da soma
        BigDecimal soma = dtos.stream()
                .map(PagamentoRequestDTO::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (soma.compareTo(pedido.getValorTotal()) != 0) {
            throw new IllegalArgumentException("A soma dos pagamentos (" + soma + ") deve ser igual ao valor total do pedido (" + pedido.getValorTotal() + ").");
        }

        gerenciarPagamentos(pedido, dtos);
    }

    public void gerenciarPagamentos(Pedido pedido, List<PagamentoRequestDTO> dtos) {
        if (dtos == null) return;

        // 1. Identificar IDs recebidos no DTO (Garante Long para comparação segura)
        java.util.Set<Long> idsRecebidos = dtos.stream()
                .map(d -> d.getId() != null ? d.getId().longValue() : null)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        // 2. Remover da coleção do banco apenas o que NÃO veio no DTO
        java.util.Iterator<Pagamento> iterator = pedido.getPagamentos().iterator();
        while (iterator.hasNext()) {
            Pagamento p = iterator.next();
            if (p.getId() != null && !idsRecebidos.contains(p.getId().longValue())) {
                if (p.getMercadopagoPagamentoId() != null && !p.getMercadopagoPagamentoId().isEmpty()) {
                    try {
                        mercadoPagoService.cancelarPagamento(p.getMercadopagoPagamentoId());
                    } catch (Exception e) {
                        System.err.println("Erro ao cancelar no MP durante remoção: " + e.getMessage());
                    }
                }
                iterator.remove();
            }
        }
        // 3. Sincronizar (Atualizar existentes ou Adicionar novos)
        for (PagamentoRequestDTO dto : dtos) {
            System.out.println(">>> [SYNC] Processando Parcela DTO: " + (dto != null ? dto.getId() : "null") + " - Valor: " + (dto != null ? dto.getValor() : "0") + " - Pago: " + (dto != null ? dto.getPago() : "false"));

            Pagamento p;
            if (dto.getId() != null) {
                final Long buscarId = dto.getId().longValue();
                p = pedido.getPagamentos().stream()
                        .filter(x -> x.getId() != null && x.getId().longValue() == buscarId)
                        .findFirst()
                        .orElse(null);
                
                if (p == null) {
                    p = repository.findById(buscarId).orElse(null);
                    if (p != null && p.getPedido().getId().longValue() == pedido.getId().longValue()) {
                        System.out.println(">>> [SYNC] Parcela RESGATADA do repositório: #" + buscarId);
                        pedido.getPagamentos().add(p);
                    } else {
                        System.out.println(">>> [SYNC] Parcela ID #" + buscarId + " não encontrada! Criando nova (Fallback).");
                        p = new Pagamento();
                        p.setPedido(pedido);
                        pedido.getPagamentos().add(p);
                    }
                } else {
                    System.out.println(">>> [SYNC] Parcela mantida/atualizada: #" + buscarId);
                }

                // Já está na coleção (ou foi re-adicionado), verifica se mudou valor ou forma para limpar MP
                boolean valorAlterado = p.getValor() != null && p.getValor().compareTo(dto.getValor()) != 0;
                boolean formaAlterada = p.getFormaPagamento() == null || 
                                       dto.getFormaPagamentoId() == null ||
                                       p.getFormaPagamento().getId().longValue() != dto.getFormaPagamentoId().longValue();

                if (p.getMercadopagoPagamentoId() != null && (valorAlterado || formaAlterada)) {
                    System.out.println(">>> [SYNC] Forma/Valor alterado. Cancelando MP: " + p.getMercadopagoPagamentoId());
                    try {
                        mercadoPagoService.cancelarPagamento(p.getMercadopagoPagamentoId());
                    } catch (Exception e) {
                        System.err.println("Erro ao cancelar no MP durante alteração: " + e.getMessage());
                    }
                    p.setMercadopagoPagamentoId(null);
                    p.setBoletoPdfUrl(null);
                    p.setBoletoLinhaDigitavel(null);
                    p.setBoletoCodigoBarras(null);
                    p.setPixCopiaECola(null);
                    p.setPixQrCode(null);
                }
            } else {
                System.out.println(">>> [SYNC] Criando nova parcela (sem ID).");
                p = new Pagamento();
                p.setPedido(pedido);
                pedido.getPagamentos().add(p);
            }

            p.setValor(dto.getValor());
            p.setDataVencimento(dto.getDataVencimento());
            p.setPago(Boolean.TRUE.equals(dto.getPago()));
            
            FormaPagamento fp = formaPagamentoRepository.findById(dto.getFormaPagamentoId())
                    .orElseThrow(() -> new EntityNotFoundException("Forma de pagamento não encontrada: " + dto.getFormaPagamentoId()));
            p.setFormaPagamento(fp);
            ajustarVencimentoBoleto(p);
            p.setPagamentoOnline(Boolean.TRUE.equals(dto.getPagamentoOnline()));
        }

        // Consolida status final sem fazer múltiplos saves
        boolean todosPagos = !pedido.getPagamentos().isEmpty() && 
                             pedido.getPagamentos().stream().allMatch(x -> Boolean.TRUE.equals(x.getPago()));
        pedido.setPago(todosPagos);
        
        pedidoRepository.save(pedido);
        System.out.println(">>> [SYNC] Pedido #" + pedido.getId() + " processado com sucesso.");
    }

    @Transactional
    public List<PagamentoResponseDTO> adicionarPagamentoERecalcular(Long pedidoId, PagamentoRequestDTO novoDto) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));

        Pagamento novo = new Pagamento();
        novo.setValor(BigDecimal.ZERO); // Temporário
        novo.setDataVencimento(novoDto.getDataVencimento());
        novo.setPago(false);
        FormaPagamento fp = formaPagamentoRepository.findById(novoDto.getFormaPagamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Forma de pagamento não encontrada"));
        novo.setFormaPagamento(fp);
        
        ajustarVencimentoBoleto(novo);
        pedido.getPagamentos().add(novo);
        
        recalcularPagamentosNaoPagos(pedido);
        
        Pedido saved = pedidoRepository.save(pedido);
        return saved.getPagamentos().stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<PagamentoResponseDTO> excluirPagamentoERecalcular(Long pedidoId, Long pagamentoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));

        if (pedido.getPagamentos().size() <= 1) {
            throw new IllegalArgumentException("O pedido deve ter ao menos um pagamento.");
        }

        java.util.Optional<Pagamento> removendo = pedido.getPagamentos().stream()
                .filter(p -> p.getId() != null && p.getId().equals(pagamentoId))
                .findFirst();

        if (removendo.isPresent()) {
            Pagamento r = removendo.get();
            if (r.getMercadopagoPagamentoId() != null && !r.getMercadopagoPagamentoId().isEmpty()) {
                mercadoPagoService.cancelarPagamento(r.getMercadopagoPagamentoId());
            }
            pedido.getPagamentos().remove(r);
        }
        
        recalcularPagamentosNaoPagos(pedido);
        
        Pedido saved = pedidoRepository.save(pedido);
        return saved.getPagamentos().stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    private void recalcularPagamentosNaoPagos(Pedido pedido) {
        List<Pagamento> todos = pedido.getPagamentos();
        List<Pagamento> naoPagosIniciais = todos.stream().filter(p -> !Boolean.TRUE.equals(p.getPago())).collect(Collectors.toList());
        
        final List<Pagamento> naoPagos = naoPagosIniciais.isEmpty() ? todos : naoPagosIniciais;

        BigDecimal totalJaPago = todos.stream()
                .filter(p -> !naoPagos.contains(p))
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal restante = pedido.getValorTotal().subtract(totalJaPago);
        
        if (naoPagos.size() > 0) {
            BigDecimal valorParcela = restante.divide(BigDecimal.valueOf(naoPagos.size()), 2, RoundingMode.HALF_UP);
            
            for (int i = 0; i < naoPagos.size(); i++) {
                Pagamento p = naoPagos.get(i);
                BigDecimal novoValor;
                if (i == naoPagos.size() - 1) {
                    BigDecimal somaOutras = valorParcela.multiply(BigDecimal.valueOf(naoPagos.size() - 1));
                    novoValor = restante.subtract(somaOutras);
                } else {
                    novoValor = valorParcela;
                }

                // Se o valor mudou e já tinha integração com Mercado Pago, cancela a antiga
                if (p.getMercadopagoPagamentoId() != null && p.getValor() != null && p.getValor().compareTo(novoValor) != 0) {
                    mercadoPagoService.cancelarPagamento(p.getMercadopagoPagamentoId());
                    p.setMercadopagoPagamentoId(null);
                    p.setBoletoPdfUrl(null);
                    p.setBoletoLinhaDigitavel(null);
                    p.setBoletoCodigoBarras(null);
                    p.setPixCopiaECola(null);
                    p.setPixQrCode(null);
                    p.setDataExpiracao(null);
                }

                p.setValor(novoValor);
            }
        }
    }

    public void atualizarStatusPagoPedido(Pedido pedido) {
        boolean todosPagos = !pedido.getPagamentos().isEmpty() && 
                            pedido.getPagamentos().stream().allMatch(p -> Boolean.TRUE.equals(p.getPago()));
        pedido.setPago(todosPagos);
        pedidoRepository.save(pedido);
    }

    private PagamentoResponseDTO convertToResponseDTO(Pagamento entity) {
        PagamentoResponseDTO dto = new PagamentoResponseDTO();
        dto.setId(entity.getId());
        dto.setFormaPagamentoId(entity.getFormaPagamento().getId());
        dto.setFormaPagamentoDescricao(entity.getFormaPagamento().getDescricao());
        dto.setDataVencimento(entity.getDataVencimento());
        dto.setPago(entity.getPago());
        dto.setValor(entity.getValor());
        return dto;
    }

    private void ajustarVencimentoBoleto(Pagamento p) {
        if (p.getFormaPagamento() != null && "BOLETO".equalsIgnoreCase(p.getFormaPagamento().getDescricao())) {
            LocalDate data = p.getDataVencimento();
            if (data != null) {
                if (data.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    p.setDataVencimento(data.plusDays(2));
                } else if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    p.setDataVencimento(data.plusDays(1));
                }
            }
        }
    }
}
