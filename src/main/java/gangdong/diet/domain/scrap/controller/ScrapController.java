package gangdong.diet.domain.scrap.controller;

import gangdong.diet.domain.scrap.service.ScrapService;
import gangdong.diet.global.auth.MemberDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "스크랩 API")
@RequiredArgsConstructor
@RequestMapping("/api/scraps")
@RestController
public class ScrapController {

    private final ScrapService scrapService;

    @PatchMapping("/{postId}")
    public ResponseEntity editScrap(@PathVariable Long postId, @AuthenticationPrincipal MemberDetails memberDetails) {
        scrapService.editScrap(postId, memberDetails.getUsername());

        return ResponseEntity.ok().body("스크랩 작업 완료");
    }

}
