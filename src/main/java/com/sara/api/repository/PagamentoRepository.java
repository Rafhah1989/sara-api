package com.sara.api.repository;

import com.sara.api.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByPedidoId(Long pedidoId);
    java.util.Optional<Pagamento> findByMercadopagoPagamentoId(String mercadopagoPagamentoId);
    
    List<Pagamento> findAllByPagoFalseAndPagamentoOnlineTrueAndMercadopagoPagamentoIdIsNotNullAndDataExpiracaoAfter(OffsetDateTime now);
}
