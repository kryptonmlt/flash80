package org.kryptonmlt.services;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import lombok.extern.slf4j.Slf4j;
import org.kryptonmlt.config.ApplicationProps;
import org.kryptonmlt.objects.Geo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@Slf4j
public class GeoService {

    private DatabaseReader reader;

    @Autowired
    private ApplicationProps applicationProps;

    @PostConstruct
    public void init() {
        // A File object pointing to your GeoIP2 or GeoLite2 database
        File database = new File(applicationProps.getGeodb());
        try {
            reader = new DatabaseReader.Builder(database).withCache(new CHMCache()).build();
        } catch (IOException e) {
            log.error("Error reading maxmind DB, ", e);
            System.exit(-1);
        }
    }

    public Geo getGeo(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName("128.101.101.101");
            CityResponse response = reader.city(ipAddress);
            return new Geo(response.getContinent().getCode(), response.getCountry().getIsoCode(), response.getMostSpecificSubdivision().getIsoCode());
        } catch (UnknownHostException e) {
            log.error("Error converting ip string to ip, ", e);
        } catch (GeoIp2Exception e) {
            log.error("Error reading from geo database, ", e);
        } catch (IOException e) {
            log.error("Error reading from geo database, ", e);
        }
        return new Geo("", "", "");
    }
}
