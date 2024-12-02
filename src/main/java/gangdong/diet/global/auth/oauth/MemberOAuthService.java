package gangdong.diet.global.auth.oauth;

import gangdong.diet.global.auth.MemberDetails;
import gangdong.diet.global.auth.oauth.provider.GoogleMemberInfo;
import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.member.dto.SaveMemberDTO;
import gangdong.diet.domain.member.repository.MemberRepository;
import gangdong.diet.global.auth.oauth.provider.KakaoMemberInfo;
import gangdong.diet.global.auth.oauth.provider.NaverMemberInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberOAuthService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        OAuth2MemberInfo oAuth2MemberInfo;

        if(userRequest.getClientRegistration().getRegistrationId().equals("google")){
            log.info("구글 로그인 요청");
            oAuth2MemberInfo = new GoogleMemberInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            log.info("네이버 로그인 요청");
            oAuth2MemberInfo = new NaverMemberInfo((Map)oAuth2User.getAttributes().get("response"));
        } else if(userRequest.getClientRegistration().getRegistrationId().equals("kakao")){
            log.info("카카오 로그인 요청");
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Long id = (Long) attributes.get("id");
            log.info(id.toString());
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            log.info(kakaoAccount.get("email").toString());
            oAuth2MemberInfo = new KakaoMemberInfo(attributes);
        }else {
            oAuth2MemberInfo = null;
            log.info("다른 로그인 요청입니다.");
        }

        log.info("################### "+oAuth2MemberInfo.getEmail());
        String email = oAuth2MemberInfo.getEmail();

        SaveMemberDTO member =
                new SaveMemberDTO(memberRepository.findByMemberEmail(email)
                        .orElseGet(()->createMember(oAuth2MemberInfo)));

        return new MemberDetails(member);
    }

    private Member createMember(OAuth2MemberInfo oAuth2MemberInfo) {
        String provider = oAuth2MemberInfo.getProvider();
        String providerId = oAuth2MemberInfo.getProviderId();

        Member member = Member.builder()
                .memberEmail(oAuth2MemberInfo.getEmail())
                .name(oAuth2MemberInfo.getName())
                .password(passwordEncoder.encode("Do_Health"))
                .provider(provider)
                .providerId(providerId)
                .role("ROLE_USER")
                .build();

        return memberRepository.save(member);
    }
}
