package gangdong.diet.domain.member.dto;

import gangdong.diet.domain.member.entity.Member;
import lombok.Builder;

public class MemberResponse {

    private String memberEmail;
    private String name;
    private String profileUrl;
    private static final String IMAGE_URL_PREFIX = "https://ec2아이디";

    @Builder
    public MemberResponse(Member member) {
        this.memberEmail = member.getMemberEmail();
        this.name = member.getName();
//        this.profileUrl = member.getProfileUrl() == null ? "" : IMAGE_URL_PREFIX +  member.getProfileUrl(); TODO : 프로필 유알엘 추가되면 변경
    }

}
