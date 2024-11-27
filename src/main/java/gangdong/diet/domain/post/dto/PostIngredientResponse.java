package gangdong.diet.domain.post.dto;

import gangdong.diet.domain.ingredient.entity.Ingredient;
import gangdong.diet.domain.post.entity.PostIngredient;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostIngredientResponse {

    private Long id;
    private String name;
    private String amount;

    @Builder
    public PostIngredientResponse(PostIngredient postIngredient) {
        this.id = postIngredient.getIngredient().getId();
        this.name = postIngredient.getIngredient().getName();
        this.amount = postIngredient.getIngredient().getUnit() + postIngredient.getAmount();
    }

}
