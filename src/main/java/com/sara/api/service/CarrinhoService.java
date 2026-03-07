package com.sara.api.service;

import com.sara.api.dto.CarrinhoRequestDTO;
import com.sara.api.dto.CarrinhoResponseDTO;
import com.sara.api.exception.ValidationException;
import com.sara.api.model.Carrinho;
import com.sara.api.model.CarrinhoId;
import com.sara.api.model.Produto;
import com.sara.api.model.Usuario;
import com.sara.api.repository.CarrinhoRepository;
import com.sara.api.repository.ProdutoRepository;
import com.sara.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarrinhoService {

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    public CarrinhoResponseDTO adicionar(CarrinhoRequestDTO dto) {
        CarrinhoId id = new CarrinhoId(dto.getUsuarioId(), dto.getProdutoId());
        
        if (carrinhoRepository.existsById(id)) {
            throw new ValidationException("O produto já está no carrinho do usuário.");
        }

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ValidationException("Usuário não encontrado: " + dto.getUsuarioId()));
                
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new ValidationException("Produto não encontrado: " + dto.getProdutoId()));

        Carrinho carrinho = new Carrinho();
        carrinho.setId(id);
        carrinho.setUsuario(usuario);
        carrinho.setProduto(produto);
        carrinho.setQuantidade(dto.getQuantidade() != null ? dto.getQuantidade() : 1);

        carrinho = carrinhoRepository.save(carrinho);
        return converterParaDto(carrinho);
    }

    public void remover(Long usuarioId, Long produtoId) {
        CarrinhoId id = new CarrinhoId(usuarioId, produtoId);
        if (!carrinhoRepository.existsById(id)) {
            throw new ValidationException("Item de carrinho não encontrado.");
        }
        carrinhoRepository.deleteById(id);
    }

    public CarrinhoResponseDTO atualizarQuantidade(Long usuarioId, Long produtoId, Integer quantidade) {
        CarrinhoId id = new CarrinhoId(usuarioId, produtoId);
        Carrinho carrinho = carrinhoRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Item de carrinho não encontrado."));
        
        if (quantidade == null || quantidade <= 0) {
            throw new ValidationException("A quantidade deve ser maior que zero.");
        }
        
        carrinho.setQuantidade(quantidade);
        carrinho = carrinhoRepository.save(carrinho);
        return converterParaDto(carrinho);
    }

    public List<CarrinhoResponseDTO> listarTodos() {
        return carrinhoRepository.findAll().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    public List<CarrinhoResponseDTO> buscarPorUsuario(Long usuarioId) {
        return carrinhoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    public List<CarrinhoResponseDTO> buscarPorProduto(Long produtoId) {
        return carrinhoRepository.findByProdutoId(produtoId).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    private CarrinhoResponseDTO converterParaDto(Carrinho carrinho) {
        CarrinhoResponseDTO dto = new CarrinhoResponseDTO();
        
        Usuario u = carrinho.getUsuario();
        dto.setUsuarioId(u.getId());
        dto.setUsuarioNome(u.getNome());
        dto.setUsuarioCpfCnpj(u.getCpfCnpj());
        dto.setUsuarioRole(u.getRole());

        Produto p = carrinho.getProduto();
        dto.setProdutoId(p.getId());
        dto.setProdutoNome(p.getNome());
        dto.setProdutoCodigo(p.getCodigo());
        dto.setProdutoPreco(p.getPreco());
        dto.setProdutoAtivo(p.getAtivo());
        dto.setProdutoTamanho(p.getTamanho());
        dto.setProdutoImagem(p.getImagem());
        dto.setQuantidade(carrinho.getQuantidade());
        
        return dto;
    }
}
