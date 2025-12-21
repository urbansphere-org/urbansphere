package com.urbansphere.authservice.security.jwt;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String issuer;
    private int accessTokenExpirationMinutes;
    private int refreshTokenExpirationDays;
    private String privateKeyPath;
    private String publicKeyPath;
}
