package gangdong.diet.domain.post.service;

import gangdong.diet.domain.post.dto.PostResponse;
import org.springframework.data.domain.Slice;

public interface PostService {

    public Slice<PostResponse> findByKeywords(Long cursorId, String keywords, int size);

    public PostResponse getOnePost(Long id);

}
