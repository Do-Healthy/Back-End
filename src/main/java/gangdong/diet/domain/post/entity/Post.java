package gangdong.diet.domain.post.entity;


import gangdong.diet.domain.BaseEntity;
import gangdong.diet.domain.BaseTimeEntity;
import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.review.entity.Review;
import gangdong.diet.domain.scrap.entity.Scrap;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String title;

    @Setter
    private String content;

    @Setter
    private String cookingTime; // 이 친구들 string으로 둬도 좋은지.

    @Setter
    private Integer calories;

    @Setter
    private Integer servings;

    @Setter
    private String thumbnailUrl;

    // 작성자
    @Setter
    private String youtubeUrl;

    private Boolean isApproved;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true) // 카스케이드 고민
    private List<PostIngredient> ingredients = new ArrayList<>();


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostNutrient> nutrients = new ArrayList<>();


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Review> reviews = new ArrayList<>();


    @OneToMany(mappedBy = "post")
    @BatchSize(size = 10)
    private List<Scrap> scraps = new ArrayList<>(); // TODO : 트랜잭션 동시성 알아보기

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> postImages = new ArrayList<>();

    @Builder
    private Post(Long id, String title, String content, String cookingTime, Integer calories, Integer servings, String thumbnailUrl, String youtubeUrl, Member member, List<PostIngredient> postIngredients, List<PostNutrient> postNutrients, List<PostImage> postImages, Boolean isApproved, List<PostTag> postTags) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.cookingTime = cookingTime;
        this.calories = calories;
        this.servings = servings;
        this.thumbnailUrl = thumbnailUrl;
        this.youtubeUrl = youtubeUrl;
        this.member = member;
        this.ingredients = postIngredients != null ? postIngredients : new ArrayList<>();
        this.nutrients = postNutrients != null ? postNutrients : new ArrayList<>();
        this.postImages = postImages != null ? postImages : new ArrayList<>();
        this.isApproved = isApproved;
        this.postTags = postTags != null ? postTags : new ArrayList<>();
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
