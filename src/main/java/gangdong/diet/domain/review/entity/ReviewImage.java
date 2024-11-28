package gangdong.diet.domain.review.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class ReviewImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Review review;

    @Builder
    private ReviewImage(String imageUrl, String description, Review review) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.review = review;
    }

}
