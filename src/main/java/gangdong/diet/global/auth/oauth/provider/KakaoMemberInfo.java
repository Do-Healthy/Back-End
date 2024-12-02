package gangdong.diet.global.auth.oauth.provider;

import gangdong.diet.global.auth.oauth.OAuth2MemberInfo;

import java.util.Map;

public class KakaoMemberInfo implements OAuth2MemberInfo {

    private Map<String, Object> attributes; // getAttributes()

    public KakaoMemberInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount != null ? kakaoAccount.get("email").toString() : null;
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties != null ? properties.get("nickname").toString() : null;
    }
}
