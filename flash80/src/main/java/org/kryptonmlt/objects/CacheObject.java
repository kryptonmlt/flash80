package org.kryptonmlt.objects;

import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Date;


@Data
public class CacheObject {

    private String data;
    private HttpHeaders headers;
    private HttpStatus statusCode;
    private Date created;
}
