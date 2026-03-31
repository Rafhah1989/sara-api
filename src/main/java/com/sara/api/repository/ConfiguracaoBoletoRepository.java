package com.sara.api.repository;

import com.sara.api.model.ConfiguracaoBoleto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracaoBoletoRepository extends JpaRepository<ConfiguracaoBoleto, Long> {
}
