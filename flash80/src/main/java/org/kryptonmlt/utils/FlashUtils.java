package org.kryptonmlt.utils;

import org.apache.http.conn.util.InetAddressUtils;
import org.kryptonmlt.objects.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class FlashUtils {

    private FlashUtils() {

    }

    public static ResponseEntity<String> toResponseEntity(CacheObject cacheObject) {
        return new ResponseEntity<>(cacheObject.getData(), cacheObject.getHeaders(), cacheObject.getStatusCode());
    }

    public static Flash80Request toFlash80Request(HttpServletRequest request, Geo geo, UserAgentInfo userAgentInfo) {
        Flash80Request flash80Request = new Flash80Request();
        flash80Request.setUri(request.getRequestURI());

        String possibleHost = request.getRemoteHost();
        if (InetAddressUtils.isIPv4Address(possibleHost) || InetAddressUtils.isIPv6Address(possibleHost)) {
            String hostHeader = request.getHeader("host");
            if (hostHeader != null && !hostHeader.isEmpty()) {
                possibleHost = hostHeader;
            }
        }
        flash80Request.setSite(possibleHost);

        String queryString = (request.getQueryString() != null ? "?" +
                request.getQueryString() : "");
        flash80Request.setRequestParams(queryString);

        flash80Request.setGeo(geo);

        flash80Request.setHeaderReq(FlashUtils.constructRequestHeaders(request));


        flash80Request.setScheme(request.getScheme());

        flash80Request.setUserAgentInfo(userAgentInfo);

        return flash80Request;
    }

    public static CacheObject toCacheObject(ResponseEntity<String> response) {
        CacheObject cacheObject = new CacheObject();
        cacheObject.setData(response.getBody());
        cacheObject.setHeaders(response.getHeaders());
        cacheObject.setStatusCode(response.getStatusCode());
        cacheObject.setCreated(new Date());
        return cacheObject;
    }

    public static UICacheObject toUICacheObject(String key, CacheObject cacheObject) {
        UICacheObject uiCacheObject = new UICacheObject();
        uiCacheObject.setKey(key);
        uiCacheObject.setDataMB(cacheObject.getData().length() * 0.000001f);
        uiCacheObject.setStatusCode(cacheObject.getStatusCode());
        uiCacheObject.setCreated(cacheObject.getCreated());
        return uiCacheObject;
    }

    public static List<UICacheObject> toUICacheObject(Map<String, CacheObject> cache) {
        List<UICacheObject> uiCacheObjects = new ArrayList<>();
        Set<String> keys = new HashSet<>(cache.keySet());
        for (String key : keys) {
            CacheObject cacheObject = cache.get(key);
            if (cacheObject != null) {
                uiCacheObjects.add(FlashUtils.toUICacheObject(key, cacheObject));
            }
        }
        return uiCacheObjects;
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

    public static MultiValueMap<String, String> constructRequestHeaders(HttpServletRequest request) {

        MultiValueMap<String, String> headerReq = new LinkedMultiValueMap<>();
        boolean foundHost = false;
        Iterator<String> iter = request.getHeaderNames().asIterator();
        while (iter.hasNext()) {
            String key = iter.next();
            String value = request.getHeader(key);
            List<String> vals = new ArrayList<>();
            if (key.equalsIgnoreCase("host")) {
                vals.add(request.getServerName());
                foundHost = true;
            } else {
                vals.add(value);
            }
            headerReq.put(key, vals);
        }
        if (!foundHost) {
            List<String> vals = new ArrayList<>();
            vals.add(request.getServerName());
            headerReq.put("host", vals);
        }
        return headerReq;
    }
}
