package com.sara.api.service;

import com.sara.api.dto.UsuarioRequestDTO;
import com.sara.api.dto.UsuarioResponseDTO;
import com.sara.api.model.Role;
import com.sara.api.model.Usuario;
import com.sara.api.repository.UsuarioRepository;
import com.sara.api.repository.SetorRepository;
import com.sara.api.repository.TabelaFreteRepository;
import com.sara.api.repository.FormaPagamentoRepository;
import com.sara.api.validator.UsuarioValidator;
import com.sara.api.exception.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioValidator usuarioValidator;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private TabelaFreteRepository tabelaFreteRepository;

    @Autowired
    private FormaPagamentoRepository formaPagamentoRepository;

    public UsuarioResponseDTO criar(UsuarioRequestDTO request) {
        usuarioValidator.validar(request, true);
        Usuario usuario = new Usuario();
        updateUsuarioFromDTO(request, usuario);
        usuario.setSenha(convertToMD5(request.getSenha()));
        usuario.setAtivo(true);
        return toDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO alterar(Long id, UsuarioRequestDTO request) {
        usuarioValidator.validar(request, false);
        return usuarioRepository.findById(id).map(usuario -> {
            if (usuario.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN && usuario.getAtivo() && usuarioRepository.countByRoleAndAtivoTrue(Role.ADMIN) <= 1) {
                throw new ValidationException("Não é possível remover o perfil de ADMIN. O sistema deve possuir pelo menos um ADMIN ativo.", HttpStatus.BAD_REQUEST);
            }
            updateUsuarioFromDTO(request, usuario);
            if (request.getSenha() != null && !request.getSenha().isEmpty()) {
                usuario.setSenha(convertToMD5(request.getSenha()));
            }
            return toDTO(usuarioRepository.save(usuario));
        }).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private void updateUsuarioFromDTO(UsuarioRequestDTO request, Usuario usuario) {
        BeanUtils.copyProperties(request, usuario, "id", "senha", "setor", "tabelaFrete");

        if (request.getSetorId() != null) {
            usuario.setSetor(setorRepository.findById(request.getSetorId())
                    .orElseThrow(() -> new RuntimeException("Setor não encontrado")));
        } else {
            usuario.setSetor(null);
        }

        if (request.getTabelaFreteId() != null) {
            usuario.setTabelaFrete(tabelaFreteRepository.findById(request.getTabelaFreteId())
                    .orElseThrow(() -> new RuntimeException("Tabela de frete não encontrada")));
        } else {
            usuario.setTabelaFrete(null);
        }

        if (request.getFormaPagamentoId() != null) {
            usuario.setFormaPagamento(formaPagamentoRepository.findById(request.getFormaPagamentoId())
                    .orElseThrow(() -> new RuntimeException("Forma de Pagamento não encontrada")));
        } else {
            usuario.setFormaPagamento(null);
        }
    }

    public void desativar(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            if (usuario.getRole() == Role.ADMIN && usuarioRepository.countByRoleAndAtivoTrue(Role.ADMIN) <= 1) {
                throw new ValidationException("Não é possível desativar o usuário. O sistema deve possuir pelo menos um ADMIN ativo.", HttpStatus.BAD_REQUEST);
            }
            usuario.setAtivo(false);
            usuarioRepository.save(usuario);
        });
    }

    public void ativar(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setAtivo(true);
            usuarioRepository.save(usuario);
        });
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UsuarioResponseDTO> buscarPorNome(String nome) {
        return usuarioRepository.findByNomeContainingIgnoreCase(nome).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<UsuarioResponseDTO> buscarPorId(Long id) {
        return usuarioRepository.findById(id).map(this::toDTO);
    }

    private String convertToMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao converter senha para MD5", e);
        }
    }

    private UsuarioResponseDTO toDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        BeanUtils.copyProperties(usuario, dto);

        if (usuario.getSetor() != null) {
            dto.setSetorId(usuario.getSetor().getId());
        }

        if (usuario.getTabelaFrete() != null) {
            dto.setTabelaFreteId(usuario.getTabelaFrete().getId());
        }

        if (usuario.getFormaPagamento() != null) {
            dto.setFormaPagamentoId(usuario.getFormaPagamento().getId());
            dto.setFormaPagamentoDescricao(usuario.getFormaPagamento().getDescricao());
        }

        return dto;
    }
}
