package gangdong.diet.domain.post.repository;

import gangdong.diet.domain.cookingstep.entity.CookingStep;
import gangdong.diet.domain.post.dto.PostResponse;
import gangdong.diet.domain.post.dto.PostSearchResponse;
import gangdong.diet.domain.post.entity.*;
import gangdong.diet.domain.review.entity.Review;
import gangdong.diet.domain.scrap.entity.Scrap;

import java.util.List;
import java.util.Optional;

public interface PostQRepository {

    List<PostSearchResponse> findByRecipeName(Long cursorId, List<String> keywords, int size);

    List<PostSearchResponse> findByIngredient(Long cursorId, List<String> keywords, int size);

    Optional<PostResponse> getOnePost(Long cursorId);

    List<PostIngredient> getIngredients(Long postId);

    List<PostNutrient> getNutrients(Long postId);

    List<PostTag> getPostTags(Long postId);

    List<CookingStep> getCookingSetps(Long postId);

    List<Review> getReviews(Long postId);

    public List<Scrap> getScraps(Long postId);

//    List<Post> findAllPosts(Long cursorId, int size);

}
