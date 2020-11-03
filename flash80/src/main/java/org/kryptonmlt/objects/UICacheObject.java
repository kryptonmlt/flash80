package org.kryptonmlt.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UICacheObject {

    private float dataMB;
    private String key;
    private HttpStatus statusCode;
    private Date created;
}
