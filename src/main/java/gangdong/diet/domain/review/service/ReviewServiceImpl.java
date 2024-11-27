package gangdong.diet.domain.review.service;

import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.post.repository.PostRepository;
import gangdong.diet.domain.review.dto.ReviewRequest;
import gangdong.diet.domain.review.entity.Review;
import gangdong.diet.domain.review.repository.ReviewRepository;
import gangdong.diet.global.exception.ApiException;
import gangdong.diet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class ReviewServiceImpl implements ReviewService{

    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;

    @Transactional
    @Override
    public void addReview(ReviewRequest reviewRequest) {
        Post post = postRepository.findById(reviewRequest.getPostId())
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        Review review = Review.builder()
                .content(reviewRequest.getContent())
                .rating(reviewRequest.getRating())
                .post(post)
                .build();

        post.getReviews().add(review);

        reviewRepository.save(review);
    }

    @Transactional
    @Override
    public void updateReview(Long reviewId, ReviewRequest reviewRequest) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));

        review.setContent(reviewRequest.getContent());

    }

    @Transactional
    @Override
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));

        reviewRepository.delete(review);
    }
}
