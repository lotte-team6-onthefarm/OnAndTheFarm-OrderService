package com.team6.onandthefarmorderservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TccRestAdapter {
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * http://localhost:8081/api/user/product-service/order-try 로 post방식으로 try를 보낸다.
     * @param requestURL : 다음 스탭으로 갈 url(try을 위한 url)
     * @param requestBody :
     * @return response.getBody() 에는 ParticipantLink객체(confirm을 위한 url과 expiretime이 들어있다)가 있다.
     */
    public ParticipantLink doTry(final String requestURL, final Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<ParticipantLink> response = restTemplate.postForEntity(requestURL, new HttpEntity<>(requestBody, headers), ParticipantLink.class);
        if(response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException(String.format("TRY Error[URI : %s][HTTP Status : %s]",
                    requestURL, response.getStatusCode().name()));
        }
        return response.getBody();
    }

    /**
     * 주문 예약에 대한 confirm을 처리하는 메서드
     * @param confirmUrl : http://localhost:8081/api/user/product-service/order-try/{id}
     *                     confirm을 위해 사용되는 url. 이때, id는 reservedOrder의 pk값
     */
    public void stockConfirm(URI confirmUrl) {

        try {
            restTemplate.put(confirmUrl, null);
        } catch (RestClientException e) {
            throw new RuntimeException(String.format("Confirm Error[URI : %s]",
                    confirmUrl.toString()), e);
        }
    }

    public void paymentConfirm(URI confirmUrl) {
        try {
            restTemplate.put(confirmUrl, null);
        } catch (RestClientException e) {
            throw new RuntimeException(String.format("Confirm Error[URI : %s]",
                    confirmUrl.toString()), e);
        }
    }

    public void pointConfirm(URI confirmUrl) {
        try {
            restTemplate.put(confirmUrl, null);
        } catch (RestClientException e) {
            throw new RuntimeException(String.format("Confirm Error[URI : %s]",
                    confirmUrl.toString()), e);
        }
    }

    /**
     * 주문 실패시 예약된 리소스들을 cancel해주는 메서드
     * @param participantLinks
     */
    public void cancelAll(List<ParticipantLink> participantLinks) {
        participantLinks.forEach(participantLink -> {
            try {
                restTemplate.delete(participantLink.getUri());
            } catch (RestClientException e) {
                log.error(String.format("TCC - Cancel Error[URI : %s]", participantLink.getUri().toString()), e);
            }
        });
    }
}
