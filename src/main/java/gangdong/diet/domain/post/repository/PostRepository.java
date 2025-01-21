package gangdong.diet.domain.post.repository;

import gangdong.diet.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, PostQRepository {

    List<Post> findByIngredients_IngredientName(String name);
}
