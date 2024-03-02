package com.scheduler.service;

import com.scheduler.constants.SchedulerConstants;
import com.scheduler.jwt.constants.JwtConstants;
import com.scheduler.jwt.model.JwtRequest;
import com.scheduler.jwt.model.JwtResponse;
import com.scheduler.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {
    private final RestTemplate restTemplate;

    /**
     * Cron Job will get triggered every 15 minutes
     * Calls the user/getUsers API, fetch the Users data and logs it
     *
     * First, It will call /jwt/login API to generate a valid JWT Token by passing Username & Password
     * Then the JWT Bearer Token will be used in the Header to the call the user/getUsers API
     */
    @Scheduled(cron = "${scheduler.cron-expression:0 0/15 * * * ?}")
    public void triggerCronJob() {
        try {
            log.info("Cron Job Scheduler Started, Current Time: {}", System.currentTimeMillis());
            JwtRequest jwtRequest = JwtRequest.builder()
                    .email("Sunny")
                    .password("abc@123")
                    .build();
            ResponseEntity<JwtResponse> jwtResponseEntity = restTemplate.postForEntity(SchedulerConstants.JWT_GENERATION_POST_ENDPOINT_URL, jwtRequest, JwtResponse.class);
            if(jwtResponseEntity.getStatusCode().equals(HttpStatus.OK) && null != jwtResponseEntity.getBody()) {
                String bearerToken = JwtConstants.BEARER + jwtResponseEntity.getBody().getJwtToken();
                log.info("Generated JWT Bearer Token: {}", bearerToken);
                HttpHeaders headers = getHeaders();
                headers.set(JwtConstants.AUTHORIZATION, bearerToken);
                HttpEntity<String> httpEntity = new HttpEntity<>(headers);
                ResponseEntity<List<User>> usersResponseEntity = restTemplate.exchange(SchedulerConstants.USERS_GET_ENDPOINT_URL,
                        HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<User>>(){});
                if(usersResponseEntity.getStatusCode().equals(HttpStatus.OK) && null != usersResponseEntity.getBody()) {
                    log.info("Fetched User Details: {}", usersResponseEntity.getBody().toString());
                    log.info("Cron Job Scheduler Completed, Current Time: {}", System.currentTimeMillis());
                } else {
                    log.error("Exception occurred while fetching User Details: Didn't received a 200 OK Response");
                }
            } else {
                log.error("Exception occurred while generating JWT Token: Invalid Username or Password");
            }
        } catch (Exception ex) {
            log.error("Exception occurred while triggering Cron Job Scheduler: {},{}", ex.getMessage(), ex);
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
