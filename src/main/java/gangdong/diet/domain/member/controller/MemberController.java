package gangdong.diet.domain.member.controller;

import gangdong.diet.domain.member.dto.SaveMemberDTO;
import gangdong.diet.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
