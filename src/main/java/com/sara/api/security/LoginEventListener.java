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
import com.sara.api.service.GeoIpService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginEventListener {

    private final LoginLogRepository loginLogRepository;
    private final UsuarioRepository usuarioRepository;
    private final GeoIpService geoIpService;

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
        String ip = "Unknown";
        String local = "Local Indisponível";
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            userAgent = request.getHeader("User-Agent");
            
            // Tenta obter o IP real se estiver atrás de um proxy (como Nginx/Cloudflare)
            ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            
            // Em caso de múltiplos IPs no X-Forwarded-For, pega o primeiro
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            local = geoIpService.obterLocalizacao(ip);
        }

        LoginLog logEntry = LoginLog.builder()
                .usuario(usuario)
                .dataHora(LocalDateTime.now())
                .userAgent(userAgent)
                .status(status)
                .local(local)
                .build();

        loginLogRepository.save(logEntry);
        log.info("Log de login registrado para o usuário: {} - Status: {}", usuario.getUsername(), status);
    }
}
