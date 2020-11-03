package org.kryptonmlt.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAgentInfo {
    private String browser;
    private String device;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAgentInfo that = (UserAgentInfo) o;
        return Objects.equals(getBrowser(), that.getBrowser()) &&
                Objects.equals(getDevice(), that.getDevice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBrowser(), getDevice());
    }
}
