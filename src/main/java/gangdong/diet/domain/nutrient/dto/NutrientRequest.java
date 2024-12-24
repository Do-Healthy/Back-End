package gangdong.diet.domain.nutrient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class NutrientRequest {

//    @NotBlank(message = "성분명은 필수입니다.")
    private String name;

//    @NotBlank(message = "성분 값은 필수입니다.")
    private String amount;
}
