package gangdong.diet.domain.post.dto;

import gangdong.diet.domain.post.entity.PostImage;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostImageResponse {

    private final Long id;
    private final String imageUrl;
    private final String description;
    private static final String IMAGE_URL_PREFIX = "https://ec2주소";

    @Builder
    public PostImageResponse(PostImage postImage) {
        this.id = postImage.getId();
        this.imageUrl = postImage.getImageUrl() == null ? "" : IMAGE_URL_PREFIX + postImage.getImageUrl();
        this.description = postImage.getDescription();
    }
}
