package com.example.deliver.domain.payment.client;

import com.example.deliver.domain.payment.dto.TossPaymentConfirmRequest;
import com.example.deliver.domain.payment.dto.TossPaymentConfirmResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class TossPaymentClient { //Toss API 호출하는 클래스. 결제 저장은 PaymentService에서 한다.

    //Toss 승인 API 호출 실패 시 원인 확인이 쉽도록 로그 추가.
    private static final Logger log = LoggerFactory.getLogger(TossPaymentClient.class);
    //Toss 결제 승인 API 주소
    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final RestTemplate restTemplate = new RestTemplate();

    //application.yml에 있는 Toss Secret Key 가져오기.
    @Value("${toss.payments.secret-key}")
    private String secretKey;

    //Toss 승인 요청
    public TossPaymentConfirmResponse confirm(TossPaymentConfirmRequest request) {
        //HTTP Header 생성.
        HttpHeaders headers = new HttpHeaders();
        //Content-Type을 application/json으로 설정.
        headers.setContentType(MediaType.APPLICATION_JSON);
        //Authorization 헤더에 Toss Secret Key 인증값 추가.
        headers.set(HttpHeaders.AUTHORIZATION, createAuthorizationHeader());

        //요청 Body에 paymentKey, orderId, amount 담기.
        HttpEntity<TossPaymentConfirmRequest> httpEntity = new HttpEntity<>(request, headers);

        //Toss confirm API로 POST 요청
        try {
            ResponseEntity<TossPaymentConfirmResponse> response = restTemplate.postForEntity(
                    CONFIRM_URL,
                    httpEntity,
                    TossPaymentConfirmResponse.class
            );

            //Toss 응답이 2xx가 아니면 실패 or 응답 Body가 없으면 실패.
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Toss confirm request failed. statusCode={}", response.getStatusCode());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Toss 결제 승인에 실패했습니다.");
            }

            if (response.getBody() == null) {
                log.warn("Toss confirm response body is empty. statusCode={}", response.getStatusCode());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Toss 결제 승인에 실패했습니다.");
            }

            return response.getBody();
        }  catch (HttpStatusCodeException e) {
            log.warn(
                    "Toss confirm request failed. statusCode={}, responseBody={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Toss 결제 승인에 실패했습니다.", e);
        } catch (RestClientException e) {//HTTP 통신 중 예외가 나도 실패.
            log.warn("Toss confirm request failed. message={}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Toss 결제 승인에 실패했습니다.", e);
        }
    }

    //Authorization 헤더 생성
    private String createAuthorizationHeader() {
        //Toss API는 Secret Key를 Basic 인증 방식으로 보냄.
        String credential = secretKey + ":";
        String encodedCredential = Base64.getEncoder() //Base64 인코딩
                .encodeToString(credential.getBytes(StandardCharsets.UTF_8));

        //ex) Basic dGVzdF9za19hYmNkOg==
        return "Basic " + encodedCredential;
    }
}