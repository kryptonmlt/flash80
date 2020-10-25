package org.kryptonmlt.services;

import lombok.extern.slf4j.Slf4j;
import org.kryptonmlt.config.ApplicationProps;
import org.kryptonmlt.objects.CacheObject;
import org.kryptonmlt.objects.Geo;
import org.kryptonmlt.utils.FlashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@Slf4j
public class MemoryCache {

    private HashMap<String, CacheObject> cache = new HashMap<>();

    @Autowired
    private ApplicationProps applicationProps;

    private long cacheBustMS;

    @PostConstruct
    public void init() {
        cacheBustMS = (applicationProps.getCache().getHours() * 60 * 60 * 1000) + (applicationProps.getCache().getMinutes() * 60 * 1000);
    }

    @Scheduled(fixedDelayString = "${flash80.cache.cronmilliseconds}", initialDelay = 1000 * 60)
    public void cacheClearer() {
        log.debug("Starting cache cleaning");
        List<String> keysToDelete = new ArrayList<>();
        for (String key : cache.keySet()) {
            if ((cache.get(key).getCreated().getTime() - cacheBustMS) > 0) {
                keysToDelete.add(key);
            }
        }
        log.debug("Deleting {} objects from cache", keysToDelete.size());
        // LOCK cache
        for (String key : keysToDelete) {
            cache.remove(key);
        }

    }

    public boolean isCacheable(String site, String uri, String requestParams, MultiValueMap<String, String> headers) {
        String url = site + uri + requestParams;

        if (FlashUtils.isMatch(url, applicationProps.getIncludes().getUrls())
                || FlashUtils.isMatch(headers, applicationProps.getIncludes().getHeaders())
                || FlashUtils.isMatch(headers, applicationProps.getIncludes().getCookies())) {
            if (!FlashUtils.isMatch(url, applicationProps.getExcludes().getUrls())
                    && !FlashUtils.isMatch(headers, applicationProps.getExcludes().getHeaders())
                    && !FlashUtils.isMatch(headers, applicationProps.getExcludes().getCookies())) {
                return true;
            }
        }
        return false;
    }

    public CacheObject get(Geo geo, String site, String uri, String requestParams, MultiValueMap<String, String> headers) {
        String key = this.buildCacheKey(geo, site, uri, requestParams);
        return cache.get(key);
    }

    public CacheObject save(Geo geo, String site, String uri, String requestParams, ResponseEntity<String> response) {

        if (cache.size() > applicationProps.getCache().getLimit()) {
            return null;
        }

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
