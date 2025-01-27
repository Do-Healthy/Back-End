package gangdong.diet.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JwtUtil {

    private SecretKey secretKey;
    private final Long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30; // 30분
    private final Long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24; // 24시간
    private final RedisTemplate<String, String> redisTemplate;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret, RedisTemplate<String, String> redisTemplate) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.redisTemplate = redisTemplate;
    }

    // 토큰에서 "memberEmail" 클레임을 추출하는 메서드. JWT 토큰을 파싱하여 memberEmail 값을 반환.
    public String getMemberEmail(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("memberEmail", String.class);
    }

    // 토큰에서 "role" 클레임을 추출하는 메서드. JWT 토큰을 파싱하여 role 값을 반환.
    public String getRole(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Long getId(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("id",Long.class);
    }

    // 토큰의 유효 기간이 만료되었는지 확인하는 메서드. 토큰의 만료 날짜와 현재 날짜를 비교하여 만료 여부 반환.
    public Boolean isExpired(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String getAccess(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("Authorization", String.class);
    }

    public String getRefresh(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("refresh", String.class);
    }

//    // 새로운 JWT 토큰을 생성하는 메서드. username, role, 만료 시간(ms)을 받아 JWT 토큰을 생성하고 반환.
//    public String createJwt(String category, String username,Long expiredMs){
//
//        return Jwts.builder()
//                .claim("category", category)
//                .claim("username", username)
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + expiredMs))
//                .signWith(SignatureAlgorithm.HS256,secretKey)
//                .compact();
//    }

    public String createAccessToken(String memberEmail, String role) {
        return Jwts.builder()
                .claim("memberEmail", memberEmail)
                .claim("access","access_token")
                .claim("role", role)
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createRefreshToken(String memberEmail) {
        String refreshToken = Jwts.builder()
                .claim("memberEmail",memberEmail)
                .claim("refresh","refresh_token")
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        // Redis에 저장
        redisTemplate.opsForValue().set("refresh_token:" + memberEmail, refreshToken, REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        return refreshToken;
    }

}