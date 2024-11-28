package gangdong.diet.domain.review.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class ReviewRequest {

    private Long postId;
    private String content;
    private int rating;
    private List<MultipartFile> images;

}
