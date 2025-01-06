package gangdong.diet.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import gangdong.diet.domain.ingredient.entity.Ingredient;
import gangdong.diet.domain.post.dto.PostResponse;
import gangdong.diet.domain.post.dto.PostSearchResponse;
import gangdong.diet.domain.post.entity.*;
import gangdong.diet.domain.review.entity.Review;
import gangdong.diet.domain.scrap.entity.Scrap;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static com.querydsl.jpa.JPAExpressions.select;

import static gangdong.diet.domain.ingredient.entity.QIngredient.ingredient;
import static gangdong.diet.domain.nutrient.entity.QNutrient.nutrient;
import static gangdong.diet.domain.post.entity.QPost.post;
import static gangdong.diet.domain.post.entity.QPostImage.postImage;
import static gangdong.diet.domain.post.entity.QPostIngredient.postIngredient;
import static gangdong.diet.domain.post.entity.QPostNutrient.postNutrient;
import static gangdong.diet.domain.post.entity.QPostTag.postTag;
import static gangdong.diet.domain.review.entity.QReview.review;
import static gangdong.diet.domain.scrap.entity.QScrap.scrap;
import static gangdong.diet.domain.tag.entity.QTag.tag;

@RequiredArgsConstructor
public class PostQRepositoryImpl implements PostQRepository{ // TODO 중복된 게시물 안 뜨게.

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PostSearchResponse> findByRecipeName(Long cursorId, List<String> keywords, int size) {
        return queryFactory
                .selectDistinct(Projections.constructor(
                        PostSearchResponse.class,
                        post.id, post.title, post.thumbnailUrl, post.cookingTime, post.calories, post.servings
                ))
                .from(post)
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
    public List<PostSearchResponse> findByIngredient(Long cursorId, List<String> keywords, int size) {
        return queryFactory
                .selectDistinct(Projections.constructor(
                        PostSearchResponse.class,
                        post.id, post.title, post.thumbnailUrl, post.cookingTime, post.calories, post.servings
                ))
                .from(postIngredient) // Todo postIngredient를 from으로 둘 경우?
                .join(post).on(postIngredient.post.id.eq(post.id))
                .join(ingredient).on(postIngredient.ingredient.id.eq(ingredient.id))

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

    @Override
    public Optional<PostResponse> getOnePost(Long postId) { // MultipleBagFetchException 다중 페치 조인
//        return Optional.ofNullable(queryFactory
//                .selectFrom(post)
//                .join(post.ingredients, postIngredient).fetchJoin()
//                .join(postIngredient.ingredient).fetchJoin()
//
//                .join(post.nutrients, postNutrient).fetchJoin()
//                .join(postNutrient.nutrient, nutrient).fetchJoin()
//
//                .join(post.postTags, postTag).fetchJoin()
//                .join(postTag.tag, tag).fetchJoin()
//
//                .leftJoin(post.scraps, scrap).fetchJoin()
//                .join(post.postImages, postImage).fetchJoin()
//
//                .where(post.id.eq(postId))
//                .fetchOne());
        return Optional.ofNullable(queryFactory
                        .select(
                                Projections.constructor(
                                        PostResponse.class,
                                        post.id, post.title, post.content, post.cookingTime, post.calories, post.servings,
                                        post.thumbnailUrl, post.youtubeUrl, post.isApproved
                                )
                        )
                        .from(post)
                        .where(post.id.eq(postId))
                        .fetchOne()
        );

    }

    @Override
    public List<PostIngredient> getIngredients(Long postId) {
        return queryFactory.selectFrom(postIngredient)
                .join(postIngredient.post, post)
                .join(postIngredient.ingredient, ingredient).fetchJoin()
                .where(post.id.eq(postId))
                .fetch();
    }

    @Override
    public List<PostNutrient> getNutrients(Long postId) {
        return queryFactory.selectFrom(postNutrient)
                .join(postNutrient.post, post)
                .join(postNutrient.nutrient, nutrient).fetchJoin()
                .where(post.id.eq(postId))
                .fetch();
    }

    @Override
    public List<PostTag> getPostTags(Long postId) {
        return queryFactory.selectFrom(postTag)
                .join(postTag.post, post)
                .join(postTag.tag, tag).fetchJoin()
                .where(post.id.eq(postId))
                .fetch();
    }

    @Override
    public List<PostImage> getPostImages(Long postId) {
        return queryFactory.selectFrom(postImage)
                .join(postImage.post, post)
                .where(post.id.eq(postId))
                .fetch();
    }

    @Override
    public List<Review> getReviews(Long postId) {
        return queryFactory.selectFrom(review)
                .join(review.post, post)
                .where(post.id.eq(postId))
                .fetch();
    }

    @Override
    public List<Scrap> getScraps(Long postId) {
        return queryFactory.selectFrom(scrap)
                .join(scrap.post, post)
                .where(post.id.eq(postId))
                .fetch();
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

    @Override
    public List<Post> findAllByIngredient(Ingredient ingredient) {
        return queryFactory.selectFrom(post)
                .join(post.ingredients, postIngredient).fetchJoin()
                .where(postIngredient.ingredient.eq(ingredient))
                .distinct()
                .limit(10)
                .fetch();
    }

    @Override
    public Optional<PostResponse> findOneByKeyword(String keyword) {
        return Optional.ofNullable(queryFactory
                .select(
                        Projections.constructor(
                                PostResponse.class,
                                post.id, post.title, post.content, post.cookingTime, post.calories, post.servings,
                                post.thumbnailUrl, post.youtubeUrl, post.isApproved
                        )
                )
                .from(post)
                .where(post.title.like("%" + keyword.toLowerCase() + "%"))
                .fetchFirst()
        );
    }


}

