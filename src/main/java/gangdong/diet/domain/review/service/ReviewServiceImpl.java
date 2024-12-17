package gangdong.diet.domain.review.service;

import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.member.repository.MemberRepository;
import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.post.repository.PostRepository;
import gangdong.diet.domain.review.dto.ReviewRequest;
import gangdong.diet.domain.review.entity.Review;
import gangdong.diet.domain.review.repository.ReviewRepository;
import gangdong.diet.global.auth.MemberDetails;
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
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public void addReview(Long postId, ReviewRequest reviewRequest, MemberDetails memberDetails) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = Review.builder()
                .content(reviewRequest.getContent())
                .rating(reviewRequest.getRating())
                .member(member)
                .post(post)
                .build();

        post.getReviews().add(review);

        reviewRepository.save(review);
    }

    @Transactional
    @Override
    public void updateReview(Long reviewId, ReviewRequest reviewRequest, MemberDetails memberDetails) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));

        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getId() == review.getMember().getId()) {
            review.setContent(reviewRequest.getContent());
        }
        else {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }


    }

    @Transactional
    @Override
    public void deleteReview(Long reviewId, MemberDetails memberDetails) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));

        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getId() == review.getMember().getId()) {
            reviewRepository.delete(review);
        }
        else {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }
    }
}
