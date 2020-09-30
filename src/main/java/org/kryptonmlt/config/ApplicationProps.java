package org.kryptonmlt.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "application")
@Data
@NoArgsConstructor
public class ApplicationProps {

    private List<Map<String, Object>> props;
    private List<String> excludes;
    private List<String> includes;
    private List<Server> hosts;

    @Data
    @NoArgsConstructor
    public static class Server {

        private String host;
        private int httpPort;
        private int httpsPort;
        private List<String> sites;

    }

}
