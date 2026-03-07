package com.sara.api.repository;

import com.sara.api.model.Carrinho;
import com.sara.api.model.CarrinhoId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarrinhoRepository extends JpaRepository<Carrinho, CarrinhoId> {
    List<Carrinho> findByUsuarioId(Long usuarioId);
    List<Carrinho> findByProdutoId(Long produtoId);
}
