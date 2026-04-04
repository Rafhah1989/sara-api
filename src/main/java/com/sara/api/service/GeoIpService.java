package com.sara.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@Slf4j
public class GeoIpService {

    private final RestTemplate restTemplate;

    public GeoIpService() {
        this.restTemplate = new RestTemplate();
    }

    public String obterLocalizacao(String ip) {
        if (ip == null || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return "Local (Desenvolvimento)";
        }

        try {
            String url = "http://ip-api.com/json/" + ip + "?fields=status,message,country,city,regionName";
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                String city = (String) response.get("city");
                String country = (String) response.get("country");
                return city + ", " + country;
            }
        } catch (Exception e) {
            log.warn("Falha ao identificar localização para o IP {}: {}", ip, e.getMessage());
        }

        return "Local Indisponível";
    }
}
