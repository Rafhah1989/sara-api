package com.sara.api.service;

import com.sara.api.dto.ProdutoMiniDTO;
import com.sara.api.dto.ProdutoResumoDTO;
import com.sara.api.model.Produto;
import com.sara.api.repository.ProdutoRepository;
import com.sara.api.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private OciObjectStorageService ociService;

    @Value("${oci.bucket.name.products:sara_produtos}")
    private String productsBucket;

    private void processarImagem(Produto produto) {
        String base64Image = produto.getImagem();
        if (base64Image != null && base64Image.startsWith("data:image")) {
            try {
                String[] parts = base64Image.split(",");
                String metadata = parts[0];
                String payload = parts[1];
                
                String ext = "jpeg";
                if (metadata.contains("image/png")) ext = "png";
                else if (metadata.contains("image/gif")) ext = "gif";
                else if (metadata.contains("image/webp")) ext = "webp";
                
                String fileName = produto.getId() + "_imagem." + ext;
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(payload);
                
                try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(decodedBytes)) {
                    ociService.uploadFile(productsBucket, fileName, bis, decodedBytes.length, "image/" + ext);
                }
                
                produto.setImagem(fileName);
                produtoRepository.save(produto);
            } catch (Exception e) {
                System.err.println("Erro ao processar imagem para o OCI: " + e.getMessage());
            }
        }
    }

    public int migrarImagensEmLote() {
        List<Produto> produtos = produtoRepository.findAll();
        int count = 0;
        for (Produto p : produtos) {
            if (p.getImagem() != null && p.getImagem().startsWith("data:image")) {
                processarImagem(p);
                count++;
            }
        }
        return count;
    }

    public Produto salvar(Produto produto) {
        validar(produto);
        String imagemOriginal = produto.getImagem();
        if (imagemOriginal != null && imagemOriginal.startsWith("data:image")) {
            produto.setImagem(null);
        }
        Produto salvo = produtoRepository.save(produto);
        
        if (imagemOriginal != null && imagemOriginal.startsWith("data:image")) {
            salvo.setImagem(imagemOriginal);
            processarImagem(salvo);
        } else {
            salvo.setImagem(imagemOriginal);
            salvo = produtoRepository.save(salvo);
        }
        return salvo;
    }

    public List<ProdutoMiniDTO> buscarPorNomeMini(String nome) {
        return produtoRepository.findByNomeMini(nome);
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Produto> listarAtivos() {
        return produtoRepository.findByAtivoTrue();
    }

    public Optional<Produto> buscarPorCodigo(String codigo) {
        return produtoRepository.findByCodigoAndAtivoTrue(codigo);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public Produto alterar(Long id, Produto produtoAtualizado) {
        validar(produtoAtualizado);
        return produtoRepository.findById(id)
                .map(produto -> {
                    produto.setNome(produtoAtualizado.getNome());
                    produto.setTamanho(produtoAtualizado.getTamanho());
                    produto.setAtivo(produtoAtualizado.getAtivo());
                    produto.setCodigo(produtoAtualizado.getCodigo());
                    produto.setPeso(produtoAtualizado.getPeso());
                    produto.setPreco(produtoAtualizado.getPreco());

                    String novaImagem = produtoAtualizado.getImagem();
                    if (novaImagem != null && novaImagem.startsWith("data:image")) {
                        produto.setImagem(novaImagem);
                        Produto salvado = produtoRepository.save(produto);
                        processarImagem(salvado);
                        return salvado;
                    } else if (novaImagem == null || novaImagem.isEmpty()) {
                        if (produto.getImagem() != null && !produto.getImagem().isEmpty() && !produto.getImagem().startsWith("data:image")) {
                            try {
                                ociService.deleteFile(productsBucket, produto.getImagem());
                            } catch (Exception e) {}
                        }
                        produto.setImagem("");
                    }
                    return produtoRepository.save(produto);
                })
                .orElseThrow(() -> new ValidationException("Produto não encontrado com id: " + id));
    }

    private void validar(Produto produto) {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new ValidationException("O nome do produto é obrigatório");
        }
    }

    public void excluir(Long id) {
        produtoRepository.findById(id).ifPresent(p -> {
            if (p.getImagem() != null && !p.getImagem().isEmpty() && !p.getImagem().startsWith("data:image")) {
                try {
                    ociService.deleteFile(productsBucket, p.getImagem());
                } catch (Exception e) {}
            }
            produtoRepository.delete(p);
        });
    }

    public Page<ProdutoResumoDTO> buscarParaLoja(String nome, List<Integer> tamanhos, Double precoMin, Double precoMax, Pageable pageable) {
        Specification<Produto> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("ativo")));
            predicates.add(cb.isNotNull(root.get("preco")));

            if (nome != null && !nome.trim().isEmpty()) {
                String termLower = nome.toLowerCase();
                Predicate matchNome = cb.like(cb.lower(root.get("nome")), "%" + termLower + "%");
                Predicate matchCodigo = cb.like(cb.lower(root.get("codigo")), "%" + termLower + "%");
                predicates.add(cb.or(matchNome, matchCodigo));

                // ORDENAÇÃO CUSTOMIZADA POR PRIORIDADE DE CÓDIGO
                query.orderBy(
                    cb.asc(
                        cb.selectCase()
                            .when(cb.like(cb.lower(root.get("codigo")), termLower + "%"), 1)
                            .when(cb.like(cb.lower(root.get("codigo")), "%" + termLower), 2)
                            .when(cb.like(cb.lower(root.get("codigo")), "%" + termLower + "%"), 3)
                            .otherwise(4)
                    ),
                    cb.asc(root.get("nome"))
                );
            }
            if (tamanhos != null && !tamanhos.isEmpty()) {
                predicates.add(root.get("tamanho").in(tamanhos));
            }
            if (precoMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("preco"), precoMin));
            }
            if (precoMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("preco"), precoMax));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Garante que a ordenação seja estável adicionando nome e codigo como critérios secundários
        org.springframework.data.domain.Sort stableSort = pageable.getSort();
        if (stableSort.isSorted()) {
            stableSort = stableSort.and(org.springframework.data.domain.Sort.by("nome").ascending())
                                 .and(org.springframework.data.domain.Sort.by("codigo").ascending());
        } else {
            // Se já não houver ordenação (que incluímos acima na Specification), usamos o padrão estável.
            // Nota: Specification.orderBy sobrescreve o Pageable.sort na maioria das configurações do Hibernate/Spring.
            stableSort = org.springframework.data.domain.Sort.by("nome").ascending()
                                 .and(org.springframework.data.domain.Sort.by("codigo").ascending());
        }
        
        pageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), stableSort);

        return produtoRepository.findAll(spec, pageable).map(ProdutoResumoDTO::new);
    }

    public List<ProdutoMiniDTO> listarAtivosMini() {
        return produtoRepository.findAtivosMini();
    }

    public List<Produto> buscarOutrosTamanhos(Long id) {
        Produto produtoOriginal = produtoRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Produto original não encontrado"));
        return produtoRepository.findByNomeAndAtivoTrueAndIdNot(produtoOriginal.getNome(), id);
    }

    public List<Produto> listarAtivosOrdenados() {
        return produtoRepository.findByAtivoTrueOrderByNomeAscCodigoAscTamanhoAsc();
    }

    public List<Integer> listarTamanhosAtivos() {
        return produtoRepository.findDistinctTamanhosAtivos();
    }
}
