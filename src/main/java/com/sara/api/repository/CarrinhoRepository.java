package com.sara.api.repository;

import com.sara.api.model.Carrinho;
import com.sara.api.model.CarrinhoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface CarrinhoRepository extends JpaRepository<Carrinho, CarrinhoId> {
    List<Carrinho> findByUsuarioId(Long usuarioId);
    List<Carrinho> findByProdutoId(Long produtoId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Carrinho c WHERE c.usuario.id = :usuarioId")
    void deleteByUsuarioId(Long usuarioId);
}
