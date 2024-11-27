package gangdong.diet.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;

    // Redis에 Refresh Token 저장
    public void saveRefreshToken(String memberEmail, String refreshToken) {
        redisTemplate.opsForValue().set("refresh_token:" + memberEmail, refreshToken, 24, TimeUnit.HOURS);
        log.info("refresh_token:" + memberEmail + refreshToken, 24, TimeUnit.HOURS);
    }

    // Redis에서 Refresh Token 조회
    public String getRefreshToken(String memberEmail) {
        return redisTemplate.opsForValue().get("refresh_token:" + memberEmail);
    }

    // Redis에서 Refresh Token 삭제
    public void deleteRefreshToken(String memberEmail) {
        redisTemplate.delete("refresh_token:" + memberEmail);
    }
}