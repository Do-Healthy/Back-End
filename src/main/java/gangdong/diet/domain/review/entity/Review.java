package gangdong.diet.domain.review.entity;

import gangdong.diet.domain.BaseTimeEntity;
import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Review extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Setter
    private String content;

    @Setter
    private int rating;

    @ManyToOne
    private Member member;

    @ManyToOne
    private Post post;

    @Setter
    @OneToMany(mappedBy = "review")
    List<ReviewImage> reviewImages = new ArrayList<>();

    @Builder
    public Review(String content, int rating, Member member, Post post, List<ReviewImage> reviewImages) {
        this.content = content;
        this.rating = rating;
        this.member = member;
        this.post = post;
        this.reviewImages = reviewImages;
    }
}
