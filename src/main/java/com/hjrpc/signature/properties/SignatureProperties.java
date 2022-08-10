package com.hjrpc.signature.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

@ConfigurationProperties(prefix = "signature")
@Data
public class SignatureProperties {
    private Set<AccessCodeEntity> secretGroup;

    @Data
    static public class AccessCodeEntity {
        private String code;
        private Map<String, String> accessKey;
    }
}
