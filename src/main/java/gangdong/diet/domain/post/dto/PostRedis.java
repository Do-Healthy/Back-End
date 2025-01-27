package gangdong.diet.domain.post.dto;

import gangdong.diet.domain.cookingstep.dto.CookingStepResponse;
import gangdong.diet.domain.review.dto.ReviewResponse;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostRedis {

    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private List<String> tagName = new ArrayList<>();
    private String cookingTime;
    private String calories;
    private String servings;
    private String youtubeUrl;
    private Integer viewCount;
    private List<PostIngredientResponse> ingredients = new ArrayList<>();
    private List<PostNutrientResponse> nutrients = new ArrayList<>();
    private List<CookingStepResponse> cookingSteps = new ArrayList<>();

    private int reviewCount;
    private double averageRating;
    private List<ReviewResponse> reviews = new ArrayList<>();

    private int scrapCount;

    @Builder
    public PostRedis(Long id, String title, String description, String thumbnailUrl, List<String> tagName, String cookingTime, String calories, String servings, String youtubeUrl, Integer viewCount, List<PostIngredientResponse> ingredients, List<PostNutrientResponse> nutrients, List<CookingStepResponse> cookingSteps, int reviewCount, double averageRating, List<ReviewResponse> reviews, int scrapCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.tagName = tagName;
        this.cookingTime = cookingTime;
        this.calories = calories;
        this.servings = servings;
        this.youtubeUrl = youtubeUrl;
        this.viewCount = viewCount;
        this.ingredients = ingredients;
        this.nutrients = nutrients;
        this.cookingSteps = cookingSteps;
        this.reviewCount = reviewCount;
        this.averageRating = averageRating;
        this.reviews = reviews;
        this.scrapCount = scrapCount;
    }
}
