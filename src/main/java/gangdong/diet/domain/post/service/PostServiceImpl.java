package gangdong.diet.domain.post.service;

import gangdong.diet.domain.post.dto.PostResponse;
import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.post.repository.PostRepository;
import gangdong.diet.global.exception.ApiException;
import gangdong.diet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;

    @Override
    public Slice<PostResponse> findByKeywords(Long cursorId, String keywords, int size) {

        // 공백 제거 후 쉼표 기준으로 리스트 생성
        String newKeywords = keywords.replaceAll(" ", "");
        StringTokenizer tokenizer = new StringTokenizer(newKeywords, ",");
        List<String> finalKeywords = new ArrayList<>();
        while(tokenizer.hasMoreTokens()) {
            finalKeywords.add(tokenizer.nextToken());
        }

        // Repository 호출
        List<Post> recipeResults = postRepository.findByRecipeName(cursorId, finalKeywords, size);
        List<Post> ingredientResults = postRepository.findByIngredient(cursorId, finalKeywords, size);

        // 합친 후 중복 제거
        Set<Post> mergedResultsSet = new LinkedHashSet<>(recipeResults);
        mergedResultsSet.addAll(ingredientResults);
        List<PostResponse> finalResults = mergedResultsSet.stream().map(p -> PostResponse.builder().post(p)
                        .userId(getUserId(p))
                        .build())
                .toList();



        // 페이징 처리 및 Slice 반환
        boolean hasNext = finalResults.size() > size;
        if (hasNext) {
            finalResults = finalResults.subList(0, size); // 초과 데이터 제거
        }

        return new SliceImpl<>(finalResults, PageRequest.of(0, size), hasNext);
    }

    @Override
    public PostResponse getOnePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        return PostResponse.builder()
                .post(post)
                .userId(getUserId(post))
                .build();
    }

    private Long getUserId(Post post) { //TODO 유저 합쳐지면 이거 바꿔야함   * 그리고 이거 고민인게 이렇게 스크랩 한 게시물 찾는게 별로 안 좋은것 같음.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        return null;
    }


}
