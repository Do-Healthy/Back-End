package gangdong.diet.domain.post.dto;

import gangdong.diet.domain.nutrient.entity.Nutrient;
import gangdong.diet.domain.post.entity.PostNutrient;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostNutrientResponse {

    private Long id;
    private String name;
    private String amount;

    @Builder
    public PostNutrientResponse(PostNutrient postNutrient) {
        this.id = postNutrient.getNutrient().getId();
        this.name = postNutrient.getNutrient().getName();
        this.amount = postNutrient.getAmount();
    }

    public PostNutrientResponse() {
    }
}
