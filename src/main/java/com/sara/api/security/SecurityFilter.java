package com.sara.api.security;

import com.sara.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // PRIORIDADE ABSOLUTA: Webhook do Mercado Pago não deve ser processado pelo filtro de segurança convencional
        if (path != null && path.contains("/api/mercadopago/webhook")) {
            System.out.println(">>> [SECURITY] Liberando webhook imediatamente: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var tokenJWT = recuperarToken(request);

            if (tokenJWT != null) {
                var subject = tokenService.getSubject(tokenJWT);
                var usuario = repository.findByCpfCnpj(subject).orElse(null);

                if (usuario != null && usuario.isEnabled()) {
                    var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // Se houver erro no processamento do token (token expirado, inválido, etc), 
            // apenas não autentica. O Spring Security cuidará do bloqueio se o endpoint for protegido.
            System.err.println(">>> [SECURITY] Erro silencioso ao processar token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.replace("Bearer ", "").trim();
        }
        return null;
    }
}
