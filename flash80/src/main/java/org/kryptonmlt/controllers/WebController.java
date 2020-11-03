package org.kryptonmlt.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.kryptonmlt.config.ApplicationProps;
import org.kryptonmlt.config.FlashErrorHandler;
import org.kryptonmlt.objects.CacheObject;
import org.kryptonmlt.objects.Flash80Request;
import org.kryptonmlt.services.DeviceService;
import org.kryptonmlt.services.GeoService;
import org.kryptonmlt.services.MemoryCache;
import org.kryptonmlt.utils.FlashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

@RestController
@Slf4j
public class WebController {

    private RestTemplate restTemplate;

    @Autowired
    private ApplicationProps applicationProps;

    @Autowired
    private MemoryCache memoryCache;

    @Autowired
    private GeoService geoService;

    @Autowired
    private DeviceService deviceService;

    public HashMap<String, ApplicationProps.Server> sites = new HashMap<>();

    @PostConstruct
    private void init() {
        final Properties props = System.getProperties();
        props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());
        props.setProperty("jdk.httpclient.allowRestrictedHeaders", "host,connection");
        for (ApplicationProps.Server host : applicationProps.getHosts()) {
            for (String site : host.getSites()) {
                String extraSite;
                if (site.contains("www.")) {
                    extraSite = site.replace("www.", "");
                } else {
                    extraSite = "www." + site;
                }
                sites.put(site, host);
                sites.put(extraSite, host);
            }
        }
        try {
            TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            };
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
            CloseableHttpClient httpClient
                    = HttpClients.custom()
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .setSSLSocketFactory(csf)
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory
                    = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            requestFactory.setReadTimeout(applicationProps.getTimeoutSeconds() * 1000);
            requestFactory.setConnectTimeout(applicationProps.getTimeoutSeconds() * 1000);
            restTemplate = new RestTemplate(requestFactory);
            restTemplate.setErrorHandler(new FlashErrorHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT, RequestMethod.TRACE}
    @RequestMapping(value = "**")
    public ResponseEntity<String> get(HttpServletRequest request, HttpServletResponse servResp) {

        Flash80Request flash80Request = FlashUtils.toFlash80Request(request, geoService.getGeo(FlashUtils.getIp(request)), deviceService.getUserAgent(request));

        if (request.getMethod().equalsIgnoreCase("purge")) {
            if (Arrays.stream(applicationProps.getPurgers()).anyMatch(FlashUtils.getIp(request)::equals)) {
                memoryCache.remove(false, flash80Request);
                return new ResponseEntity<>("PURGE success", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("This IP is not allowed to PURGE", HttpStatus.METHOD_NOT_ALLOWED);
            }
        } else if (request.getMethod().equalsIgnoreCase("ban")) {
            if (Arrays.stream(applicationProps.getPurgers()).anyMatch(FlashUtils.getIp(request)::equals)) {
                memoryCache.remove(true, flash80Request);
                return new ResponseEntity<>("BAN success", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("This IP is not allowed to BAN", HttpStatus.METHOD_NOT_ALLOWED);
            }
        }

        ApplicationProps.Server server = sites.get(flash80Request.getSite());
        if (server != null) {

            log.debug("URL: {} ({})", flash80Request.getFullUrl(server), flash80Request.getSite());

            try {
                if (request.getMethod().equalsIgnoreCase("get") && memoryCache.isCacheable(flash80Request, flash80Request.getHeaderReq())) {

                    CacheObject cacheObject = memoryCache.get(flash80Request);
                    if (cacheObject == null) {
                        ResponseEntity<String> response = performRequest(flash80Request.getFullUrl(server), flash80Request.getHeaderReq(), request.getMethod());
                        memoryCache.save(flash80Request, response);
                        return response;
                    }
                    return FlashUtils.toResponseEntity(cacheObject);
                } else {
                    return performRequest(flash80Request.getFullUrl(server), flash80Request.getHeaderReq(), request.getMethod());
                }
            } catch (Exception e) {
                log.error("error making request to backend {}", server.getHost(), e);
                return new ResponseEntity<>("Error accessing backend", HttpStatus.BAD_GATEWAY);
            }
        }
        log.debug("site {} has no backend associated with it", flash80Request.getSite());
        return new ResponseEntity<>("Site not hosted on this server", HttpStatus.BAD_GATEWAY);
    }

    public ResponseEntity<String> performRequest(String uri, MultiValueMap<String, String> headerReq, String method) {
        HttpEntity<String> entity = new HttpEntity<>("body", headerReq);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.valueOf(method), entity, String.class);
        if (response.getStatusCodeValue() != 200) {
            if (response.getHeaders().get("location") != null) {
                log.debug("Received {} response with redirection: {}", response.getStatusCodeValue(), response.getHeaders().get("location"));
            } else {
                log.debug("Received {} response", response.getStatusCodeValue());
            }

        }
        return response;
    }

}


