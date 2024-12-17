package gangdong.diet.domain.ingredient.service;

import gangdong.diet.domain.ingredient.dto.IngredientRequest;
import gangdong.diet.domain.post.entity.Post;

import java.util.List;

public interface IngredientService {

    void registerIngredient(List<IngredientRequest> ingredientRequest, Post post);

}
