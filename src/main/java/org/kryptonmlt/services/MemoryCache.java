package org.kryptonmlt.services;

import org.kryptonmlt.config.ApplicationProps;
import org.kryptonmlt.objects.CacheObject;
import org.kryptonmlt.objects.Geo;
import org.kryptonmlt.utils.FlashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MemoryCache {

    private HashMap<String, CacheObject> cache = new HashMap<>();

    @Autowired
    private ApplicationProps applicationProps;

    // TODO: IMPLEMENT LIMIT AND CACHE EXPIRY

    public boolean isCacheable(String site, String uri, String requestParams, MultiValueMap<String, String> headers) {
        String url = site + uri + requestParams;

        if (isMatch(url, applicationProps.getIncludes().getUrls())
                || isMatch(headers, applicationProps.getIncludes().getHeaders())
                || isMatch(headers, applicationProps.getIncludes().getCookies())) {
            if (!isMatch(url, applicationProps.getExcludes().getUrls())
                    || !isMatch(headers, applicationProps.getExcludes().getHeaders())
                    || !isMatch(headers, applicationProps.getExcludes().getCookies())) {
                return true;
            }
        }
        return false;
    }

    public CacheObject get(Geo geo, String site, String uri, String requestParams, MultiValueMap<String, String> headers) {
        String key = this.buildCacheKey(geo, site, uri, requestParams);
        return cache.get(key);
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

    private boolean isMatch(String[] words, String[] list) {
        for (String word : words) {
            if (isMatch(word, list)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMatch(MultiValueMap<String, String> headers, String[] list) {
        for (String header : headers.keySet()) {
            List<String> headerContents = headers.get(header);
            String[] itemsArray = new String[headerContents.size()];
            if (isMatch(headerContents.toArray(itemsArray), list)) {
                return true;
            }
        }
        return false;
    }


    public CacheObject save(Geo geo, String site, String uri, String requestParams, ResponseEntity<String> response) {

        if (this.isCacheable(site, uri, requestParams, response.getHeaders())) {
            String key = this.buildCacheKey(geo, site, uri, requestParams);
            return cache.put(key, FlashUtils.toCacheObject(response));
        }
        return null;
    }

    public String buildCacheKey(Geo geo, String site, String uri, String requestParams) {

        StringBuilder key = new StringBuilder();
        if (applicationProps.getCache().getGeo().isContinent()) {
            key.append(geo.getContinent());
        }
        if (applicationProps.getCache().getGeo().isCountry()) {
            key.append(geo.getCountry());
        }
        if (applicationProps.getCache().getGeo().isRegion()) {
            key.append(geo.getRegion());
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
        return key.toString();
    }
}
