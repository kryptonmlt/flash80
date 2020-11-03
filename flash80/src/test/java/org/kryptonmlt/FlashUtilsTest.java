package org.kryptonmlt;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.kryptonmlt.objects.Flash80Request;
import org.kryptonmlt.objects.Geo;
import org.kryptonmlt.objects.UserAgentInfo;
import org.kryptonmlt.utils.FlashUtils;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@SpringBootTest
class FlashUtilsTest {

    @Test
    public void test_getIp_ForwardHeader() {
        String remoteAddr = "127";
        String header = "99";
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        Mockito.when(requestMock.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(requestMock.getHeader("X-FORWARDED-FOR")).thenReturn(header);
        Assert.state(header.equals(FlashUtils.getIp(requestMock)), "incorrect ip retrieved");
    }

    @Test
    public void test_getIp_ForwardAddr() {
        String remoteAddr = "127";
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        Mockito.when(requestMock.getRemoteAddr()).thenReturn(remoteAddr);
        Assert.state(remoteAddr.equals(FlashUtils.getIp(requestMock)), "incorrect ip retrieved");
    }

    @Test
    public void test_isMatch_Map_Success() {

        String[] list = {"logged_in", "test"};
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        List<String> header1 = new ArrayList<>();
        header1.add(RandomStringUtils.randomAlphanumeric(10));
        header1.add(RandomStringUtils.randomAlphanumeric(10));
        header1.add(RandomStringUtils.randomAlphanumeric(10));
        List<String> header2 = new ArrayList<>();
        header2.add(RandomStringUtils.randomAlphanumeric(10));
        header2.add(RandomStringUtils.randomAlphanumeric(10) + list[0] + RandomStringUtils.randomAlphanumeric(10));
        header2.add(RandomStringUtils.randomAlphanumeric(10));
        headers.put(RandomStringUtils.randomAlphanumeric(10), header1);
        headers.put(RandomStringUtils.randomAlphanumeric(10), header2);
        Assert.state(FlashUtils.isMatch(headers, list), "There should have been a match");
    }

    @Test
    public void test_isMatch_Map_Fail() {

        String[] list = {"logged_in", "test"};
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        List<String> header1 = new ArrayList<>();
        header1.add(RandomStringUtils.randomAlphanumeric(10));
        header1.add(RandomStringUtils.randomAlphanumeric(10));
        header1.add(RandomStringUtils.randomAlphanumeric(10));
        headers.put(RandomStringUtils.randomAlphanumeric(10), header1);
        List<String> header2 = new ArrayList<>();
        header2.add(RandomStringUtils.randomAlphanumeric(10));
        header2.add(RandomStringUtils.randomAlphanumeric(10));
        Assert.state(!FlashUtils.isMatch(headers, list), "There should not have been a match");
    }

    @Test
    public void test_isMatch_String_Success() {
        String[] list = {"logged_in", RandomStringUtils.randomAlphanumeric(20)};
        Assert.state(!FlashUtils.isMatch(list[1], list), "There should have been a match");
    }

    @Test
    public void test_isMatch_String_Fail() {
        String[] list = {"logged_in", "test"};
        Assert.state(!FlashUtils.isMatch(RandomStringUtils.randomAlphanumeric(3) + "x" + RandomStringUtils.randomAlphanumeric(3), list), "There should not have been a match");
    }

    @Test
    public void test_isMatch_List_Success() {
        String[] list = {"logged_in", "test"};
        String[] header1 = {RandomStringUtils.randomAlphanumeric(2),
                RandomStringUtils.randomAlphanumeric(2) + " logged_in " +
                        RandomStringUtils.randomAlphanumeric(2), RandomStringUtils.randomAlphanumeric(2)};
        Assert.state(!FlashUtils.isMatch(header1, list), "There should have been a match");
    }

    @Test
    public void test_isMatch_List_Fail() {
        String[] list = {"logged_in", RandomStringUtils.randomAlphanumeric(20)};
        String[] header1 = {RandomStringUtils.randomAlphanumeric(5),
                RandomStringUtils.randomAlphanumeric(4), RandomStringUtils.randomAlphanumeric(3)};
        Assert.state(!FlashUtils.isMatch(header1, list), "There should not have been a match");
    }

    @Test
    public void test_toFlash80Request_Success() {
        Geo geo = FactoryUtils.generateGeo();
        UserAgentInfo userAgentInfo = FactoryUtils.generateUserAgentInfo();
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        String uri = RandomStringUtils.randomAlphanumeric(5);
        String host = RandomStringUtils.randomAlphanumeric(5);
        String queryString = RandomStringUtils.randomAlphanumeric(5);
        String scheme = RandomStringUtils.randomAlphanumeric(5);
        String site = RandomStringUtils.randomAlphanumeric(5);
        Mockito.when(requestMock.getRequestURI()).thenReturn(uri);
        Mockito.when(requestMock.getRemoteHost()).thenReturn(host);
        Mockito.when(requestMock.getQueryString()).thenReturn(queryString);
        Mockito.when(requestMock.getScheme()).thenReturn(scheme);
        List<String> headers = new ArrayList<>();
        headers.add("host");
        Enumeration<String> headersEnum = Collections.enumeration(headers);
        Mockito.when(requestMock.getHeaderNames()).thenReturn(headersEnum);
        Mockito.when(requestMock.getHeader("host")).thenReturn(site);

        Flash80Request flash80Request = FlashUtils.toFlash80Request(requestMock, geo, userAgentInfo);
        Assert.state(flash80Request.getHeaderReq().size() == 1, "There should be no headers");
        Assert.state(flash80Request.getRequestParams().equals("?"+queryString), "Query string not same");
        Assert.state(flash80Request.getScheme().equals(scheme), "scheme not same");
        Assert.state(flash80Request.getSite().equals(host), "site not same");
        Assert.state(flash80Request.getUri().equals(uri), "uri not same");
        Assert.state(flash80Request.getGeo().equals(geo), "geo not same");
        Assert.state(flash80Request.getUserAgentInfo().equals(userAgentInfo), "useragentinfo not same");
    }

}
