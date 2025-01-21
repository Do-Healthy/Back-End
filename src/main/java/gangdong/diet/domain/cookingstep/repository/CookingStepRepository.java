package gangdong.diet.domain.cookingstep.repository;

import gangdong.diet.domain.cookingstep.entity.CookingStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookingStepRepository extends JpaRepository<CookingStep, Long> {

    void deleteByPostId(Long postId);

}
