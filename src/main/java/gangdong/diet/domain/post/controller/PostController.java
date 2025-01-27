package gangdong.diet.domain.post.controller;

import gangdong.diet.domain.post.dto.PostRedis;
import gangdong.diet.domain.post.dto.PostRequest;
import gangdong.diet.domain.post.dto.PostResponse;
import gangdong.diet.domain.post.dto.PostSearchResponse;
import gangdong.diet.domain.post.service.PostService;
import gangdong.diet.global.auth.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "게시물 API")
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@RestController
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시물 목록 조회", description = "키워드를 통해 게시물을 검색합니다. 키워드 간의 구분은 ,과 같은 쉼표로 합니다.")
    @GetMapping("") // 뭐라고 이름 줄까?
    public ResponseEntity<Slice<PostSearchResponse>> getPostsByKeywords(@RequestParam(value = "cursorId", required = false) Long cursorId,
                                                                        @RequestParam(value = "keywords", required = false) String keywords,
                                                                        @RequestParam(value = "size") int size) {
        return ResponseEntity.ok().body(postService.findByKeywords(cursorId, keywords, size));
    }

    @Operation(summary = "게시물 상세 조회", description = "게시물 id를 통해 게시물 1개를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getOnePostById(@PathVariable Long id) {
        return ResponseEntity.ok().body(postService.getOnePost(id));
    }

    @Operation(summary = "게시물 저장")
    @PostMapping
    public ResponseEntity<String> createPost(@RequestPart("postRequest") @Validated PostRequest postRequest,
                                             @AuthenticationPrincipal MemberDetails memberDetails) {
        postService.savePost(postRequest, memberDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body("게시물 저장을 완료했습니다.");
    }


    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id,
                                                   @RequestPart("postRequest") @Validated PostRequest postRequest,
//                                                   @RequestPart("thumbnail") MultipartFile thumbnail,
//                                                   @RequestPart(value = "postImages", required = false) List<MultipartFile> postImages,
                                                   @AuthenticationPrincipal MemberDetails memberDetails) {

        return ResponseEntity.ok().body(postService.updatePost(id, postRequest, memberDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id, @AuthenticationPrincipal MemberDetails memberDetails) {
        postService.deletePost(id, memberDetails);
        return ResponseEntity.ok().body("삭제가 완료됐습니다.");
    }

    @Operation(summary = "재료 추천 게시물 조회", description = "게시물 id를 통해 관련 추천 리스트를 조회 합니다.")
    @GetMapping("recommended/{id}")
    public ResponseEntity<List<PostSearchResponse>> getRecommendedPostsById(@PathVariable Long id) {
        List<PostSearchResponse> list = postService.findRelatedPosts(id);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "인게 게시물 조회", description = "인기 게시물을 조회 합니다.")
    @GetMapping("recommended")
    public ResponseEntity<List<PostRedis>> getRecommendedPostsById() {
        List<PostRedis> responsePostList = postService.getRecommendPosts();
        return ResponseEntity.ok(responsePostList);
    }

    @Operation(summary = "설문조사 결과지로 관련 게시물 조회", description = "멤버의 id를 통해 설문지와 관련 된 게시물을 조회 합니다.")
    @GetMapping("survey")
    public ResponseEntity<PostResponse> getSurveyPost(@AuthenticationPrincipal MemberDetails memberDetails) {
        PostResponse postResponse = postService.getSurveyPost(memberDetails);
        return ResponseEntity.ok(postResponse);
    }


}
