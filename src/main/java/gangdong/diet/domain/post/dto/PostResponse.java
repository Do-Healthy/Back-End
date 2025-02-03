package gangdong.diet.domain.post.dto;

import gangdong.diet.domain.cookingstep.dto.CookingStepResponse;
import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.review.dto.ReviewResponse;
import gangdong.diet.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PostResponse {

    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private List<String> tagName = new ArrayList<>();
    private String cookingTime;
    private String calories;
    private String servings;
    private String youtubeUrl;
    @Setter private Long viewCount;
    private List<PostIngredientResponse> ingredients = new ArrayList<>();
    private List<PostNutrientResponse> nutrients = new ArrayList<>();
    private List<CookingStepResponse> cookingSteps = new ArrayList<>();

    private int reviewCount;
    private double averageRating;
    private List<ReviewResponse> reviews = new ArrayList<>();

    private int scrapCount;
    @Setter
    private Boolean isScrapped;

    private static final String IMAGE_URL_PREFIX = "http:ec2주소";

    @Builder
    public PostResponse(Post post, Boolean isScrapped) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.description = post.getDescription();
        this.thumbnailUrl = post.getThumbnailUrl() == null ? "" : IMAGE_URL_PREFIX + post.getThumbnailUrl();
        this.cookingTime = post.getCookingTime();
        this.calories = post.getCalories();
        this.servings = post.getServings();
        this.youtubeUrl = post.getYoutubeUrl();
        this.viewCount = post.getViewCount();
        this.ingredients = post.getIngredients().stream().map(pi -> PostIngredientResponse.builder()
                .postIngredient(pi).build()).toList();
        this.nutrients = post.getNutrients().stream().map(pn -> PostNutrientResponse.builder()
                .postNutrient(pn).build()).toList();
        this.reviews = post.getReviews().stream().map(r -> ReviewResponse.builder()
                .review(r).build()).toList();
        this.cookingSteps = post.getCookingSteps().stream().map(cs -> CookingStepResponse.builder()
                .cookingStep(cs).build()).toList();
        this.averageRating = post.getReviews().stream()
                .mapToDouble(review -> review.getRating()) // 리뷰의 평점 추출
                .average() // 평균 계산
                .orElse(0.0); // 리뷰가 없으면 0.0 반환
        this.reviewCount = post.getReviews().size();
        this.scrapCount = post.getScraps().size();
//        this.isScrapped = post.getScraps().stream()
//                .anyMatch(scrap -> scrap.getMember().getId().equals(null)); // TODO 여기에 현재 로그인된 아이디 넣어야함
        this.isScrapped = isScrapped;
        this.tagName = post.getPostTags().stream().map(pt -> pt.getTag().getName()).toList();
    }

    public PostResponse(Long id, String title, String description, String thumbnailUrl, String cookingTime, String calories, String servings, String youtubeUrl, Long viewCount, Boolean isApproved) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.cookingTime = cookingTime;
        this.calories = calories;
        this.servings = servings;
        this.youtubeUrl = youtubeUrl;
        this.viewCount = viewCount;
    }

    public PostResponse() {}

    public void setScrapCount(int scrapCount) {
        this.scrapCount = scrapCount;
    }

    public void setReview(List<Review> reviews) {
        this.reviews = reviews.stream().map(r -> ReviewResponse.builder()
                .review(r).build()).toList();
        this.reviewCount = reviews.size();
        this.averageRating = reviews.stream()
                .mapToDouble(review -> review.getRating()) // 리뷰의 평점 추출
                .average() // 평균 계산
                .orElse(0.0); // 리뷰가 없으면 0.0 반환
    }
}