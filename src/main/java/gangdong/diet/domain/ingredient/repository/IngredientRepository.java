package gangdong.diet.domain.ingredient.repository;

import gangdong.diet.domain.ingredient.entity.Ingredient;
import gangdong.diet.domain.nutrient.entity.Nutrient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByName(String name);

}
