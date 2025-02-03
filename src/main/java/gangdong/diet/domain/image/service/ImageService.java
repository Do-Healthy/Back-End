package gangdong.diet.domain.image.service;

import gangdong.diet.domain.cookingstep.repository.CookingStepRepository;
import gangdong.diet.global.exception.ApiException;
import gangdong.diet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageService {

    private final S3ImageService s3ImageService;

    public List<String> uploadImages(List<MultipartFile> images) {

        List<String> uploadedImageUrls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            try {
                for (int i = 0; i < images.size(); i++) {
                    MultipartFile image = images.get(i);
                    // 이미지 업로드
                    String imageUrl = s3ImageService.uploadFile(image, "post/afterSaved/");
                    uploadedImageUrls.add(imageUrl);
                }
            } catch (Exception e) {
                // 실패한 경우 업로드된 이미지 삭제
                if (!uploadedImageUrls.isEmpty()) {
                    s3ImageService.cleanupUploadedFiles(uploadedImageUrls);
                }
                throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }

        return uploadedImageUrls;
    }

}
