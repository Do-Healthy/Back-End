package gangdong.diet.domain.post.repository;

import gangdong.diet.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    void deleteByPostId(Long postId);

}
