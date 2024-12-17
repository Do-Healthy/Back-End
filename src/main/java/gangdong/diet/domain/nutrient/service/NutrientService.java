package gangdong.diet.domain.nutrient.service;

import gangdong.diet.domain.nutrient.dto.NutrientRequest;
import gangdong.diet.domain.post.entity.Post;

import java.util.List;

public interface NutrientService {

    void registerNutrient(List<NutrientRequest> nutrientRequests, Post post);

}
