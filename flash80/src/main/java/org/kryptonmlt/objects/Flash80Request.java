package org.kryptonmlt.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kryptonmlt.config.ApplicationProps;
import org.springframework.util.MultiValueMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Flash80Request {
    private Geo geo;
    private String site;
    private String scheme;
    private String uri;
    private String requestParams;
    private MultiValueMap<String, String> headerReq;
    private UserAgentInfo userAgentInfo;

    public String getFullUrl(ApplicationProps.Server server){
        int port = server.getHttpsPort();
        if (scheme.equalsIgnoreCase("http")) {
            port = server.getHttpPort();
        }
        return scheme + "://" +   // "http" + "://
                server.getHost() +       // "myhost"
                ":" + port + // ":" + "8080"
                uri +       // "/people"
                requestParams; // "?" + "lastname=Fox&age=30"
    }
}
