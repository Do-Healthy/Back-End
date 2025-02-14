package gangdong.diet.domain.post.service;

import gangdong.diet.domain.post.dto.PostRedis;
import gangdong.diet.domain.post.dto.PostRequest;
import gangdong.diet.domain.post.dto.PostResponse;
import gangdong.diet.domain.post.dto.PostSearchResponse;
import gangdong.diet.global.auth.MemberDetails;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface PostService {

    Slice<PostSearchResponse> findByKeywords(String category, Long cursorId, String keywords, int size);

//    Slice<PostSearchResponse> findAllPosts(Long cursorId, int size);

    PostResponse getOnePost(Long id);

    Long savePost(PostRequest postRequest, MemberDetails memberDetails);

    PostResponse updatePost(Long id, PostRequest postRequest, MemberDetails memberDetails);

    void deletePost(Long id, MemberDetails memberDetails);

    Long savePostAll(List<PostRequest> postRequest, MemberDetails memberDetails);

    List<PostSearchResponse> getPopularPosts();

    public List<PostSearchResponse> findRelatedPosts(Long id);

    public List<PostRedis> getRecommendPosts();

    public PostResponse getSurveyPost(MemberDetails memberDetails);


}
