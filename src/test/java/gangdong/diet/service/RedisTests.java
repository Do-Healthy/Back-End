package gangdong.diet.service;

import gangdong.diet.DietApplication;
import gangdong.diet.domain.member.service.RedisService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = DietApplication.class)
@Transactional
@Rollback(false)
@ActiveProfiles("test")
public class RedisTests {
    @Autowired
    private RedisService redisService;

    @Test
    public void 레디스_테스트(){

        redisService.saveRefreshToken("김아무게","fdsaffwr_gmrkegmrk");
        System.out.println("테스트 럽샷");
    }
}
