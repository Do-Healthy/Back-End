package gangdong.diet.domain.nutrient.service;

import gangdong.diet.domain.nutrient.dto.NutrientRequest;
import gangdong.diet.domain.nutrient.entity.Nutrient;
import gangdong.diet.domain.nutrient.repository.NutrientRepository;
import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.post.entity.PostNutrient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NutrientServiceImpl implements NutrientService {

    private final NutrientRepository nutrientRepository;

    @Transactional
    @Override
    public void registerNutrient(List<NutrientRequest> requests, Post post) {
        post.getNutrients().clear();
        requests.forEach(nutrientRequest -> {
            Nutrient nutrient = nutrientRepository.findByName(nutrientRequest.getName())
                    .orElseGet(() -> nutrientRepository.save(
                            Nutrient.builder().name(nutrientRequest.getName()).build()
                    ));

            PostNutrient postNutrient = PostNutrient.builder()
                    .post(post)
                    .nutrient(nutrient)
                    .amount(nutrientRequest.getAmount())
                    .build();

            post.getNutrients().add(postNutrient);
        });
    }
}
