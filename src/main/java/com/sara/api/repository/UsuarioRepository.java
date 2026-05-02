package com.sara.api.repository;

import com.sara.api.model.Role;
import com.sara.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findByNomeContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nome, String codigo);
    List<Usuario> findByTelefoneContaining(String telefone);
    Optional<Usuario> findByCpfCnpj(String cpfCnpj);
    Optional<Usuario> findByToken(String token);
    Optional<Usuario> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
    long countByRoleAndAtivoTrue(Role role);
}
