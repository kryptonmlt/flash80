package org.kryptonmlt.services;

import com.blueconic.browscap.BrowsCapField;
import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import lombok.extern.slf4j.Slf4j;
import org.kryptonmlt.config.ApplicationProps;
import org.kryptonmlt.objects.UserAgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Component
@Slf4j
public class DeviceService {

    @Autowired
    private ApplicationProps applicationProps;

    private UserAgentParser parser;

    @PostConstruct
    public void init() {
        try {
            UserAgentParser parser =
                    new UserAgentService().loadParser(Arrays.asList(BrowsCapField.BROWSER, BrowsCapField.DEVICE_TYPE));
        } catch (Exception e) {
            log.error("Error loading UserAgentService: ", e);
        }
    }

    public UserAgentInfo getUserAgent(HttpServletRequest request) {
        String useragent = request.getHeader("user-agent");
        if (useragent == null || useragent.isEmpty()) {
            return new UserAgentInfo();
        }
        Capabilities capabilites = parser.parse(useragent);
        return new UserAgentInfo(capabilites.getBrowser(), capabilites.getDeviceType());
    }
}
