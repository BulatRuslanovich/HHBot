package com.bipbup.service.impl;

import com.bipbup.service.APIConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;


@Service
public class APIConnectionImpl implements APIConnection {
    @Value("${headhunter.user-agent}")
    private String userAgent;
    @Value("${headhunter.token}")
    private String token;

    @Override
    public HttpEntity<HttpHeaders> createRequestWithHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("User-Agent", userAgent);
//        headers.set("Authorization", "Bearer " + token);
        headers.add("User-Agent", userAgent);
        headers.add("Bearer", token);
        return new HttpEntity<>(headers);
    }
}
