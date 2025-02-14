package gangdong.diet.domain.post.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

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

    public PostSearchResponse(Long id, String title, String mainImageUrl, String cookingTime, String calories, String servings) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = mainImageUrl;
        this.cookingTime = cookingTime;
        this.calories = calories.toString();
        this.servings = servings.toString();
        this.isScrapped = isScrapped;
    }

    public PostSearchResponse() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostSearchResponse that = (PostSearchResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
