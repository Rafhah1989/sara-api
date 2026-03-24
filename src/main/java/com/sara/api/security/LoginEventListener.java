package com.sara.api.security;

import com.sara.api.model.LoginLog;
import com.sara.api.model.LoginStatus;
import com.sara.api.model.Usuario;
import com.sara.api.repository.LoginLogRepository;
import com.sara.api.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginEventListener {

    private final LoginLogRepository loginLogRepository;
    private final UsuarioRepository usuarioRepository;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) {
            saveLog(usuario, LoginStatus.SUCESSO);
        }
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        usuarioRepository.findByCpfCnpj(username).ifPresent(usuario -> {
            saveLog(usuario, LoginStatus.FALHA);
        });
    }

    private void saveLog(Usuario usuario, LoginStatus status) {
        String userAgent = "Unknown";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            userAgent = request.getHeader("User-Agent");
        }

        LoginLog logEntry = LoginLog.builder()
                .usuario(usuario)
                .dataHora(LocalDateTime.now())
                .userAgent(userAgent)
                .status(status)
                .build();

        loginLogRepository.save(logEntry);
        log.info("Log de login registrado para o usuário: {} - Status: {}", usuario.getUsername(), status);
    }
}
