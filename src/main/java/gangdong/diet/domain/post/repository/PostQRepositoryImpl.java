package gangdong.diet.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import gangdong.diet.domain.cookingstep.entity.CookingStep;
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

import static gangdong.diet.domain.cookingstep.entity.QCookingStep.cookingStep;
import static gangdong.diet.domain.ingredient.entity.QIngredient.ingredient;
import static gangdong.diet.domain.nutrient.entity.QNutrient.nutrient;
import static gangdong.diet.domain.post.entity.QPost.post;
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
                .orderBy(post.id.asc())
                .limit(size + 1)  // 다음 페이지 유무 확인을 위해 한 개 더 요청
                .fetch(); // 결과가 없으면 빈 리스트 반환함

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
                .orderBy(post.id.asc())
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
                                        post.id, post.title, post.description, post.thumbnailUrl, post.cookingTime, post.calories,
                                        post.servings, post.youtubeUrl, post.viewCount, post.isApproved
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
    public List<CookingStep> getCookingSetps(Long postId) {
        return queryFactory.selectFrom(cookingStep)
                .join(cookingStep.post, post)
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

    @Override
    public List<PostSearchResponse> getPopularPosts() {
        // 리뷰 평점 평균
        NumberExpression<Double> avgReviewScore = review.rating.avg().coalesce(0.0);

        // 리뷰 개수
        NumberExpression<Long> reviewCount = review.count().coalesce(0L);

        // 스크랩 개수
        NumberExpression<Long> scrapCount = scrap.count().coalesce(0L);

        // 조회수 (Post 엔티티에 존재한다고 가정)
        NumberExpression<Long> viewCount = post.viewCount.coalesce(0L);

        // 가중치를 반영한 인기 점수 계산
        NumberExpression<Double> popularityScore = scrapCount.doubleValue().multiply(0.35)
                .add(reviewCount.doubleValue().multiply(0.35))
                .add(avgReviewScore.multiply(0.2))
                .add(viewCount.doubleValue().multiply(0.1));

        return queryFactory
                .select(Projections.constructor(
                        PostSearchResponse.class, post.id, post.title, post.thumbnailUrl, post.cookingTime, post.calories, post.servings
                ))
                .from(post)
                .leftJoin(review).on(review.post.id.eq(post.id)) // 리뷰와 조인
                .leftJoin(scrap).on(scrap.post.id.eq(post.id)) // 스크랩과 조인
                .groupBy(post.id)
                .orderBy(popularityScore.desc()) // 가중치 적용 후 인기순 정렬
                .limit(10) // 상위 10개 게시물 가져오기
                .fetch();
    }

    private BooleanExpression eqCursorId(Long cursorId) {
        return (cursorId == null) ? null : post.id.gt(cursorId);
    }


    private static final double SIMILARITY_THRESHOLD = 0.3; // 여기 값 조정 가능

    private BooleanExpression containsRecipeNameKeywords(List<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return null;
        }

        return keywords.stream()
                .map(keyword -> post.title.likeIgnoreCase("%" + keyword + "%")  // ILIKE 추가
                        .or(Expressions.numberTemplate(Double.class,
                                        "similarity({0}, {1})", post.title, keyword)
                                .gt(SIMILARITY_THRESHOLD)))
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
                        .where(ingredient.name.in(keywords))
                        .groupBy(postIngredient.post.id)
                        .having(ingredient.name.count().eq((long) keywords.size()))
        );
    }

}

