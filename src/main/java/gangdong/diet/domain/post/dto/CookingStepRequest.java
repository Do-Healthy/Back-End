package gangdong.diet.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Deprecated
@Getter
public class CookingStepRequest {

    @NotBlank
    private MultipartFile image;
    @NotBlank
    private String description;


}
