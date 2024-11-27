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
@RequestMapping("/scrap")
@RestController
public class ScrapController {

    private final ScrapService scrapService;

    @PostMapping // 이거 근데 한 uri로 매핑해야하는지.
    public ResponseEntity addScrap(@RequestParam Long postId,
                                   @AuthenticationPrincipal MemberDetails memberDetails) { // 여기서 유저 가져오기?

        scrapService.saveScrap(postId, memberDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity deleteScrap(@RequestParam Long postId, @AuthenticationPrincipal MemberDetails memberDetails) {
        scrapService.deleteScrap(postId, memberDetails.getUsername());

        return ResponseEntity.ok().build();
    }

}
