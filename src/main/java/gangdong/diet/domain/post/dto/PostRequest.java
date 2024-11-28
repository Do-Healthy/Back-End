package gangdong.diet.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(title = "게시물 등록 요청 API")
@Getter
public class PostRequest {

    private String title;
    private String content;
    private String youtubeUrl;


}
