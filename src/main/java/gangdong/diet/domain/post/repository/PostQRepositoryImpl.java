package gangdong.diet.domain.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import gangdong.diet.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;

import static gangdong.diet.domain.ingredient.entity.QIngredient.ingredient;
import static gangdong.diet.domain.nutrient.entity.QNutrient.nutrient;
import static gangdong.diet.domain.post.entity.QPost.post;
import static gangdong.diet.domain.post.entity.QPostImage.postImage;
import static gangdong.diet.domain.post.entity.QPostIngredient.postIngredient;
import static gangdong.diet.domain.post.entity.QPostNutrient.postNutrient;
import static gangdong.diet.domain.review.entity.QReview.review;

@RequiredArgsConstructor
public class PostQRepositoryImpl implements PostQRepository{ // TODO 중복된 게시물 안 뜨게.

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> findByRecipeName(Long cursorId, List<String> keywords, int size) {
        return queryFactory
                .selectDistinct(post).from(post)
                .join(post.ingredients, postIngredient).fetchJoin()
                .join(postIngredient.ingredient, ingredient).fetchJoin()

                .join(post.nutrients, postNutrient).fetchJoin()
                .join(postNutrient.nutrient, nutrient).fetchJoin()

//                .leftJoin(post.reviews, review).fetchJoin()
                .leftJoin(post.postImages, postImage).fetchJoin()
                .where(
                        eqCursorId(cursorId),
                        containsRecipeNameKeywords(keywords)  // 키워드 조건 추가
                )
                .limit(size + 1)  // 다음 페이지 유무 확인을 위해 한 개 더 요청
                .fetch();

        // 다음 페이지 여부 확인 및 Slice 반환
//        boolean hasNext = results.size() > size;
//        if (hasNext) {
//            results.remove(size);  // 추가된 한 개 제거
//        }
//        return new SliceImpl<>(results, PageRequest.of(0,size), hasNext);
    }



    @Override
    public List<Post> findByIngredient(Long cursorId, List<String> keywords, int size) {
        return queryFactory
                .selectDistinct(post).from(post) // Todo postIngredient를 from으로 둘 경우?
                .leftJoin(post.ingredients, postIngredient).fetchJoin()
                .join(postIngredient.ingredient, ingredient).fetchJoin()

                .join(post.nutrients, postNutrient).fetchJoin()
                .join(postNutrient.nutrient, nutrient).fetchJoin()

//                .leftJoin(post.reviews, review).fetchJoin()
                .join(post.postImages, postImage).fetchJoin()
                .where(
                        eqCursorId(cursorId),
                        findByKeywordOfIngredient(keywords)
                )
                .limit(size + 1)
                .fetch();

//        boolean hasNext = results.size() > size;
//        if (hasNext) {
//            results.remove(size);  // 추가된 한 개 제거
//        }
//        return new SliceImpl<>(results, PageRequest.of(0,size), hasNext);
    }


    private BooleanExpression eqCursorId(Long cursorId) {
        return (cursorId == null) ? null : post.id.gt(cursorId);
    }


    private BooleanExpression containsRecipeNameKeywords(List<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return null;
        }

        // Querydsl의 anyOf()를 활용하여 OR 조건 생성
        return keywords.stream()
                .map(keyword -> post.title.containsIgnoreCase(keyword))
                .reduce(BooleanExpression::or)
                .orElse(post.id.isNull());
    }


    private BooleanExpression findByKeywordOfIngredient(List<String> keywords) { // TODO: 최적화 고려
        if (CollectionUtils.isEmpty(keywords)) {
            return post.id.isNull();
        }

        return post.id.in(
                select(postIngredient.post.id).from(postIngredient)
                        .join(postIngredient.ingredient, ingredient)
                        .where(ingredient.name.in(keywords)) // 한 번의 조건 처리
        );
    }

}

