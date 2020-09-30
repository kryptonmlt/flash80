package org.kryptonmlt.config;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class FlashErrorHandler implements ResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return true;
    }
}