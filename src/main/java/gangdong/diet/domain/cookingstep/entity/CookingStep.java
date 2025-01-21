package gangdong.diet.domain.cookingstep.entity;

import gangdong.diet.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CookingStep {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private String stepDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

//    private String type; 타입이 필요할 경우라면 추가

    @Builder
    public CookingStep(String imageUrl, String stepDescription, Post post) {
        this.imageUrl = imageUrl;
        this.stepDescription = stepDescription;
        this.post = post;
    }
}
