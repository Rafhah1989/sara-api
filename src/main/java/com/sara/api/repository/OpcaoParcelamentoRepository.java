package com.sara.api.repository;

import com.sara.api.model.OpcaoParcelamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OpcaoParcelamentoRepository extends JpaRepository<OpcaoParcelamento, Long> {

    @Modifying
    @Query(value = "DELETE FROM usuario_opcao_parcelamento WHERE opcao_parcelamento_id = :id", nativeQuery = true)
    void deleteUserAssociations(@Param("id") Long id);
}
