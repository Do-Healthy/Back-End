package gangdong.diet.domain.review.dto;

import gangdong.diet.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponse {

    public Long id;
    public String content;
    public double score;
    public String author;
    public LocalDateTime createdAt;

    @Builder
    public ReviewResponse(Review review) {
        this.id = review.getReviewId();
        this.content = review.getContent();
        this.score = review.getRating();
        this.author = review.getMember().getName();
        this.createdAt = review.getCreatedAt();
    }

}
