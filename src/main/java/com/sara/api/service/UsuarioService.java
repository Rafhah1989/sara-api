package com.sara.api.service;

import com.sara.api.dto.OpcaoParcelamentoResponseDTO;
import com.sara.api.dto.UsuarioRequestDTO;
import com.sara.api.dto.UsuarioResponseDTO;
import com.sara.api.model.OpcaoParcelamento;
import com.sara.api.model.Role;
import com.sara.api.model.Usuario;
import com.sara.api.repository.*;
import com.sara.api.validator.UsuarioValidator;
import com.sara.api.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
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

    @Autowired
    private EmailService emailService;

    @Autowired
    private OpcaoParcelamentoRepository opcaoParcelamentoRepository;

    @Value("${URL_BASE:http://localhost:4200}")
    private String urlBase;

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO request) {
        usuarioValidator.validar(request, true);
        Usuario usuario = new Usuario();
        updateUsuarioFromDTO(request, usuario);
        
        boolean enviarEmailCertificacao = false;
        String senhaAleatoria = null;
        
        if (request.getSenha() == null || request.getSenha().trim().isEmpty()) {
            senhaAleatoria = gerarSenhaAleatoria();
            usuario.setSenha(convertToMD5(senhaAleatoria));
            usuario.setToken(UUID.randomUUID().toString());
            usuario.setDataExpiracao(LocalDateTime.now().plusHours(24));
            enviarEmailCertificacao = true;
        } else {
            usuario.setSenha(convertToMD5(request.getSenha()));
        }
        
        usuario.setAtivo(true);
        Usuario saved = usuarioRepository.save(usuario);
        
        if (enviarEmailCertificacao) {
            enviarEmailConvite(saved, senhaAleatoria);
        }
        
        return toDTO(saved);
    }

    private String gerarSenhaAleatoria() {
        SecureRandom random = new SecureRandom();
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }

    private void enviarEmailConvite(Usuario usuario, String senhaProvisoria) {
        String link = String.format("%s/reset-password?token=%s", urlBase, usuario.getToken());
        String subject = "Bem-vindo ao Sistema Sara - Sua conta foi criada";
        
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; color: #333;'>");
        sb.append("<h1 style='color: #c5a059;'>Conta Criada com Sucesso!</h1>");
        sb.append("<p>Olá, <strong>").append(usuario.getNome()).append("</strong>.</p>");
        sb.append("<p>Sua conta no Sistema Sara foi criada pelo administrador.</p>");
        sb.append("<p><strong>Login (CPF/CNPJ):</strong> ").append(usuario.getCpfCnpj()).append("</p>");
        sb.append("<p><strong>Senha Provisória:</strong> ").append(senhaProvisoria).append("</p>");
        sb.append("<p>Para sua segurança, é necessário definir uma senha pessoal clicando no botão abaixo:</p>");
        sb.append("<div style='margin: 30px 0;'>");
        sb.append("<a href='").append(link).append("' style='background-color: #c5a059; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Definir Minha Senha</a>");
        sb.append("</div>");
        sb.append("<p>Caso o botão não funcione, copie e cole o link abaixo no seu navegador:</p>");
        sb.append("<p style='word-break: break-all; color: #666;'>").append(link).append("</p>");
        sb.append("<p><small>Este link é válido por 24 horas. Se você não reconhece esta ação, entre em contato com o suporte.</small></p>");
        sb.append("</div>");
        
        // Vamos usar o EmailService já existente
        emailService.enviarEmailGenerico(usuario.getEmail(), subject, sb.toString());
        log.info("E-mail de convite enviado para {}. Link: {}", usuario.getEmail(), link);
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

        if (request.getPermitirParcelamento() != null) {
            usuario.setPermitirParcelamento(request.getPermitirParcelamento());
        }

        if (request.getAtivarDescontoAVista() != null) {
            usuario.setAtivarDescontoAVista(request.getAtivarDescontoAVista());
        }

        if (request.getOpcoesParcelamentoIds() != null) {
            usuario.getOpcoesParcelamento().clear();
            List<OpcaoParcelamento> opcoes = opcaoParcelamentoRepository.findAllById(request.getOpcoesParcelamentoIds());
            usuario.getOpcoesParcelamento().addAll(opcoes);
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

    public List<UsuarioResponseDTO> buscarPorTelefone(String telefone) {
        String cleanedTelefone = telefone.replaceAll("\\D", "");
        return usuarioRepository.findByTelefoneContaining(cleanedTelefone).stream()
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

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        Usuario usuario = usuarioRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Token inválido", HttpStatus.NOT_FOUND));

        if (usuario.getDataExpiracao() == null || usuario.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Token expirado. Solicite uma nova recuperação.", HttpStatus.BAD_REQUEST);
        }

        usuario.setSenha(convertToMD5(novaSenha));
        usuario.setToken(null);
        usuario.setDataExpiracao(null);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void iniciarRecuperacaoSenha(String cpfCnpj) {
        String cleanedCpfCnpj = cpfCnpj.replaceAll("\\D", "");
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpfCnpj(cleanedCpfCnpj);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setToken(UUID.randomUUID().toString());
            usuario.setDataExpiracao(LocalDateTime.now().plusHours(72));
            usuarioRepository.save(usuario);
            
            enviarEmailRecuperacao(usuario);
        } else {
            log.warn("Tentativa de recuperação de senha para CPF/CNPJ não cadastrado: {}", cleanedCpfCnpj);
        }
    }

    private void enviarEmailRecuperacao(Usuario usuario) {
        String link = String.format("%s/reset-password?token=%s", urlBase, usuario.getToken());
        String subject = "Recuperação de Senha - Sistema Sara";
        
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; color: #333;'>");
        sb.append("<h1 style='color: #c5a059;'>Recuperação de Senha</h1>");
        sb.append("<p>Olá, <strong>").append(usuario.getNome()).append("</strong>.</p>");
        sb.append("<p>Recebemos uma solicitação para alterar a senha da sua conta no Sistema Sara.</p>");
        sb.append("<p>Para definir uma nova senha, clique no botão abaixo:</p>");
        sb.append("<div style='margin: 30px 0;'>");
        sb.append("<a href='").append(link).append("' style='background-color: #c5a059; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Alterar Minha Senha</a>");
        sb.append("</div>");
        sb.append("<p>Caso o botão não funcione, copie e cole o link abaixo no seu navegador:</p>");
        sb.append("<p style='word-break: break-all; color: #666;'>").append(link).append("</p>");
        sb.append("<p><small>Este link é válido por 72 horas. Se você não solicitou esta alteração, pode ignorar este e-mail com segurança.</small></p>");
        sb.append("</div>");
        
        emailService.enviarEmailGenerico(usuario.getEmail(), subject, sb.toString());
        log.info("E-mail de recuperação enviado para {}. Link: {}", usuario.getEmail(), link);
    }

    public UsuarioResponseDTO validarToken(String token) {
        Usuario usuario = usuarioRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Token inválido", HttpStatus.NOT_FOUND));

        if (usuario.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Token expirado", HttpStatus.BAD_REQUEST);
        }

        return toDTO(usuario);
    }

    @Transactional
    public void reenviarEmailConvite(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String novaSenha = gerarSenhaAleatoria();
        usuario.setSenha(convertToMD5(novaSenha));
        usuario.setToken(UUID.randomUUID().toString());
        usuario.setDataExpiracao(LocalDateTime.now().plusHours(24));
        
        Usuario saved = usuarioRepository.save(usuario);
        enviarEmailConvite(saved, novaSenha);
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

        dto.setPermitirParcelamento(usuario.getPermitirParcelamento());
        dto.setAtivarDescontoAVista(usuario.getAtivarDescontoAVista());
        if (usuario.getOpcoesParcelamento() != null) {
            dto.setOpcoesParcelamento(usuario.getOpcoesParcelamento().stream().map(opt -> {
                OpcaoParcelamentoResponseDTO optDto = new OpcaoParcelamentoResponseDTO();
                optDto.setId(opt.getId());
                optDto.setFormaPagamentoId(opt.getFormaPagamento().getId());
                optDto.setFormaPagamentoDescricao(opt.getFormaPagamento().getDescricao());
                optDto.setQtdMaxParcelas(opt.getQtdMaxParcelas());
                optDto.setDiasVencimentoIntervalo(opt.getDiasVencimentoIntervalo());
                optDto.setValorMinimoParcela(opt.getValorMinimoParcela());
                return optDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}
