package com.sara.api.repository;

import com.sara.api.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    java.util.List<Pagamento> findByPedidoId(Long pedidoId);
    java.util.Optional<Pagamento> findByMercadopagoPagamentoId(String mercadopagoPagamentoId);
}
