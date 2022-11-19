package com.team6.onandthefarmorderservice.security.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

@Slf4j
@Component
public class JwtTokenUtil {

    private final Key secretKey;

    public static final String TOKEN_PREFIX = "Bearer ";

    Environment env;


    @Autowired
    public JwtTokenUtil(Environment env) {
        this.env = env;

        String secretKey = env.getProperty("custom-api-key.jwt.secret");

        // secretKey 바이트로 변환하여 Base64로 인코딩
        String encodingSecretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
        // Base64 byte[]로 변환
        byte[] decodedByte = Base64.getDecoder().decode(encodingSecretKey.getBytes(StandardCharsets.UTF_8));
        // byte[]로 key 생성
        this.secretKey = Keys.hmacShaKeyFor(decodedByte);
    }

    // 토큰에 담긴 payload 값 가져오기
    public Claims extractAllClaims(String token) throws ExpiredJwtException {
        String tokenDelPrefix = token.replace(TOKEN_PREFIX, "");
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(tokenDelPrefix)
                .getBody();
    }

    // id 가져오기
    public Long getId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }

    // Role 가져오기
    public String getRole(String token){
        return extractAllClaims(token).get("role", String.class);
    }

}
