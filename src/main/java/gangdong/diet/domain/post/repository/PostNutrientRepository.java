package gangdong.diet.domain.post.repository;

import gangdong.diet.domain.post.entity.PostNutrient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostNutrientRepository extends JpaRepository<PostNutrient, Long> {

    void deleteByPostId(Long postId);

}
