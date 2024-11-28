package gangdong.diet.domain.post.repository;

import gangdong.diet.domain.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface PostQRepository {

    List<Post> findByRecipeName(Long cursorId, List<String> keywords, int size);

    List<Post> findByIngredient(Long cursorId, List<String> keywords, int size);

}
