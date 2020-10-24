package org.kryptonmlt.services;

import org.kryptonmlt.objects.Geo;
import org.springframework.stereotype.Component;

@Component
public class GeoService {

    // TODO: IMPLEMENT REAL GEO SERVICE

    public Geo getGeo(String ip){
        return new Geo("America","US","Texas");
    }
}
