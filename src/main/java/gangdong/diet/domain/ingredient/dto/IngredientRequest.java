package gangdong.diet.domain.ingredient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class IngredientRequest {

    @NotBlank(message = "재료명은 필스입니다.")
    private String name;
    @NotBlank(message = "재료 양은 필수입니다.")
    private String amount;
}
