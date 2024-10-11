package com.bipbup.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public interface APIConnection {

    HttpEntity<HttpHeaders> createRequestWithHeaders();
}
