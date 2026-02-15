package com.sara.api.repository;

import com.sara.api.model.TabelaFrete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TabelaFreteRepository extends JpaRepository<TabelaFrete, Long> {

    List<TabelaFrete> findByDescricaoContainingIgnoreCase(String descricao);

}
