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

    @Override
    public HttpEntity<HttpHeaders> createRequestWithHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("User-Agent", userAgent);
        return new HttpEntity<>(headers);
    }
}
