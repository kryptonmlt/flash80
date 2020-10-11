package org.kryptonmlt.services;

import org.kryptonmlt.config.ApplicationProps;
import org.kryptonmlt.objects.CacheObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class MemoryCache {

    private HashMap<String, CacheObject> cache = new HashMap<>();

    @Autowired
    private ApplicationProps applicationProps;

    public CacheObject get(String geo, String site, String uri, String requestParams, Map<String, String> headers, String cookie) {

        // check if it should be cached
        String url = site + uri + requestParams;
        if (isMatch(url, applicationProps.getIncludes().getUrls()) ||
                isMatch(headers.keySet(), applicationProps.getIncludes().getHeaders()) ||
                isMatch(cookie, applicationProps.getIncludes().getCookies())) {
            if (!isMatch(url, applicationProps.getExcludes().getUrls()) &&
                    !isMatch(headers.keySet(), applicationProps.getExcludes().getHeaders()) &&
                    !isMatch(cookie, applicationProps.getExcludes().getCookies())) {

                // attempt to get from cache
                StringBuilder key = new StringBuilder();
                if (applicationProps.getCache().isGeo()) {
                    key.append(geo);
                }
                if (applicationProps.getCache().isSite()) {
                    key.append(site);
                }
                if (applicationProps.getCache().isUri()) {
                    key.append(uri);
                }
                if (applicationProps.getCache().isRequestParams()) {
                    key.append(requestParams);
                }
                if (applicationProps.getCache().getHeader() != null && applicationProps.getCache().getHeader().length > 0) {
                    for (String h : applicationProps.getCache().getHeader()) {
                        if (headers.get(h) != null) {
                            key.append(headers.get(h));
                        }
                    }
                }
                if (applicationProps.getCache().getCookie() != null && applicationProps.getCache().getCookie().length > 0) {
                    for (String c : applicationProps.getCache().getCookie()) {
                        if (cookie != null) {
                            key.append(headers.get(c));
                        }
                    }
                }

                return cache.get(key);
            }
        }
        return null;
    }

    private boolean isMatch(String word, String[] list) {
        if (list.length > 0) {
            for (String toCheck : list) {
                if (word.matches(toCheck)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMatch(Set<String> words, String[] list) {
        for (String word : words) {
            if (isMatch(word, list)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMatch(String[] words, String[] list) {
        for (String word : words) {
            if (isMatch(word, list)) {
                return true;
            }
        }
        return false;
    }
}
