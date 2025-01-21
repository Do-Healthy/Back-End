package gangdong.diet.domain.post.dto;

import gangdong.diet.domain.cookingstep.dto.CookingStepRequest;
import gangdong.diet.domain.ingredient.dto.IngredientRequest;
import gangdong.diet.domain.nutrient.dto.NutrientRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;


@Schema(title = "게시물 등록 요청 API")
@Getter
public class PostRequest {

    @Schema(description = "레시피 제목", example = "김치지깨")
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @Schema(description = "레시피 간단한 내용", example = "간단하고 맛있는 져엄식 김치찌개에요")
    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 500, message = "500자를 넘길 수 없습니다.")
    private String description;

    @Schema(description = "조리시간", example = "1시간30분")
    @NotBlank
    private String cookingTime;

    @Schema(description = "칼로리", example = "450")
    @NotNull
    private String calories;

    @Schema(description = "몇인분", example = "3")
    @NotNull
    private String servings;

    @Schema(description = "썸네일", example = "")
    @NotNull
    private String thumbnailUrl;

//    @Schema(description = "썸네일", example = "asdfv.jpg")
//    private MultipartFile thumbnail;

    @Schema(description = "유튜브 URL, 아직 고민중인데 레시피 관련 아무 url 올릴 수 있도록", example = "")
    private String youtubeUrl;

    @NotEmpty(message = "재료는 필수입니다.")
    @Schema(description = "레시피 재료", example = "\"김치\" : \"200g\", \"두부\" : \"한모\", \"돼지고기\" : \"200g\"")
    private List<IngredientRequest> ingredients;

    @Schema(description = "성분", example = "재료와 같은 형식인데 여긴 null값도 가능입니다.")
    private List<NutrientRequest> nutrients;

    private List<CookingStepRequest> cookingSteps; // todo 필드명과 객체명 고민....

    @Schema(description = "태그", example = "저염식, 고단백, etc")
    private String tags;

//    @Schema(description = "레시피 관련 사진(조리법 관련 사진을 올립니다.)")
//    private List<MultipartFile> postImages = new ArrayList<>();

}
