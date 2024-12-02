package gangdong.diet.domain.scrap.entity;

import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Scrap {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Builder
    private Scrap(Post post, Member member) {
        this.post = post;
        this.member = member;
    }
}
