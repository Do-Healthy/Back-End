package gangdong.diet.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PostImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

//    private String type; 타입이 필요할 경우라면 추가

    @Builder
    public PostImage(String imageUrl, String description, Post post) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.post = post;
    }
}
