package com.bipbup.service.api;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public interface APIConnection {

    HttpEntity<HttpHeaders> createRequestWithHeaders();
}
