package gangdong.diet.global.auth.oauth.provider;

import gangdong.diet.global.auth.oauth.OAuth2MemberInfo;

import java.util.Map;

public class NaverMemberInfo implements OAuth2MemberInfo {

    private Map<String, Object> attributes; // getAttributes()

    public NaverMemberInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    }
}
