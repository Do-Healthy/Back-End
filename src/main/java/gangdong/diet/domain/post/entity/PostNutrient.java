package gangdong.diet.domain.post.entity;

import gangdong.diet.domain.nutrient.entity.Nutrient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PostNutrient {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Setter // 변경을 대비해서 붙여줘야할듯
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrient_id")
    private Nutrient nutrient;

    @Setter
    @NotNull
    private String amount;

    @Builder
    private PostNutrient(Post post, Nutrient nutrient, String amount) {
        this.post = post;
        this.nutrient = nutrient;
        this.amount = amount;
    }
}
