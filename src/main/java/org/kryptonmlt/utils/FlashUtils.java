package org.kryptonmlt.utils;

import org.kryptonmlt.objects.CacheObject;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public class FlashUtils {
    private FlashUtils() {

    }

    public static ResponseEntity<String> toResponseEntity(CacheObject cacheObject) {
        return new ResponseEntity<String>(cacheObject.getData(), cacheObject.getHeaders(), cacheObject.getStatusCode());
    }

    public static CacheObject toCacheObject(ResponseEntity<String> response) {
        CacheObject cacheObject = new CacheObject();
        cacheObject.setData(response.getBody());
        cacheObject.setHeaders(response.getHeaders());
        cacheObject.setStatusCode(response.getStatusCode());
        return cacheObject;
    }

    public static String getIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        } else {
            String[] ips = ipAddress.split(" ");
            if (ips.length > 1) {
                return ips[ips.length - 1];
            }
        }
        return ipAddress;
    }
}
