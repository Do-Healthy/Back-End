package gangdong.diet.domain.cookingstep.service;

import gangdong.diet.domain.cookingstep.dto.CookingStepRequest;
import gangdong.diet.domain.cookingstep.entity.CookingStep;
import gangdong.diet.domain.image.service.S3ImageService;
import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.cookingstep.repository.CookingStepRepository;
import gangdong.diet.global.exception.ApiException;
import gangdong.diet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CookingStepService {

    private final S3ImageService s3ImageService;
    private final CookingStepRepository cookingStepRepository;

    @Transactional
    public void registerCookingSteps(List<CookingStepRequest> requests, Post post) {
        requests.forEach(imageRequest -> {
            CookingStep cookingStep = CookingStep.builder().post(post)
                    .imageUrl(imageRequest.getImageUrl())
                    .stepDescription(imageRequest.getStepDescription())
                    .build();

            post.getCookingSteps().add(cookingStep);
        });
    }

    @Transactional
    public void updateCookingSteps(List<CookingStepRequest> requests, Post post) {

        List<CookingStep> cookingSteps = post.getCookingSteps();

        cookingSteps.forEach(cookingStep -> {
            try {
                s3ImageService.deleteFile(cookingStep.getImageUrl());
            } catch (Exception e) {
                log.error("S3 이미지 삭제 중 오류 발생: {}", e.getMessage());
            }
        });
        cookingStepRepository.deleteAll(cookingSteps);

        requests.forEach(imageRequest -> {
            CookingStep cookingStep = CookingStep.builder().post(post)
                    .imageUrl(imageRequest.getImageUrl())
                    .stepDescription(imageRequest.getStepDescription())
                    .build();

            post.getCookingSteps().add(cookingStep);
        });
    }

    @Transactional
    public void deleteCookingSteps(Post post) {
        List<CookingStep> cookingSteps = post.getCookingSteps();

        cookingSteps.forEach(cookingStep -> {
            try {
                s3ImageService.deleteFile(cookingStep.getImageUrl());
            } catch (Exception e) {
                log.error("S3 이미지 삭제 중 오류 발생: {}", e.getMessage());
            }
        });
        cookingStepRepository.deleteAll(cookingSteps);
    }


}
