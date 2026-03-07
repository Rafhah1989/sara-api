package com.sara.api.repository;

import com.sara.api.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {
    List<Produto> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);

    java.util.Optional<Produto> findByCodigoAndAtivoTrue(Long codigo);

    List<Produto> findByAtivoTrue();
}
