package gangdong.diet.domain.post.entity;


import gangdong.diet.domain.BaseTimeEntity;
import gangdong.diet.domain.review.entity.Review;
import gangdong.diet.domain.scrap.entity.Scrap;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String content;

    private String mainImageUrl;

    // 작성자

    private String youtubeUrl;

    private Boolean isApproved;

    @OneToMany(mappedBy = "post")
    private List<PostIngredient> ingredients = new ArrayList<>();


    @OneToMany(mappedBy = "post")
    private List<PostNutrient> nutrients = new ArrayList<>();


    @OneToMany(mappedBy = "post")
    private List<Review> reviews = new ArrayList<>();


    @OneToMany(mappedBy = "post")
    private List<Scrap> scraps = new ArrayList<>(); // TODO : 트랜잭션 동시성 알아보기


    @OneToMany(mappedBy = "post")
    private List<PostImage> postImages = new ArrayList<>();

    @Builder
    public Post(String title, String content, String mainImageUrl, String youtubeUrl, List<PostIngredient> postIngredients, List<PostNutrient> postNutrients, List<PostImage> postImages) {
        this.title = title;
        this.content = content;
        this.mainImageUrl = mainImageUrl;
        this.youtubeUrl = youtubeUrl;
        this.ingredients = postIngredients;
        this.nutrients = postNutrients;
        this.postImages = postImages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
