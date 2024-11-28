package gangdong.diet.domain.review.service;

import gangdong.diet.domain.review.dto.ReviewRequest;

public interface ReviewService {

    void addReview(ReviewRequest reviewRequest);

    void updateReview(Long reviewId, ReviewRequest reviewRequest);

    void deleteReview(Long reviewId);

}
