package org.kryptonmlt.utils;

import org.kryptonmlt.objects.CacheObject;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

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
        cacheObject.setCreated(new Date());
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

    public static boolean isMatch(String word, String[] list) {
        if (list.length > 0) {
            for (String toCheck : list) {
                if (word.matches(toCheck)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMatch(String[] words, String[] list) {
        for (String word : words) {
            if (isMatch(word, list)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMatch(MultiValueMap<String, String> headers, String[] list) {
        for (String header : headers.keySet()) {
            List<String> headerContents = headers.get(header);
            String[] itemsArray = new String[headerContents.size()];
            if (isMatch(headerContents.toArray(itemsArray), list)) {
                return true;
            }
        }
        return false;
    }
}
