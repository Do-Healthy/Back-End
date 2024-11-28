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
@RequestMapping("/review")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity addReview(@Validated ReviewRequest request
            , @AuthenticationPrincipal MemberDetails memberDetails) { // AuthenticationPrincipal
        reviewService.addReview(request);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity updateReview(@PathVariable Long id, @Validated ReviewRequest request) {
        reviewService.updateReview(id, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);

        return ResponseEntity.ok().build();
    }

}
