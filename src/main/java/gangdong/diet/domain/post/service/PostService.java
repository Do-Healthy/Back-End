package gangdong.diet.domain.post.service;

import gangdong.diet.domain.post.dto.PostRequest;
import gangdong.diet.domain.post.dto.PostResponse;
import gangdong.diet.domain.post.dto.PostSearchResponse;
import gangdong.diet.global.auth.MemberDetails;
import org.springframework.data.domain.Slice;

public interface PostService {

    public Slice<PostSearchResponse> findByKeywords(Long cursorId, String keywords, int size);

    public Slice<PostSearchResponse> findAllPosts(Long cursorId, int size);

    public PostResponse getOnePost(Long id);

    public Long savePost(PostRequest postRequest, MemberDetails memberDetails);

    public PostResponse updatePost(Long id, PostRequest postRequest, MemberDetails memberDetails);

    public void deletePost(Long id, MemberDetails memberDetails);

//    Slice<PostSearchResponse> test(Long cursorId, String keywords, int size, String category);

//    Slice<PostSearchResponse> testkeywords(Long cursorId, String keywords, int size);

//    Slice<PostSearchResponse> testingredient(Long cursorId, String keywords, int size);

//    PostResponse testGetOnePost(Long id);



}
