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
        sb.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        sb.append("<thead><tr style='background-color: #f2f2f2;'>");
        sb.append("<th>Produto</th><th>Tamanho</th><th>Qtd</th><th>Valor Unit.</th><th>Subtotal</th>");
        sb.append("</tr></thead><tbody>");

        for (PedidoProduto item : pedido.getProdutos()) {
            double subtotal = item.getValor().doubleValue() * item.getQuantidade().doubleValue();
            sb.append("<tr>");
            sb.append("<td>").append(item.getProduto().getNome()).append("</td>");
            sb.append("<td>").append(item.getProduto().getTamanho() != null ? item.getProduto().getTamanho() : "-").append("</td>");
            sb.append("<td style='text-align: center;'>").append(item.getQuantidade()).append("</td>");
            sb.append("<td style='text-align: right;'>R$ ").append(String.format("%.2f", item.getValor().doubleValue())).append("</td>");
            sb.append("<td style='text-align: right;'>R$ ").append(String.format("%.2f", subtotal)).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</tbody></table>");

        sb.append("<div style='margin-top: 20px;'>");
        sb.append("<p><strong>Frete:</strong> R$ ").append(String.format("%.2f", pedido.getFrete().doubleValue())).append("</p>");
        sb.append("<p><strong>Desconto:</strong> R$ ").append(String.format("%.2f", pedido.getDesconto().doubleValue())).append("</p>");
        sb.append("<p style='font-size: 1.2em;'><strong>Valor Total:</strong> R$ ").append(String.format("%.2f", pedido.getValorTotal().doubleValue())).append("</p>");
        sb.append("</div>");

        return sb.toString();
    }
}
