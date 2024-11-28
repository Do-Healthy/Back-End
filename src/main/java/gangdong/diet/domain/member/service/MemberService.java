package gangdong.diet.domain.member.service;


import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.member.dto.SaveMemberDTO;
import gangdong.diet.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;


@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member save(SaveMemberDTO memberDTO) {

        memberDTO.setRole("ROLE_USER");

        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);

        Member member = Member.builder()
                .memberEmail(memberDTO.getMemberEmail())
                .password(memberDTO.getPassword())
                .name(memberDTO.getName())
                .role(memberDTO.getRole())
                .build();

        return memberRepository.save(member);
    }


}
