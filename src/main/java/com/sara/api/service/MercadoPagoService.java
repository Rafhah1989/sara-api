package com.sara.api.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.sara.api.model.Pagamento;
import com.sara.api.model.Pedido;
import com.sara.api.model.Usuario;
import com.sara.api.repository.PagamentoRepository;
import com.sara.api.repository.PedidoRepository;
import jakarta.annotation.PostConstruct;

import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class MercadoPagoService {

    @Value("${MP_ACCESS_TOKEN}")
    private String accessToken;

    @Value("${URL_WEBHOOK:http://localhost:8080}")
    private String urlWebhook;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    @Lazy
    private PedidoService pedidoService;

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new RuntimeException("A variável de ambiente MP_ACCESS_TOKEN deve ser configurada.");
        }
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    @Scheduled(fixedDelay = 30000) // Executa a cada 30 segundos
    @Transactional
    public void verificarPagamentosPixPendentes() {
        try {
            OffsetDateTime agora = OffsetDateTime.now();
            List<Pagamento> pendentes = pagamentoRepository
                .findAllByPagoFalseAndPagamentoOnlineTrueAndMercadopagoPagamentoIdIsNotNullAndDataExpiracaoPixAfter(agora);
            
            if (!pendentes.isEmpty()) {
                System.out.println(">>> [POLLING] Iniciando varredura de " + pendentes.size() + " pagamentos PIX pendentes...");
                for (Pagamento p : pendentes) {
                    try {
                        verificarStatusPagamento(p.getMercadopagoPagamentoId());
                    } catch (Exception e) {
                        System.err.println(">>> [POLLING] Erro ao verificar pagamento #" + p.getId() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [POLLING] Erro crítico na varredura de pagamentos: " + e.getMessage());
        }
    }

    @Async
    @Transactional
    public void processarNotificacaoAsync(Map<String, Object> payload, Map<String, String> params) {
        try {
            System.out.println("--- NOVO WEBHOOK MERCADO PAGO (STR) ---");
            System.out.println("Params: " + params);
            System.out.println("Payload: " + payload);
            
            String paymentId = null;

            // 1. Tenta extrair do payload JSON pré-processado pela Spring (v2)
            if (payload != null && !payload.isEmpty()) {
                try {
                    // Prioridade 1: data.id (Padrão atual v1.1+)
                    if (payload.containsKey("data")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) payload.get("data");
                        if (data != null && data.get("id") != null) {
                            paymentId = data.get("id").toString();
                        }
                    }
                    
                    // Prioridade 2: id na raiz (Padrão v1.0 ou legado)
                    if (paymentId == null && payload.containsKey("id")) {
                        Object idObj = payload.get("id");
                        // Verifica se não é o ID da própria notificação (geralmente numérico)
                        if (idObj != null) paymentId = idObj.toString();
                    }

                    if (payload.containsKey("action")) {
                        String action = (String) payload.get("action");
                        System.out.println("Ação detectada: " + action);
                        
                        if ("payment.created".equals(action) && paymentId != null) {
                            System.out.println("Notificação de criação recebida. Verificando status atual por segurança...");
                        } else if ("payment.updated".equals(action)) {
                            System.out.println("Notificação de atualização recebida.");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao extrair dados do payload do Webhook: " + e.getMessage());
                }
            }

            // 2. Se não achou no JSON, tenta nos params (Query Params legado / v1)
            if (paymentId == null && params != null) {
                if (params.containsKey("id")) {
                    paymentId = params.get("id");
                } else if (params.containsKey("data.id")) {
                    paymentId = params.get("data.id");
                }
            }

            if (paymentId != null && !paymentId.isEmpty()) {
                System.out.println("ID identificado para sincronia: " + paymentId);
                verificarStatusPagamento(paymentId);
            } else {
                System.out.println("ALERTA: Webhook sem ID de pagamento identificado.");
            }
        } catch (Exception e) {
            System.err.println("CRÍTICO: Erro no processamento do Webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    public String verificarStatusPagamento(String paymentId) {
        try {
            Payment payment = buscarPagamento(paymentId);
            System.out.println("Consulta Mercado Pago - Pagamento " + paymentId + ": Status=" + payment.getStatus());
            
            if ("approved".equals(payment.getStatus())) {
                Pagamento pagamento = pagamentoRepository.findByMercadopagoPagamentoId(paymentId)
                        .orElse(null);
                
                if (pagamento != null) {
                    if (Boolean.TRUE.equals(pagamento.getPago())) {
                        System.out.println("Pagamento #" + pagamento.getId() + " já consta como Pago. Nenhuma ação necessária.");
                    } else {
                        pagamento.setPago(true);
                        pagamentoRepository.save(pagamento);
                        System.out.println("SUCESSO: Pagamento #" + pagamento.getId() + " marcado como PAGO via Webhook.");
                        
                        // Atualiza o status do pedido
                        pedidoService.syncStatusPedido(pagamento.getPedido());
                        pedidoRepository.save(pagamento.getPedido());

                        // Envia e-mails de confirmação
                        emailService.enviarEmailPagamentoConfirmado(pagamento.getPedido(), pagamento);
                    }
                } else {
                    System.err.println("ALERTA: Recebido pagamento aprovado do MP (" + paymentId + ") mas o ID não foi encontrado no nosso banco de dados.");
                }
            }
            return payment.getStatus();
        } catch (Exception e) {
            System.err.println("Erro ao verificar status: " + e.getMessage());
            return "Erro: " + e.getMessage();
        }
    }

    public Payment criarPagamentoPix(Pagamento pagamento) throws MPException, MPApiException {
        Pedido pedido = pagamento.getPedido();
        Usuario user = pedido.getUsuario();
        
        PaymentClient client = new PaymentClient();

        PaymentCreateRequest.PaymentCreateRequestBuilder builder = PaymentCreateRequest.builder()
                .description("Pedido #" + pedido.getId() + " - Parcela #" + pagamento.getId() + " - Sara System")
                .transactionAmount(pagamento.getValor())
                .paymentMethodId("pix")
                .notificationUrl(urlWebhook)
                .dateOfExpiration(OffsetDateTime.now().plusMinutes(15));

        String email = user.getEmail();
        if (email == null || email.isBlank() || !email.contains("@")) {
            // Mercado Pago exige um e-mail válido. Se o usuário não tiver, usamos um fallback corporativo ou do sistema
            email = "financeiro@sarasystem.com.br"; // Exemplo de fallback
        }

        String nome = user.getNome();
        if (nome == null || nome.isBlank()) {
            nome = "Cliente Sara";
        }

        PaymentPayerRequest.PaymentPayerRequestBuilder payerBuilder = PaymentPayerRequest.builder()
                .email(email)
                .firstName(nome);

        if (user.getCpfCnpj() != null && !user.getCpfCnpj().replaceAll("\\D", "").isBlank()) {
            String numbersOnly = user.getCpfCnpj().replaceAll("\\D", "");
            if (numbersOnly.length() >= 11) {
                payerBuilder.identification(com.mercadopago.client.common.IdentificationRequest.builder()
                        .type(numbersOnly.length() > 11 ? "CNPJ" : "CPF")
                        .number(numbersOnly)
                        .build());
            }
        }

        PaymentCreateRequest request = builder.payer(payerBuilder.build()).build();

        return client.create(request);
    }

    public Payment buscarPagamento(String paymentId) throws MPException, MPApiException {
        PaymentClient client = new PaymentClient();
        return client.get(Long.parseLong(paymentId));
    }
}
