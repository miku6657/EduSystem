package com.keshe.edumanage.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JWTUtil {
    /**
     * JWT密钥
     *
     * HS256要求至少32字节
     */
    private static final String SECRET_KEY =
            "edumanage123456789012345678901234567890";

    /**
     * token有效时间
     *
     * 7天
     */
    private static final long EXPIRATION_TIME =
            1000L * 60 * 60 * 24 * 7;

    /**
     * 获取密钥对象
     */
    private SecretKey getKey(){
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 生成JWT Token
     *
     * @param username 用户名
     * @param role 角色
     * @return token
     */
    public String generateToken(
            String username,
            String role
    ){
        return Jwts.builder()

                // 用户名
                .setSubject(username)

                // 保存角色
                .claim(
                        "role",
                        role
                )

                // 创建时间
                .setIssuedAt(
                        new Date()
                )

                // 过期时间
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION_TIME
                        )
                )

                // 签名
                .signWith(
                        getKey()
                )

                .compact();
    }

    /**
     * 获取用户名
     */
    public String getUsername(String token){
        return parseToken(token)
                .getSubject();
    }

    /**
     * 获取角色
     */
    public String getRole(String token){
        return parseToken(token)
                .get(
                        "role",
                        String.class
                );
    }

    /**
     * 解析Token
     */
    private Claims parseToken(String token){
        return Jwts.parserBuilder()

                .setSigningKey(
                        getKey()
                )

                .build()

                .parseClaimsJws(token)

                .getBody();
    }

    /**
     * 校验Token
     */
    public boolean validateToken(String token){
        try {
            parseToken(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
