package org.kryptonmlt.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.kryptonmlt.config.ApplicationProps;
import org.kryptonmlt.config.FlashErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@RestController
@Slf4j
public class WebController {

    private RestTemplate restTemplate;
    private CloseableHttpClient httpClient;

    @Autowired
    private ApplicationProps applicationProps;

    public HashMap<String, ApplicationProps.Server> sites = new HashMap<>();

    @PostConstruct
    private void init() {
        final Properties props = System.getProperties();
        //-Djdk.httpclient.allowRestrictedHeaders=host,connection
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
            httpClient
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

    @RequestMapping(value = "**", method = RequestMethod.GET)
    public ResponseEntity<String> get(HttpServletRequest request, HttpServletResponse servResp) {
        String possibleHost = request.getRemoteHost();
        if (InetAddressUtils.isIPv4Address(possibleHost) || InetAddressUtils.isIPv6Address(possibleHost)) {
            String hostHeader = request.getHeader("host");
            if (hostHeader != null && !hostHeader.isEmpty()) {
                possibleHost = hostHeader;
            }
        }
        ApplicationProps.Server server = sites.get(possibleHost);
        if (server != null) {
            int port = server.getHttpsPort();
            if (request.getScheme().equalsIgnoreCase("http")) {
                port = server.getHttpPort();
            }
            String uri = request.getScheme() + "://" +   // "http" + "://
                    server.getHost() +       // "myhost"
                    ":" + port + // ":" + "8080"
                    request.getRequestURI() +       // "/people"
                    (request.getQueryString() != null ? "?" +
                            request.getQueryString() : ""); // "?" + "lastname=Fox&age=30"
            log.debug("URL: " + uri);
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
            try {

                HttpEntity<String> entity = new HttpEntity<String>("body", headerReq);
                ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
                if (response.getStatusCodeValue() != 200) {
                    if (response.getHeaders().get("location") != null) {
                        log.debug("Received {} response with redirection: {}",response.getStatusCodeValue(),response.getHeaders().get("location"));
                    }else{
                        log.debug("Received {} response",response.getStatusCodeValue());
                    }

                }
                return response;
            } catch (Exception e) {
                log.debug("error making request to backend {}", server.getHost(), e);
                return new ResponseEntity<>("Error accessing backend", HttpStatus.BAD_GATEWAY);
            }
        }
        log.debug("site {} has no backend associated with it", possibleHost);
        return new ResponseEntity<>("Site not hosted on this server", HttpStatus.BAD_GATEWAY);
    }
}


