package com.sara.api.service;

import com.sara.api.model.Configuracao;
import com.sara.api.model.Pedido;
import com.sara.api.model.PedidoProduto;
import com.sara.api.repository.ConfiguracaoRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final ConfiguracaoRepository configuracaoRepository;

    @Async
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void enviarEmailNovoPedido(Pedido pedido) {
        log.info("Iniciando tentativa de envio de e-mail para o pedido #{}", pedido.getId());
        
        Configuracao config = configuracaoRepository.findAll().stream().findFirst().orElse(null);
        if (config == null || !Boolean.TRUE.equals(config.getEmailAtivo())) {
            log.info("Envio de e-mail desativado ou configuração não encontrada.");
            return;
        }

        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(config.getMailHost());
            mailSender.setPort(config.getMailPort());
            mailSender.setUsername(config.getMailUsername());
            mailSender.setPassword(config.getMailPassword());

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", String.valueOf(config.getMailAuth()));
            props.put("mail.smtp.starttls.enable", String.valueOf(config.getMailStarttls()));
            props.put("mail.smtp.ssl.trust", config.getMailHost());
            props.put("mail.debug", "false");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = String.format("Pedido %d criado - %s", pedido.getId(), pedido.getUsuario().getNome());
            helper.setSubject(subject);
            helper.setFrom(config.getMailUsername());
            helper.setTo(config.getEmailsNotificacao().split(","));

            String content = buildOrderEmailContent(pedido);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("E-mail enviado com sucesso para o pedido #{}", pedido.getId());

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para o pedido #{}: {}", pedido.getId(), e.getMessage());
            throw new RuntimeException("Falha no envio de e-mail", e);
        }
    }

    private String buildOrderEmailContent(Pedido pedido) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = pedido.getDataPedido().format(formatter);

        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Novo Pedido Recebido!</h1>");
        sb.append("<p><strong>Número do Pedido:</strong> #").append(pedido.getId()).append("</p>");
        sb.append("<p><strong>Cliente:</strong> ").append(pedido.getUsuario().getNome()).append("</p>");
        sb.append("<p><strong>Data:</strong> ").append(dataFormatada).append("</p>");
        sb.append("<p><strong>Observação:</strong> ").append(pedido.getObservacao() != null ? pedido.getObservacao() : "-").append("</p>");

        sb.append("<h3>Itens do Pedido:</h3>");
        sb.append("<table border='1' style='border-collapse: collapse; width: 60%; font-family: Arial, sans-serif;'>");
        sb.append("<thead><tr style='background-color: #f2f2f2;'>");
        sb.append("<th>Produto</th><th>Tam.</th><th>Qtd</th><th style='text-align: center;'>Unt R$</th><th style='text-align: center;'>Tot R$</th>");
        sb.append("</tr></thead><tbody>");

        java.util.List<PedidoProduto> produtos = new java.util.ArrayList<>(pedido.getProdutos());
        produtos.sort(java.util.Comparator
                .comparing((PedidoProduto p) -> p.getProduto().getNome().toLowerCase())
                .thenComparing(p -> p.getProduto().getTamanho())
                .thenComparing(p -> p.getValor()));

        for (PedidoProduto item : produtos) {
            double subtotal = item.getValor().doubleValue() * item.getQuantidade().doubleValue();
            
            String valorUnitStr = String.format("%.2f", item.getValor().doubleValue()).replace(".", ",");
            String subtotalStr = String.format("%.2f", subtotal).replace(".", ",");
            
            String[] vUnitParts = valorUnitStr.split(",");
            String[] vTotalParts = subtotalStr.split(",");

            String tamFormatado = "-";
            if (item.getProduto().getTamanho() != null) {
                tamFormatado = String.format("%02dcm", item.getProduto().getTamanho());
            }

            String qtdFormatada = String.format("%02d", item.getQuantidade().intValue());

            sb.append("<tr>");
            sb.append("<td style='padding: 5px;'>").append(item.getProduto().getNome()).append("</td>");
            sb.append("<td style='text-align: center; padding: 5px;'>").append(tamFormatado).append("</td>");
            sb.append("<td style='text-align: center; padding: 5px;'>").append(qtdFormatada).append("</td>");
            
            // Valor Unitário (Centralização decimal perfeita: vírgula no centro do TD)
            sb.append("<td style='padding: 0;'>");
            sb.append("<table style='width: 100%; border: none; border-collapse: collapse; font-family: Arial, sans-serif;'>");
            sb.append("<tr><td style='border: none; padding: 5px 0; text-align: right; width: 50%;'>").append(vUnitParts[0]).append("</td>");
            sb.append("<td style='border: none; padding: 5px 0; text-align: left; width: 50%;'>,").append(vUnitParts[1]).append("</td>");
            sb.append("</tr></table></td>");

            // Subtotal (Centralização decimal perfeita: vírgula no centro do TD)
            sb.append("<td style='padding: 0;'>");
            sb.append("<table style='width: 100%; border: none; border-collapse: collapse; font-family: Arial, sans-serif;'>");
            sb.append("<tr><td style='border: none; padding: 5px 0; text-align: right; width: 50%;'>").append(vTotalParts[0]).append("</td>");
            sb.append("<td style='border: none; padding: 5px 0; text-align: left; width: 50%;'>,").append(vTotalParts[1]).append("</td>");
            sb.append("</tr></table></td>");
            
            sb.append("</tr>");
        }

        sb.append("</tbody></table>");

        sb.append("<div style='margin-top: 20px;'>");
        sb.append("<p><strong>Frete:</strong> R$ ").append(String.format("%.2f", pedido.getFrete().doubleValue())).append("</p>");
        sb.append("<p><strong>Desconto:</strong> ").append(String.format("%.2f", pedido.getDesconto().doubleValue())).append("%</p>");
        sb.append("<p style='font-size: 1.2em;'><strong>Valor Total:</strong> R$ ").append(String.format("%.2f", pedido.getValorTotal().doubleValue())).append("</p>");
        sb.append("</div>");

        return sb.toString();
    }
}
