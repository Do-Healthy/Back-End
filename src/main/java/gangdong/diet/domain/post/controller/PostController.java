package gangdong.diet.domain.post.controller;

import gangdong.diet.domain.post.dto.PostResponse;
import gangdong.diet.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시물 API")
@RequiredArgsConstructor
@RequestMapping("/posts")
@RestController
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시물 목록 조회", description = "키워드를 통해 게시물을 검색합니다. 키워드 간의 구분은 ,과 같은 쉼표로 합니다.")
    @GetMapping("") // 뭐라고 이름 줄까?
    public ResponseEntity<Slice<PostResponse>> getPostsByKeywords(@RequestParam(value = "cursorId") Long cursorId,
                                                                  @RequestParam(value = "keyword", required = false) String keyword,
                                                                  @RequestParam(value = "size") int size) {
        return ResponseEntity.ok().body(postService.findByKeywords(cursorId, keyword, size));
    }

    @Operation(summary = "게시물 상세 조회", description = "게시물 id를 통해 게시물 1개를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getOnePostById(@PathVariable Long id) {
        return ResponseEntity.ok().body(postService.getOnePost(id));
    }

}
