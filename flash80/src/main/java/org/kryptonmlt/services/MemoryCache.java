package org.kryptonmlt.services;

import lombok.extern.slf4j.Slf4j;
import org.kryptonmlt.config.ApplicationProps;
import org.kryptonmlt.objects.CacheObject;
import org.kryptonmlt.objects.Flash80Request;
import org.kryptonmlt.objects.Geo;
import org.kryptonmlt.utils.FlashUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
        for (Map.Entry<String, CacheObject> key : cache.entrySet()) {
            if (System.currentTimeMillis() - (key.getValue().getCreated().getTime() + cacheBustMS) > 0) {
                keysToDelete.add(key.getKey());
            }
        }
        log.debug("Deleting {} objects from cache", keysToDelete.size());
        // LOCK cache
        for (String key : keysToDelete) {
            cache.remove(key);
        }

    }

    public HashMap<String, CacheObject> getCache() {
        return cache;
    }

    public boolean isCacheable(Flash80Request flash80Request, MultiValueMap<String, String> headers) {
        String url = flash80Request.getSite() + flash80Request.getUri() + flash80Request.getRequestParams();

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

    public CacheObject get(Flash80Request flash80Request) {
        String key = this.buildCacheKey(flash80Request);
        return cache.get(key);
    }

    public CacheObject save(Flash80Request flash80Request, ResponseEntity<String> response) {

        if (cache.size() > applicationProps.getCache().getLimit()) {
            return null;
        }

        if (this.isCacheable(flash80Request, response.getHeaders())) {
            String key = this.buildCacheKey(flash80Request);
            return cache.put(key, FlashUtils.toCacheObject(response));
        }
        return null;
    }

    public String buildCacheKey(Flash80Request flash80Request) {

        StringBuilder key = new StringBuilder();
        if (applicationProps.getCache().getGeo().isContinent()) {
            key.append(flash80Request.getGeo().getContinent());
        }
        if (applicationProps.getCache().getGeo().isCountry()) {
            key.append(flash80Request.getGeo().getCountry());
        }
        if (applicationProps.getCache().getGeo().isRegion()) {
            key.append(flash80Request.getGeo().getRegion());
        }
        if (applicationProps.getCache().isSite()) {
            key.append(flash80Request.getSite());
        }
        if (applicationProps.getCache().isUri()) {
            key.append(flash80Request.getUri());
        }
        if (applicationProps.getCache().isRequestParams()) {
            key.append(flash80Request.getRequestParams());
        }
        if (applicationProps.getCache().isDevice()) {
            key.append(flash80Request.getUserAgentInfo().getDevice());
        }
        if (applicationProps.getCache().isBrowser()) {
            key.append(flash80Request.getUserAgentInfo().getBrowser());
        }
        return key.toString().replaceAll("/", "-").replaceAll("\\.", "-");
    }

    public void remove(boolean matchAll, Flash80Request flash80Request) {
        String key = this.buildCacheKey(flash80Request);
        if (matchAll) {
            Set<String> keys = new HashSet<>(cache.keySet());
            for (String k : keys) {
                if (k.contains(key)) {
                    cache.remove(k);
                }
            }
        } else {
            cache.remove(key);
        }
    }
}
