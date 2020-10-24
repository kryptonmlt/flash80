package org.kryptonmlt.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.kryptonmlt.objects.Geo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "flash80")
@Data
@NoArgsConstructor
public class ApplicationProps {

    private int timeoutSeconds;
    private Cache cache;
    private CacheRules excludes;
    private CacheRules includes;
    private List<Server> hosts;

    @Data
    @NoArgsConstructor
    public static class Server {

        private String host;
        private int httpPort;
        private int httpsPort;
        private List<String> sites;

    }

    @Data
    @NoArgsConstructor
    public static class Cache {

        private int minutes;
        private int hours;
        private int limit;
        private GeoProps geo;
        private boolean site;
        private boolean uri;
        private boolean requestParams;

    }

    @Data
    @NoArgsConstructor
    public static class GeoProps {

        private boolean continent;
        private boolean country;
        private boolean region;

    }

    @Data
    @NoArgsConstructor
    public static class CacheRules {

        private String[] urls;
        private String[] headers;
        private String[] cookies;

    }

}
