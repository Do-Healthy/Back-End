package gangdong.diet.domain.post.service;

import gangdong.diet.domain.post.dto.CookingStepRequest;
import gangdong.diet.domain.post.dto.PostRequest;
import gangdong.diet.domain.post.dto.PostResponse;
import gangdong.diet.domain.post.dto.PostSearchResponse;
import gangdong.diet.global.auth.MemberDetails;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    public Slice<PostSearchResponse> findByKeywords(Long cursorId, String keywords, int size);

    public Slice<PostSearchResponse> findAllPosts(Long cursorId, int size);

    public PostResponse getOnePost(Long id);

    public Long savePost(PostRequest postRequest, MultipartFile thumbnail, List<MultipartFile> postImages, MemberDetails memberDetails);

    public PostResponse updatePost(Long id, PostRequest postRequest, MultipartFile thumbnail, List<MultipartFile> postImages, MemberDetails memberDetails);

    public void deletePost(Long id, MemberDetails memberDetails);

    public List<PostSearchResponse> findRelatedPosts(Long id);

    public PostResponse getSurveyPost(MemberDetails memberDetails);
}
