package gangdong.diet.global.jwt;

import gangdong.diet.domain.member.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Slf4j
public class JwtController {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @PostMapping("/api/member/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        //get refresh token
        String refresh = null;
        String access = request.getHeader("Authorization");

        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            //response status code
            return ResponseEntity.badRequest().body("Refresh Token not exist 1");
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            //response status code
            return ResponseEntity.badRequest().body("토큰이 만료 되었습니다.");
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getRefresh(refresh);

        if (!category.equals("refresh_token")) {
            //response status code
            return ResponseEntity.badRequest().body("Refresh Token not exist 2");
        }

        //DB에 저장되어 있는지 확인
        String memberEmail = jwtUtil.getMemberEmail(access);
        String role = jwtUtil.getRole(access);

        log.info("####### username ####### {}",memberEmail);
        log.info("####### role ####### {}",role);

        String isExist = "refresh_token:"+tokenService.getRefreshToken(memberEmail);

        if (isExist == null) {
            //response body
            return ResponseEntity.badRequest().body("Refresh Token not exist 3");
        }

        //make new JWT
        String newAccess = jwtUtil.createAccessToken(memberEmail, role);
        String newRefresh = jwtUtil.createRefreshToken(memberEmail);

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        tokenService.saveRefreshToken("refresh",newRefresh);

        //response
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/member/logout")
    public ResponseEntity<?> reissue(HttpServletRequest request) {
        String refresh = null;

        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }
        String memberEmail = jwtUtil.getMemberEmail(refresh);

        tokenService.deleteRefreshToken(memberEmail);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60);
        cookie.setPath("/");
        cookie.setHttpOnly(false);

        return cookie;
    }
}
