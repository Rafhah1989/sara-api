package com.sara.api.repository;

import com.sara.api.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {
    List<Produto> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);

    List<Produto> findByNomeAndAtivoTrueAndIdNot(String nome, Long id);

    java.util.Optional<Produto> findByCodigoAndAtivoTrue(Long codigo);

    List<Produto> findByAtivoTrue();
}
