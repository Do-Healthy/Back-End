package gangdong.diet.domain.review.service;

import gangdong.diet.domain.review.dto.ReviewRequest;
import gangdong.diet.global.auth.MemberDetails;

public interface ReviewService {

    void addReview(Long postId, ReviewRequest reviewRequest, MemberDetails memberDetails);

    void updateReview(Long reviewId, ReviewRequest reviewRequest, MemberDetails memberDetails);

    void deleteReview(Long reviewId, MemberDetails memberDetails);

}
