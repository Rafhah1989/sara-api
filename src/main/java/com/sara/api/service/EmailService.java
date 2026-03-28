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

import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Async
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void enviarEmailPedidoCancelado(Pedido pedido, com.sara.api.model.Usuario responsavel, String motivo) {
        log.info("Iniciando tentativa de envio de e-mail de cancelamento para o pedido #{}", pedido.getId());

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

            String subject = String.format("Pedido %d cancelado - %s", pedido.getId(), pedido.getUsuario().getNome());
            helper.setSubject(subject);
            helper.setFrom(config.getMailUsername());
            helper.setTo(config.getEmailsNotificacao().split(","));

            String content = buildOrderCanceledEmailContent(pedido, responsavel, motivo);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("E-mail de cancelamento enviado com sucesso para o pedido #{}", pedido.getId());

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de cancelamento para o pedido #{}: {}", pedido.getId(), e.getMessage());
            throw new RuntimeException("Falha no envio de e-mail de cancelamento", e);
        }
    }

    @Async
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void enviarEmailPedidoConfirmado(Pedido pedido) {
        log.info("Iniciando tentativa de envio de e-mail de confirmação para o pedido #{}", pedido.getId());

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

            String subject = String.format("Pedido %d confirmado - %s", pedido.getId(), pedido.getUsuario().getNome());
            helper.setSubject(subject);
            helper.setFrom(config.getMailUsername());
            helper.setTo(pedido.getUsuario().getEmail());

            String content = buildOrderConfirmedEmailContent(pedido);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("E-mail de confirmação enviado com sucesso para o pedido #{}", pedido.getId());

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de confirmação para o pedido #{}: {}", pedido.getId(), e.getMessage());
            throw new RuntimeException("Falha no envio de e-mail de confirmação", e);
        }
    }

    @Async
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void enviarEmailPedidoAtualizado(Pedido pedido) {
        log.info("Iniciando tentativa de envio de e-mail de atualização para o pedido #{}", pedido.getId());

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

            String subject = String.format("Pedido %d atualizado - %s", pedido.getId(), pedido.getUsuario().getNome());
            helper.setSubject(subject);
            helper.setFrom(config.getMailUsername());
            
            // Notificamos tanto o cliente quanto os e-mails de notificação
            String[] destinatarios = (pedido.getUsuario().getEmail() + "," + config.getEmailsNotificacao()).split(",");
            helper.setTo(destinatarios);

            String content = buildOrderUpdatedEmailContent(pedido);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("E-mail de atualização enviado com sucesso para o pedido #{}", pedido.getId());

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de atualização para o pedido #{}: {}", pedido.getId(), e.getMessage());
            throw new RuntimeException("Falha no envio de e-mail de atualização", e);
        }
    }

    @Async
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void enviarEmailNotaFiscalDisponivel(Pedido pedido) {
        log.info("Iniciando tentativa de envio de e-mail de nota fiscal para o pedido #{}", pedido.getId());

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

            String subject = String.format("Nota Fiscal disponível - Pedido #%d", pedido.getId());
            helper.setSubject(subject);
            helper.setFrom(config.getMailUsername());
            helper.setTo(pedido.getUsuario().getEmail());

            StringBuilder sb = new StringBuilder();
            sb.append("<div style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>");
            sb.append("<h1 style='color: #6f42c1;'>Sua Nota Fiscal está disponível!</h1>");
            sb.append("<p>Olá, <strong>").append(pedido.getUsuario().getNome()).append("</strong>.</p>");
            sb.append("<p>Informamos que a Nota Fiscal referente ao seu pedido <strong>#").append(pedido.getId())
                    .append("</strong> já foi emitida e está disponível para consulta.</p>");
            
            if (pedido.getNumeroNotaFiscal() != null) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                sb.append("<div style='background: #f8f9fa; border: 1px solid #dee2e6; padding: 15px; border-radius: 8px; margin: 20px 0;'>");
                sb.append("<p style='margin: 0;'><strong>Número da Nota Fiscal:</strong> ").append(pedido.getNumeroNotaFiscal()).append("</p>");
                if (pedido.getDataFaturamento() != null) {
                    sb.append("<p style='margin: 5px 0 0 0;'><strong>Data de Faturamento:</strong> ").append(pedido.getDataFaturamento().format(df)).append("</p>");
                }
                sb.append("</div>");
            }

            sb.append("<p>Acesse o sistema e vá na opção <strong>Pedidos</strong> para baixá-la.</p>");
            sb.append("<br/>");
            sb.append("<p>Atenciosamente,</p>");
            sb.append("<p><strong>Equipe Sara Imagens</strong></p>");
            sb.append("</div>");

            helper.setText(sb.toString(), true);

            mailSender.send(message);
            log.info("E-mail de nota fiscal enviado com sucesso para o cliente do pedido #{}", pedido.getId());

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de nota fiscal para o pedido #{}: {}", pedido.getId(), e.getMessage());
            throw new RuntimeException("Falha no envio de e-mail de nota fiscal", e);
        }
    }

    @Async
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void enviarEmailCobrancaPix(Pedido pedido, com.sara.api.model.Pagamento pagamento) {
        log.info("Iniciando envio de e-mail de cobrança PIX para o pedido #{}", pedido.getId());

        Configuracao config = configuracaoRepository.findAll().stream().findFirst().orElse(null);
        if (config == null || !Boolean.TRUE.equals(config.getEmailAtivo())) {
            log.info("Envio de e-mail desativado.");
            return;
        }

        try {
            JavaMailSenderImpl mailSender = createMailSender(config);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = String.format("Aguardando Pagamento - Pedido #%d - Parcela PIX", pedido.getId());
            helper.setSubject(subject);
            helper.setFrom(config.getMailUsername());
            helper.setTo(pedido.getUsuario().getEmail());

            StringBuilder sb = new StringBuilder();
            sb.append("<div style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>");
            sb.append("<h1 style='color: #d4af37;'>Seu código PIX está disponível!</h1>");
            sb.append("<p>Olá, <strong>").append(pedido.getUsuario().getNome()).append("</strong>.</p>");
            sb.append("<p>Informamos que o seu pedido <strong>#").append(pedido.getId())
                    .append("</strong> possui uma parcela PIX aguardando pagamento.</p>");
            
            sb.append("<div style='background: #fdf8e6; border: 1px solid #d4af37; padding: 15px; border-radius: 8px; margin: 20px 0;'>");
            sb.append("<p style='margin: 0;'><strong>Valor da Parcela:</strong> R$ ").append(String.format("%.2f", pagamento.getValor().doubleValue()).replace(".", ",")).append("</p>");
            sb.append("</div>");

            sb.append("<p>Para realizar o pagamento, por favor <strong>acesse o sistema</strong>, vá em 'Meus Pedidos', localize o pedido #").append(pedido.getId())
                    .append(" e clique no botão com o ícone de <strong>QR Code (Ver PIX)</strong> para visualizar o código de pagamento atualizado.</p>");
            sb.append("<br/>");
            sb.append("<p>Atenciosamente,</p>");
            sb.append("<p><strong>Equipe Sara Imagens</strong></p>");
            sb.append("</div>");

            helper.setText(sb.toString(), true);
            mailSender.send(message);
            log.info("E-mail de cobrança enviado para o cliente do pedido #{}", pedido.getId());

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de cobrança para o pedido #{}: {}", pedido.getId(), e.getMessage());
        }
    }

    @Async
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void enviarEmailPagamentoConfirmado(Pedido pedido, com.sara.api.model.Pagamento pagamento) {
        log.info("Iniciando envio de e-mails de confirmação de pagamento para o pedido #{}", pedido.getId());

        Configuracao config = configuracaoRepository.findAll().stream().findFirst().orElse(null);
        if (config == null || !Boolean.TRUE.equals(config.getEmailAtivo())) {
            log.info("Envio de e-mail desativado.");
            return;
        }

        try {
            JavaMailSenderImpl mailSender = createMailSender(config);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String dataConfirmacao = LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).format(formatter);

            // 1. E-mail para o Cliente
            MimeMessage clientMsg = mailSender.createMimeMessage();
            MimeMessageHelper clientHelper = new MimeMessageHelper(clientMsg, true, "UTF-8");
            clientHelper.setSubject(String.format("Pagamento Confirmado - Pedido #%d", pedido.getId()));
            clientHelper.setFrom(config.getMailUsername());
            clientHelper.setTo(pedido.getUsuario().getEmail());

            StringBuilder clientSb = new StringBuilder();
            clientSb.append("<div style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>");
            clientSb.append("<h1 style='color: #28a745;'>Confirmamos seu pagamento!</h1>");
            clientSb.append("<p>Olá, <strong>").append(pedido.getUsuario().getNome()).append("</strong>.</p>");
            clientSb.append("<p>Recebemos o pagamento referente a uma parcela do seu pedido <strong>#").append(pedido.getId()).append("</strong>.</p>");
            clientSb.append("<p><strong>Valor Pago:</strong> R$ ").append(String.format("%.2f", pagamento.getValor().doubleValue()).replace(".", ",")).append("</p>");
            clientSb.append("<p><strong>Data de Confirmação:</strong> ").append(dataConfirmacao).append("</p>");
            clientSb.append("<p>Seu pedido seguirá agora para as próximas etapas de processamento. Acompanhe o status pelo nosso sistema.</p>");
            clientSb.append("<br/><p>Obrigado por comprar conosco!</p>");
            clientSb.append("<p><strong>Equipe Sara Imagens</strong></p></div>");

            clientHelper.setText(clientSb.toString(), true);
            mailSender.send(clientMsg);

            // 2. Alerta para Administradores
            if (config.getEmailsNotificacao() != null && !config.getEmailsNotificacao().isEmpty()) {
                MimeMessage adminMsg = mailSender.createMimeMessage();
                MimeMessageHelper adminHelper = new MimeMessageHelper(adminMsg, true, "UTF-8");
                adminHelper.setSubject(String.format("ALERTA: Pagamento Recebido - Pedido #%d", pedido.getId()));
                adminHelper.setFrom(config.getMailUsername());
                adminHelper.setTo(config.getEmailsNotificacao().split(","));

                StringBuilder adminSb = new StringBuilder();
                adminSb.append("<div style='font-family: Arial, sans-serif; color: #333;'>");
                adminSb.append("<h2 style='color: #28a745;'>Novo Pagamento Recebido</h2>");
                adminSb.append("<p><strong>Pedido:</strong> #").append(pedido.getId()).append("</p>");
                adminSb.append("<p><strong>Cliente:</strong> ").append(pedido.getUsuario().getNome()).append("</p>");
                adminSb.append("<p><strong>Valor:</strong> R$ ").append(String.format("%.2f", pagamento.getValor().doubleValue()).replace(".", ",")).append("</p>");
                adminSb.append("<p><strong>Data Sistema:</strong> ").append(dataConfirmacao).append("</p>");
                adminSb.append("<p>O status da parcela foi atualizado para PAGO automaticamente via integração.</p>");
                adminSb.append("</div>");

                adminHelper.setText(adminSb.toString(), true);
                mailSender.send(adminMsg);
            }

            log.info("E-mails de confirmação enviados para o pedido #{}", pedido.getId());

        } catch (Exception e) {
            log.error("Erro ao enviar e-mails de confirmação para o pedido #{}: {}", pedido.getId(), e.getMessage());
        }
    }

    private JavaMailSenderImpl createMailSender(Configuracao config) {
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
        return mailSender;
    }

    private String buildOrderCanceledEmailContent(Pedido pedido, com.sara.api.model.Usuario responsavel,
            String motivo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataCancelamento = LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).format(formatter);
        String dataOriginal = pedido.getDataPedido().format(formatter);

        StringBuilder sb = new StringBuilder();
        sb.append("<h1 style='color: red;'>Pedido Cancelado!</h1>");
        sb.append("<p><strong>Número do Pedido:</strong> #").append(pedido.getId()).append("</p>");
        sb.append("<p><strong>Cliente:</strong> ").append(pedido.getUsuario().getNome()).append("</p>");
        sb.append("<p><strong>Data do Pedido:</strong> ").append(dataOriginal).append("</p>");

        sb.append("<div style='border: 1px solid black; padding: 10px; margin: 15px 0; display: inline-block;'>");
        sb.append("<strong>Cancelado por: ").append(responsavel.getNome()).append(" em ").append(dataCancelamento)
                .append("</strong>");
        if (motivo != null && !motivo.trim().isEmpty()) {
            sb.append("<br/><strong>Motivo: ").append(motivo).append("</strong>");
        }
        sb.append("</div>");

        sb.append("<p><strong>Observação Orig.:</strong> ")
                .append(pedido.getObservacao() != null ? pedido.getObservacao() : "-").append("</p>");

        sb.append("<h3>Itens do Pedido (Cancelado):</h3>");
        sb.append(buildProductTable(pedido));

        sb.append("<div style='margin-top: 20px;'>");
        sb.append("<p><strong>Frete:</strong> R$ ").append(String.format("%.2f", pedido.getFrete().doubleValue()))
                .append("</p>");
        sb.append("<p><strong>Desconto:</strong> ").append(String.format("%.2f", pedido.getDesconto().doubleValue()))
                .append("%</p>");
        sb.append("<p style='font-size: 1.2em;'><strong>Valor Total:</strong> R$ ")
                .append(String.format("%.2f", pedido.getValorTotal().doubleValue())).append("</p>");
        sb.append("</div>");

        return sb.toString();
    }

    private String buildOrderUpdatedEmailContent(Pedido pedido) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).format(formatter);

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; color: #333;'>");
        sb.append("<h1 style='color: #c5a059;'>Atualização no Seu Pedido!</h1>");
        sb.append("<p>Olá, <strong>").append(pedido.getUsuario().getNome()).append("</strong>.</p>");
        sb.append("<p>Informamos que houveram atualizações no seu pedido <strong>#").append(pedido.getId()).append("</strong>.</p>");
        sb.append("<p><strong>Data da Atualização:</strong> ").append(dataFormatada).append("</p>");
        sb.append("<p><strong>Observação atual:</strong> ")
                .append(pedido.getObservacao() != null ? pedido.getObservacao() : "-").append("</p>");

        sb.append("<h3>Detalhes Atualizados do Pedido:</h3>");
        sb.append(buildProductTable(pedido));

        if (pedido.getPagamentos() != null && !pedido.getPagamentos().isEmpty()) {
            sb.append("<h3>Plano de Pagamento Atualizado:</h3>");
            sb.append(buildPaymentTable(pedido));
        }

        sb.append("<div style='margin-top: 20px;'>");
        sb.append("<p><strong>Frete:</strong> R$ ").append(String.format("%.2f", pedido.getFrete().doubleValue()))
                .append("</p>");
        sb.append("<p><strong>Desconto:</strong> ").append(String.format("%.2f", pedido.getDesconto().doubleValue()))
                .append("%</p>");
        sb.append("<p style='font-size: 1.2em;'><strong>Valor Total:</strong> R$ ")
                .append(String.format("%.2f", pedido.getValorTotal().doubleValue())).append("</p>");
        sb.append("</div>");
        
        sb.append("<p>Para mais detalhes, acesse nosso sistema.</p>");
        sb.append("<p><small>Este é um e-mail automático, por favor não responda.</small></p>");
        sb.append("</div>");

        return sb.toString();
    }

    private String buildOrderEmailContent(Pedido pedido) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = pedido.getDataPedido().format(formatter);

        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Novo Pedido Recebido!</h1>");
        sb.append("<p><strong>Número do Pedido:</strong> #").append(pedido.getId()).append("</p>");
        sb.append("<p><strong>Cliente:</strong> ").append(pedido.getUsuario().getNome()).append("</p>");
        sb.append("<p><strong>Data:</strong> ").append(dataFormatada).append("</p>");
        sb.append("<p><strong>Observação:</strong> ")
                .append(pedido.getObservacao() != null ? pedido.getObservacao() : "-").append("</p>");

        sb.append("<h3>Itens do Pedido:</h3>");
        sb.append(buildProductTable(pedido));

        if (pedido.getPagamentos() != null && !pedido.getPagamentos().isEmpty()) {
            sb.append("<h3>Plano de Pagamento:</h3>");
            sb.append(buildPaymentTable(pedido));
        }

        sb.append("<div style='margin-top: 20px;'>");
        sb.append("<p><strong>Frete:</strong> R$ ").append(String.format("%.2f", pedido.getFrete().doubleValue()))
                .append("</p>");
        sb.append("<p><strong>Desconto:</strong> ").append(String.format("%.2f", pedido.getDesconto().doubleValue()))
                .append("%</p>");
        sb.append("<p style='font-size: 1.2em;'><strong>Valor Total:</strong> R$ ")
                .append(String.format("%.2f", pedido.getValorTotal().doubleValue())).append("</p>");
        sb.append("</div>");

        return sb.toString();
    }

    private String buildProductTable(Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1' style='border-collapse: collapse; width: 60%; font-family: Arial, sans-serif;'>");
        sb.append("<thead><tr style='background-color: #f2f2f2;'>");
        sb.append(
                "<th>Produto</th><th>Tam.</th><th>Qtd</th><th style='text-align: center;'>Unt R$</th><th style='text-align: center;'>Tot R$</th>");
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
            sb.append(
                    "<table style='width: 100%; border: none; border-collapse: collapse; font-family: Arial, sans-serif;'>");
            sb.append("<tr><td style='border: none; padding: 5px 0; text-align: right; width: 50%;'>")
                    .append(vUnitParts[0]).append("</td>");
            sb.append("<td style='border: none; padding: 5px 0; text-align: left; width: 50%;'>,").append(vUnitParts[1])
                    .append("</td>");
            sb.append("</tr></table></td>");

            // Subtotal (Centralização decimal perfeita: vírgula no centro do TD)
            sb.append("<td style='padding: 0;'>");
            sb.append(
                    "<table style='width: 100%; border: none; border-collapse: collapse; font-family: Arial, sans-serif;'>");
            sb.append("<tr><td style='border: none; padding: 5px 0; text-align: right; width: 50%;'>")
                    .append(vTotalParts[0]).append("</td>");
            sb.append("<td style='border: none; padding: 5px 0; text-align: left; width: 50%;'>,")
                    .append(vTotalParts[1]).append("</td>");
            sb.append("</tr></table></td>");

            sb.append("</tr>");
        }

        sb.append("</tbody></table>");
        return sb.toString();
    }

    private String buildOrderConfirmedEmailContent(Pedido pedido) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = pedido.getDataPedido().format(formatter);

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; color: #333;'>");
        sb.append("<h1 style='color: #28a745;'>Pedido Confirmado!</h1>");
        sb.append("<p>Olá, <strong>").append(pedido.getUsuario().getNome()).append("</strong>.</p>");
        sb.append("<p>Seu pedido foi confirmado pelo administrador e já está seguindo para a próxima etapa.</p>");
        sb.append("<p><strong>Número do Pedido:</strong> #").append(pedido.getId()).append("</p>");
        sb.append("<p><strong>Data Original:</strong> ").append(dataFormatada).append("</p>");
        
        if (pedido.getFormaPagamento() != null) {
            sb.append("<p><strong>Forma de Pagamento:</strong> ").append(pedido.getFormaPagamento().getDescricao()).append("</p>");
        }

        sb.append("<h3>Itens do Pedido:</h3>");
        sb.append(buildProductTable(pedido));

        if (pedido.getPagamentos() != null && !pedido.getPagamentos().isEmpty()) {
            sb.append("<h3>Plano de Pagamento:</h3>");
            sb.append(buildPaymentTable(pedido));
        }

        sb.append("<div style='margin-top: 20px;'>");
        sb.append("<p><strong>Frete:</strong> R$ ").append(String.format("%.2f", pedido.getFrete().doubleValue()))
                .append("</p>");
        sb.append("<p><strong>Desconto:</strong> ").append(String.format("%.2f", pedido.getDesconto().doubleValue()))
                .append("%</p>");
        sb.append("<p style='font-size: 1.2em;'><strong>Valor Total:</strong> R$ ")
                .append(String.format("%.2f", pedido.getValorTotal().doubleValue())).append("</p>");
        sb.append("</div>");
        
        sb.append("<p><small>Em caso de dúvidas, entre em contato conosco.</small></p>");
        sb.append("</div>");

        return sb.toString();
    }

    private String buildPaymentTable(Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1' style='border-collapse: collapse; width: 24%; font-family: Arial, sans-serif;'>");
        sb.append("<thead><tr style='background-color: #f2f2f2;'>");
        sb.append("<th style='width: 20%;'>Parc.</th><th style='text-align: center; width: 40%;'>Vencimento</th><th style='text-align: right; width: 40%;'>Valor R$</th>");
        sb.append("</tr></thead><tbody>");

        java.util.List<com.sara.api.model.Pagamento> pagamentos = new java.util.ArrayList<>(pedido.getPagamentos());
        pagamentos.sort(java.util.Comparator.comparing(com.sara.api.model.Pagamento::getDataVencimento, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));

        int i = 1;
        DateTimeFormatter dateOnly = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (com.sara.api.model.Pagamento p : pagamentos) {
            sb.append("<tr>");
            sb.append("<td style='text-align: center; padding: 5px;'>").append(i++).append("ª</td>");
            sb.append("<td style='text-align: center; padding: 5px;'>").append(p.getDataVencimento() != null ? p.getDataVencimento().format(dateOnly) : "-").append("</td>");
            sb.append("<td style='text-align: right; padding: 5px;'>").append(String.format("%.2f", p.getValor().doubleValue()).replace(".", ",")).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</tbody></table>");
        return sb.toString();
    }

    @Async
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void enviarEmailGenerico(String destinatario, String assunto, String conteudoHtml) {
        log.info("Iniciando envio de e-mail genérico para {}", destinatario);

        Configuracao config = configuracaoRepository.findAll().stream().findFirst().orElse(null);
        if (config == null || !Boolean.TRUE.equals(config.getEmailAtivo())) {
            log.info("Envio de e-mail desativado.");
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

            helper.setSubject(assunto);
            helper.setFrom(config.getMailUsername());
            helper.setTo(destinatario);
            helper.setText(conteudoHtml, true);

            mailSender.send(message);
            log.info("E-mail genérico enviado com sucesso para {}", destinatario);

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail genérico para {}: {}", destinatario, e.getMessage());
        }
    }
}
