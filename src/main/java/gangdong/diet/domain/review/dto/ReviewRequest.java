package gangdong.diet.domain.review.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class ReviewRequest {

    private String content;
    private int rating;

}
