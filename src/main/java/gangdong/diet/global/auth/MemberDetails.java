package gangdong.diet.global.auth;

import gangdong.diet.domain.member.dto.SaveMemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MemberDetails implements UserDetails, OAuth2User {

    private final SaveMemberDTO saveMemberDTO;
    private Map<String, Object> attributes;

    public MemberDetails(SaveMemberDTO saveMemberDTO, Map<String, Object> attributes) {
        this.saveMemberDTO = saveMemberDTO;
        this.attributes = attributes;
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(saveMemberDTO.getRole().toString()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return saveMemberDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return saveMemberDTO.getUsername();
    }

    @Override
    public String getName() {
        return saveMemberDTO.getName();
    }
}
