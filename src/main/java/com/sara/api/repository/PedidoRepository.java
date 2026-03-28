package com.sara.api.repository;

import com.sara.api.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {
    
    @Override
    @EntityGraph(attributePaths = {"usuario", "formaPagamento"})
    Page<Pedido> findAll(Specification<Pedido> spec, Pageable pageable);
    @EntityGraph(attributePaths = {"usuario", "formaPagamento"})
    List<Pedido> findByUsuarioId(Long usuarioId);

    @Query("SELECT p FROM Pedido p " +
            "LEFT JOIN FETCH p.produtos pp " +
            "LEFT JOIN FETCH pp.produto " +
            "WHERE p.id = :id")
    java.util.Optional<Pedido> findByIdWithProdutos(@Param("id") Long id);

    @EntityGraph(attributePaths = {"usuario", "formaPagamento", "produtos", "produtos.produto"})
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    java.util.Optional<Pedido> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p FROM Pedido p WHERE " +
            "(:id IS NULL OR p.id = :id) AND " +
            "(:clienteNome IS NULL OR UPPER(p.usuario.nome) LIKE :clienteNome) AND " +
            "(:dataInicio IS NULL OR p.dataPedido >= :dataInicio) AND " +
            "(:dataFim IS NULL OR p.dataPedido <= :dataFim)")
    Page<Pedido> findWithFilters(
            @Param("id") Long id,
            @Param("clienteNome") String clienteNome,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);
}
