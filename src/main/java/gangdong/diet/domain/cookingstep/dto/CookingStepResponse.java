package gangdong.diet.domain.cookingstep.dto;

import gangdong.diet.domain.cookingstep.entity.CookingStep;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CookingStepResponse {

    private Long id;
    private String imageUrl;
    private String stepDescription;
    private static final String IMAGE_URL_PREFIX = "http:ec2주소";

    @Builder
    public CookingStepResponse(CookingStep cookingStep) {
        this.id = cookingStep.getId();
        this.imageUrl = cookingStep.getImageUrl() == null ? "" : IMAGE_URL_PREFIX + cookingStep.getImageUrl();
        this.stepDescription = cookingStep.getStepDescription();
    }

    public CookingStepResponse() {

    }
}
