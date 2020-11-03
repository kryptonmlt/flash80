package org.kryptonmlt.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Geo {
    private String continent;
    private String country;
    private String region;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Geo geo = (Geo) o;
        return Objects.equals(getContinent(), geo.getContinent()) &&
                Objects.equals(getCountry(), geo.getCountry()) &&
                Objects.equals(getRegion(), geo.getRegion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContinent(), getCountry(), getRegion());
    }
}
