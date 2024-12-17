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
    private String thumbnailUrl;

    // 작성자
    @Setter
    private String youtubeUrl;

    private Boolean isApproved;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostIngredient> ingredients = new HashSet<>();


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostNutrient> nutrients = new HashSet<>();


    @OneToMany(mappedBy = "post")
    @BatchSize(size = 10)
    private List<Review> reviews = new ArrayList<>();


    @OneToMany(mappedBy = "post")
    private List<Scrap> scraps = new ArrayList<>(); // TODO : 트랜잭션 동시성 알아보기

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostTag> postTags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostImage> postImages = new HashSet<>();

    @Builder
    private Post(String title, String content, String thumbnailUrl, String youtubeUrl, Member member, Set<PostIngredient> postIngredients, Set<PostNutrient> postNutrients, Set<PostImage> postImages, Boolean isApproved, Set<PostTag> postTags) {
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.youtubeUrl = youtubeUrl;
        this.member = member;
        this.ingredients = postIngredients != null ? postIngredients : new HashSet<>();
        this.nutrients = postNutrients != null ? postNutrients : new HashSet<>();
        this.postImages = postImages != null ? postImages : new HashSet<>();
        this.isApproved = isApproved;
        this.postTags = postTags != null ? postTags : new HashSet<>();
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
