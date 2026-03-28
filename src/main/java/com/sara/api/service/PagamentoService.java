package com.sara.api.service;

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

        // Atualizar lista de pagamentos do pedido
        List<Pagamento> atuais = pedido.getPagamentos();
        atuais.clear();

        for (PagamentoRequestDTO dto : dtos) {
            Pagamento p = new Pagamento();
            if (dto.getId() != null) {
                // Se o ID existia nas tabelas originais, poderíamos reusar, 
                // mas como estamos em OneToMany com JoinTable e clear(), 
                // vamos criar novos ou carregar se necessário. 
                // Simplificando: criar novos conforme a lista enviada.
            }
            p.setValor(dto.getValor());
            p.setDataVencimento(dto.getDataVencimento());
            p.setPago(dto.getPago() != null ? dto.getPago() : false);
            
            FormaPagamento fp = formaPagamentoRepository.findById(dto.getFormaPagamentoId())
                    .orElseThrow(() -> new EntityNotFoundException("Forma de pagamento não encontrada: " + dto.getFormaPagamentoId()));
            p.setFormaPagamento(fp);
            
            atuais.add(p);
        }

        pedidoRepository.save(pedido);
        atualizarStatusPagoPedido(pedido);
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

        pedido.getPagamentos().removeIf(p -> p.getId() != null && p.getId().equals(pagamentoId));
        
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
                if (i == naoPagos.size() - 1) {
                    // Ajuste da última parcela para bater o total exato
                    BigDecimal somaOutras = valorParcela.multiply(BigDecimal.valueOf(naoPagos.size() - 1));
                    naoPagos.get(i).setValor(restante.subtract(somaOutras));
                } else {
                    naoPagos.get(i).setValor(valorParcela);
                }
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
}
