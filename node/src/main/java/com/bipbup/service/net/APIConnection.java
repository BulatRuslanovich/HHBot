package com.bipbup.service.net;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public interface APIConnection {

    HttpEntity<HttpHeaders> createRequestWithHeaders();
}
