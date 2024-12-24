package gangdong.diet.domain.post.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class PostSearchResponse {

    private Long id;
    private String title;
    private String thumbnailUrl;
    private String cookingTime;
    private String calories;
    private String servings;

    @Setter
    private Boolean isScrapped;
    private static final String IMAGE_URL_PREFIX = "http:ec2주소";

    public PostSearchResponse(Long id, String title, String mainImageUrl, String cookingTime, Integer calories, Integer servings) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = mainImageUrl;
        this.cookingTime = cookingTime;
        this.calories = calories.toString();
        this.servings = servings.toString();
        this.isScrapped = isScrapped;
    }
}
