package gangdong.diet.domain.post.repository;

import gangdong.diet.domain.post.entity.PostIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostIngredientRepository extends JpaRepository<PostIngredient, Long> {

    void deleteByPostId(Long postId);

}
