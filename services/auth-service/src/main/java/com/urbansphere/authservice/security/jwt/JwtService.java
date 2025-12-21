package com.urbansphere.authservice.security.jwt;

import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final JwtConfig jwtConfig;

    public JwtService(JwtConfig jwtConfig){
        this.jwtConfig = jwtConfig;
        try{
            this.privateKey = loadPrivateKey(jwtConfig.getPrivateKeyPath());
            this.publicKey = loadPublicKey(jwtConfig.getPublicKeyPath());

        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA keys",e);
        }
    }

    public String generateAccessToken(String subject, Map<String , Object> claims){
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtConfig.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .setIssuer(jwtConfig.getIssuer())
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(String subject){
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtConfig.getRefreshTokenExpirationDays(), ChronoUnit.DAYS);

        return Jwts.builder()
                .setIssuer(jwtConfig.getIssuer())
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean isValid(String token){
        try {
            extractAllClaims(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public String extractUserId(String token){
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token){

        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .requireIssuer(jwtConfig.getIssuer())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }


    private PrivateKey loadPrivateKey(String path) throws Exception{
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalStateException("Key file not found: " + path);
        }
        String key = new String(inputStream.readAllBytes());

        key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Decoders.BASE64.decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalStateException("Key file not found: " + path);
        }
        String key = new String(inputStream.readAllBytes());

        key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Decoders.BASE64.decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

}
