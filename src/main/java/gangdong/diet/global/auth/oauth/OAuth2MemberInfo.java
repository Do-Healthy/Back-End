package gangdong.diet.global.auth.oauth;

public interface OAuth2MemberInfo {
    String getProviderId();
    String getProvider();
    String getEmail();
    String getName();
}
