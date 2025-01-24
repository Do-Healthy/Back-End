package gangdong.diet.global.filter;

import gangdong.diet.domain.member.dto.SaveMemberDTO;
import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.global.auth.MemberDetails;
import gangdong.diet.global.jwt.JwtUtil;
import gangdong.diet.domain.member.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 엑세스 토큰이 있는지 부터 확인

        String accessToken = request.getHeader("Authorization");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    accessToken = cookie.getValue(); // JWT 값을 그대로 가져오기
                }
            }
        }

        log.info(accessToken+"    ##########$#$#$#$#$#$#$$#$");

        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        accessToken = accessToken.substring(7);

        log.info(accessToken+"   !!!!!!!!!!!!!!!!!!!!!!!!!$#$#$#$#$#$#$$#$");
        // 토큰 만료 여부 확인
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");
            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        // memberEmail, role 값을 획득
        String memberEmail = jwtUtil.getMemberEmail(accessToken);
        String role = jwtUtil.getRole(accessToken);

        SaveMemberDTO saveMemberDTO = SaveMemberDTO.builder().memberEmail(memberEmail).name(memberEmail).role(role).build();//todo 네임에 이메일 바꿔야함
        MemberDetails customUserDetails = new MemberDetails(saveMemberDTO);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        log.info(authToken.toString());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }


}
