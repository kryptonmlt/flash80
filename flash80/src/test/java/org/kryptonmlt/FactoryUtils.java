package org.kryptonmlt;

import org.apache.commons.lang3.RandomStringUtils;
import org.kryptonmlt.objects.Geo;
import org.kryptonmlt.objects.UserAgentInfo;

public class FactoryUtils {

    private void FactoryUtils(){}

    public static Geo generateGeo(){
        Geo geo = new Geo();
        geo.setContinent(RandomStringUtils.randomAlphabetic(3));
        geo.setCountry(RandomStringUtils.randomAlphabetic(3));
        geo.setRegion(RandomStringUtils.randomAlphabetic(3));
        return geo;
    }
    public static UserAgentInfo generateUserAgentInfo(){
        UserAgentInfo userAgentInfo = new UserAgentInfo();
        userAgentInfo.setBrowser(RandomStringUtils.randomAlphabetic(3));
        userAgentInfo.setDevice(RandomStringUtils.randomAlphabetic(3));
        return userAgentInfo;
    }
}
