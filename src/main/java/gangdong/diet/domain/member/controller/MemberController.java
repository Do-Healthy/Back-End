package gangdong.diet.domain.member.controller;

import gangdong.diet.domain.member.dto.SaveMemberDTO;
import gangdong.diet.domain.member.service.MemberService;
import gangdong.diet.global.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member/")
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping("join")
    public ResponseEntity<String> saveJoin(@RequestBody SaveMemberDTO memberDto){

        log.info(memberDto.toString());

        memberService.save(memberDto);
        return ResponseEntity.ok("회원가입 완료");
    }

}
