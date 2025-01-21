package gangdong.diet.domain.cookingstep.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CookingStepRequest {

    @NotBlank(message = "Url은 필수입니다.")
    private String imageUrl;
    @NotBlank(message = "각 조리법에 대한 설명은 필수입니다.")
    private String stepDescription;

}
