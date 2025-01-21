package gangdong.diet.domain.ingredient.service;

import gangdong.diet.domain.ingredient.dto.IngredientRequest;
import gangdong.diet.domain.ingredient.entity.Ingredient;
import gangdong.diet.domain.ingredient.repository.IngredientRepository;
import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.post.entity.PostIngredient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Service
public class IngredientServiceImpl implements IngredientService{

    private final IngredientRepository ingredientRepository;

    @Transactional
    @Override
    public void registerIngredient(List<IngredientRequest> requests, Post post) {
        post.getIngredients().clear();
        requests.forEach(ingredientRequest -> {
            Ingredient ingredient = ingredientRepository.findByName(ingredientRequest.getName())
                    .orElseGet(() -> ingredientRepository.save(
                            Ingredient.builder().name(ingredientRequest.getName()).build()));

            PostIngredient postIngredient = PostIngredient.builder()
                    .post(post)
                    .ingredient(ingredient)
                    .amount(ingredientRequest.getAmount())
                    .build();

            post.getIngredients().add(postIngredient);
        });
    }


}
