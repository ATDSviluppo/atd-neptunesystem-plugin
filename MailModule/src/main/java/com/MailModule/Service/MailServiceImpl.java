package com.MailModule.Service;

import com.MailModule.Entity.EmployeeMailConstructor;
import com.MailModule.Properties.MailProperties;
import com.MailModule.Repository.MailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MailServiceImpl implements MailService {
    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MailProperties mailProperties;

    @Override
    public List<EmployeeMailConstructor> findByEmailNotNullOrEmpty() {
        return mailRepository.findByEmailNotNullOrEmpty();
    }

    @Override
    public boolean setupEmail(String employeeId, String email) {
        int count = mailRepository.setupEmail(employeeId, email);
        return count > 0;
    }

    @Scheduled(
            cron = "0 0 9 * * *",
            zone = "Europe/Rome"
    )
    @Override
    public List<String> getEmailToSendExpirationAdvice()
            throws JsonProcessingException {

        Date currentDate = Date.valueOf(LocalDate.now());
        String url = mailProperties.getUrl() + "/api/sendMail";
        int daysBeforeExpiration = mailProperties.getDaysBeforeExpiration();

        List<String> toAddress = mailRepository.getEmailToSendExpirationAdvice(
                daysBeforeExpiration,
                currentDate
        );

        String body = mailProperties.getBody();
        String subject = mailProperties.getSubject();

        Map<String, Object> request = new HashMap<>();
        request.put("Body", body);
        request.put("Subject", subject);
        request.put("ToAddress", toAddress);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(request);

        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(
                                X509Certificate[] certs,
                                String authType) {
                        }

                        @Override
                        public void checkServerTrusted(
                                X509Certificate[] certs,
                                String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                    null,
                    trustAllCerts,
                    new SecureRandom()
            );

            HttpClient httpClient = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                log.info(
                        "Email inviata con successo ai seguenti destinatari {}",
                        toAddress
                );
            } else {
                log.error(
                        "Server ha restituito: {} - {}",
                        response.statusCode(),
                        response.body()
                );
            }

        } catch (Exception e) {
            log.error("Errore durante l'invio della mail a {}", url, e);
        }

        return toAddress;
    }
}
