package gangdong.diet.domain.review.controller;

import gangdong.diet.domain.review.dto.ReviewRequest;
import gangdong.diet.domain.review.service.ReviewService;
import gangdong.diet.global.auth.MemberDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리뷰 API")
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/posts/{postId}/reviews")
    public ResponseEntity addReview(@PathVariable("postId") Long postId,
                                    @Validated @RequestBody ReviewRequest request,
                                    @AuthenticationPrincipal MemberDetails memberDetails) { // AuthenticationPrincipal

        reviewService.addReview(postId, request, memberDetails);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity updateReview(@PathVariable Long reviewId,
                                       @Validated @RequestBody ReviewRequest request,
                                       @AuthenticationPrincipal MemberDetails memberDetails) {

        reviewService.updateReview(reviewId, request, memberDetails);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity deleteReview(@PathVariable Long reviewId, @AuthenticationPrincipal MemberDetails memberDetails) {

        reviewService.deleteReview(reviewId, memberDetails);

        return ResponseEntity.ok().body("삭제가 완료됐습니다.");
    }

}
