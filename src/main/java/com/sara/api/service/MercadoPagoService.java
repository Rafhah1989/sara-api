package com.sara.api.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.sara.api.model.Pedido;
import com.sara.api.model.Usuario;
import com.sara.api.repository.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MercadoPagoService {

    @Value("${MP_ACCESS_TOKEN}")
    private String accessToken;

    @Value("${URL_WEBHOOK:http://localhost:8080}")
    private String urlWebhook;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new RuntimeException("A variável de ambiente MP_ACCESS_TOKEN deve ser configurada.");
        }
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    @Async
    public void processarNotificacaoAsync(String body, Map<String, String> params) {
        try {
            System.out.println("Iniciando processamento assíncrono do webhook...");
            String paymentId = null;

            // Tenta extrair do body JSON (V2)
            if (body != null && body.trim().startsWith("{")) {
                try {
                    Map<String, Object> payload = objectMapper.readValue(body, Map.class);
                    if (payload.containsKey("action")) {
                        String action = (String) payload.get("action");
                        if ("payment.updated".equals(action) || "payment.created".equals(action)) {
                            Map<String, Object> data = (Map<String, Object>) payload.get("data");
                            if (data != null && data.get("id") != null) {
                                paymentId = data.get("id").toString();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao fazer parse do JSON: " + e.getMessage());
                }
            }

            // Se não achou no JSON, tenta nos params (V1/Legacy)
            if (paymentId == null) {
                if ("payment".equals(params.get("topic")) || "payment".equals(params.get("type"))) {
                    paymentId = params.get("id");
                    if (paymentId == null) paymentId = params.get("data.id");
                }
            }

            if (paymentId != null) {
                verificarStatusPagamento(paymentId);
            } else {
                System.out.println("Nenhum ID de pagamento identificado na notificação.");
            }
        } catch (Exception e) {
            System.err.println("Erro no processamento assíncrono: " + e.getMessage());
        }
    }

    public String verificarStatusPagamento(String paymentId) {
        try {
            System.out.println("Verificando status do pagamento: " + paymentId);
            Payment payment = buscarPagamento(paymentId);
            if ("approved".equals(payment.getStatus())) {
                Pedido pedido = pedidoRepository.findByMercadopagoPagamentoId(paymentId)
                        .orElse(null);
                
                if (pedido != null && (pedido.getPago() == null || !pedido.getPago())) {
                    pedido.setPago(true);
                    pedidoRepository.save(pedido);
                    System.out.println("Pedido #" + pedido.getId() + " marcado como PAGO.");
                }
            } else {
                System.out.println("Status do pagamento: " + payment.getStatus());
            }
            return payment.getStatus();
        } catch (Exception e) {
            System.err.println("Erro ao verificar status: " + e.getMessage());
            return "Erro: " + e.getMessage();
        }
    }

    public Payment criarPagamentoPix(Pedido pedido) throws MPException, MPApiException {
        Usuario user = pedido.getUsuario();
        
        PaymentClient client = new PaymentClient();

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .description("Pedido #" + pedido.getId() + " - Sara System")
                .transactionAmount(pedido.getValorTotal())
                .paymentMethodId("pix")
                .notificationUrl(urlWebhook) // Placeholder, deve ser configurável ou vir de env
                .payer(PaymentPayerRequest.builder()
                        .email(user.getEmail())
                        .firstName(user.getNome())
                        .identification(com.mercadopago.client.common.IdentificationRequest.builder()
                                .type(user.getCpfCnpj().length() > 11 ? "CNPJ" : "CPF")
                                .number(user.getCpfCnpj().replaceAll("\\D", ""))
                                .build())
                        .build())
                .dateOfExpiration(OffsetDateTime.now().plusMinutes(15))
                .build();

        return client.create(request);
    }

    public Payment buscarPagamento(String paymentId) throws MPException, MPApiException {
        PaymentClient client = new PaymentClient();
        return client.get(Long.parseLong(paymentId));
    }
}
