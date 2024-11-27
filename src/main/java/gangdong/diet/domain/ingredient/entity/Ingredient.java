package gangdong.diet.domain.ingredient.entity;

import gangdong.diet.domain.post.entity.PostIngredient;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(length = 30)
    private String unit;

    @OneToMany(mappedBy = "ingredient")
    private List<PostIngredient> posts;

}
