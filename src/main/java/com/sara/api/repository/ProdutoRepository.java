package com.sara.api.repository;

import com.sara.api.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {
    @org.springframework.data.jpa.repository.Query("SELECT new com.sara.api.dto.ProdutoMiniDTO(p.id, p.nome, p.tamanho, p.ativo, p.codigo, p.preco, p.peso, " +
            "CASE WHEN (p.imagem IS NOT NULL AND p.imagem <> '') THEN true ELSE false END) " +
            "FROM Produto p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND p.ativo = true")
    List<com.sara.api.dto.ProdutoMiniDTO> findByNomeMini(@org.springframework.data.repository.query.Param("nome") String nome);

    List<Produto> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);

    List<Produto> findByNomeAndAtivoTrueAndIdNot(String nome, Long id);

    java.util.Optional<Produto> findByCodigoAndAtivoTrue(Long codigo);

    List<Produto> findByAtivoTrue();

    @org.springframework.data.jpa.repository.Query("SELECT new com.sara.api.dto.ProdutoMiniDTO(p.id, p.nome, p.tamanho, p.ativo, p.codigo, p.preco, p.peso, " +
            "CASE WHEN (p.imagem IS NOT NULL AND p.imagem <> '') THEN true ELSE false END) " +
            "FROM Produto p WHERE p.ativo = true ORDER BY p.nome ASC, p.codigo ASC, p.tamanho ASC")
    List<com.sara.api.dto.ProdutoMiniDTO> findAtivosMini();

    List<Produto> findByAtivoTrueOrderByNomeAscCodigoAscTamanhoAsc();
}
