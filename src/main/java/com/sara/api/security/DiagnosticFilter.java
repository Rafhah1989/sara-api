package com.sara.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Filtro de diagnóstico de baixíssimo nível.
 * Deve ser registrado ANTES do Spring Security para capturar tentativas de acesso
 * que podem ser bloqueadas por filtros de segurança ou configurações do servidor.
 */
@Component
public class DiagnosticFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Foca apenas no webhook para não poluir o log desnecessariamente
        if (path != null && path.contains("/api/mercadopago/webhook")) {
            System.out.println(">>> [DIAGNOSTICO WEBHOOK] Recebida tentativa de acesso!");
            System.out.println(">>> [DIAGNOSTICO] Origem IP: " + request.getRemoteAddr());
            System.out.println(">>> [DIAGNOSTICO] Metodo: " + request.getMethod());
            System.out.println(">>> [DIAGNOSTICO] Content-Type: " + request.getContentType());
            System.out.println(">>> [DIAGNOSTICO] User-Agent: " + request.getHeader("User-Agent"));
            
            // Log de Headers importantes para o MP
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (headerName.toLowerCase().contains("x-") || headerName.toLowerCase().contains("version")) {
                     System.out.println(">>> [DIAGNOSTICO] Header - " + headerName + ": " + request.getHeader(headerName));
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
