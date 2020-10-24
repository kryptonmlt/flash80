package org.kryptonmlt.objects;

import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;


@Data
public class CacheObject {

    private String data;
    private HttpHeaders headers;
    private HttpStatus statusCode;
}
