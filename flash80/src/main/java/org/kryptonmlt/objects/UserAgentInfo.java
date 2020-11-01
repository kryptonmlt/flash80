package org.kryptonmlt.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAgentInfo {
    private String browser;
    private String device;
}
