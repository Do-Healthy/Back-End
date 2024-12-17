package gangdong.diet.domain.post.entity;

import gangdong.diet.domain.ingredient.entity.Ingredient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PostIngredient {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Setter // 변경 시를 대비해서 붙여줘야할 듯.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Setter
    @NotNull
    private String amount;

    @Builder
    private PostIngredient(Post post, Ingredient ingredient, String amount) {
        this.post = post;
        this.ingredient = ingredient;
        this.amount = amount;
    }
}
