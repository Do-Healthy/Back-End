package gangdong.diet.domain.member.entity;

import gangdong.diet.domain.BaseTimeEntity;
import gangdong.diet.domain.scrap.entity.Scrap;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memberEmail;
    private String password;
    private String name;
    private String role;

    private String provider;
    private String providerId;

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Scrap> scraps = new ArrayList<>();

    @Builder
    public Member(Long id, String memberEmail, String password, String name, String role, String provider, String providerId) {
        this.id = id;
        this.memberEmail = memberEmail;
        this.password = password;
        this.name = name;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }



}
