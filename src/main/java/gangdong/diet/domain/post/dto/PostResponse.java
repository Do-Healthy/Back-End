package gangdong.diet.domain.post.dto;

import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.review.dto.ReviewResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String mainImageUrl;
    private String youtubeUrl;
    private List<PostIngredientResponse> ingredients;
    private List<PostNutrientResponse> nutritions;
    private List<ReviewResponse> reviews;
    private List<PostImageResponse> postImages;

    private double averageRating;
    private int reviewCount;
    private int scrapCount;
    private Boolean isScrapped;
    private static final String IMAGE_URL_PREFIX = "https://ec2아이디";

    @Builder
    public PostResponse(Post post, Long userId) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.mainImageUrl = post.getMainImageUrl() == null ? "" : IMAGE_URL_PREFIX + post.getMainImageUrl();
        this.youtubeUrl = post.getYoutubeUrl();
        this.ingredients = post.getIngredients().stream().map(pi -> PostIngredientResponse.builder()
                .postIngredient(pi).build()).toList();
        this.nutritions = post.getNutrients().stream().map(pn -> PostNutrientResponse.builder()
                .postNutrient(pn).build()).toList();
        this.reviews = post.getReviews().stream().map(r -> ReviewResponse.builder()
                .review(r).build()).toList();
        this.postImages = post.getPostImages().stream().map(pim -> PostImageResponse.builder()
                .postImage(pim).build()).toList();
        this.averageRating = post.getReviews().stream()
                .mapToDouble(review -> review.getRating()) // 리뷰의 평점 추출
                .average() // 평균 계산
                .orElse(0.0); // 리뷰가 없으면 0.0 반환
        this.reviewCount = post.getReviews().size();
        this.scrapCount = post.getScraps().size();
        this.isScrapped = post.getScraps().stream()
                .anyMatch(scrap -> scrap.getMember().getId().equals(null)); // TODO 여기에 현재 로그인된 아이디 넣어야함
    }

}