package gangdong.diet.domain.post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gangdong.diet.domain.cookingstep.dto.CookingStepResponse;
import gangdong.diet.domain.cookingstep.entity.CookingStep;
import gangdong.diet.domain.cookingstep.service.CookingStepService;
import gangdong.diet.domain.image.service.S3ImageService;
import gangdong.diet.domain.ingredient.entity.Ingredient;
import gangdong.diet.domain.ingredient.service.IngredientService;
import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.member.repository.MemberRepository;
import gangdong.diet.domain.nutrient.service.NutrientService;
import gangdong.diet.domain.post.dto.*;
import gangdong.diet.domain.post.entity.*;
import gangdong.diet.domain.cookingstep.repository.CookingStepRepository;
import gangdong.diet.domain.post.repository.PostIngredientRepository;
import gangdong.diet.domain.post.repository.PostNutrientRepository;
import gangdong.diet.domain.post.repository.PostRepository;
import gangdong.diet.domain.review.entity.Review;
import gangdong.diet.domain.scrap.entity.Scrap;
import gangdong.diet.domain.scrap.repository.ScrapRepository;
import gangdong.diet.domain.survey.entity.Survey;
import gangdong.diet.domain.survey.repository.SurveyRepository;
import gangdong.diet.domain.tag.service.TagService;
import gangdong.diet.global.auth.MemberDetails;
import gangdong.diet.global.exception.ApiException;
import gangdong.diet.global.exception.ErrorCode;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final PostIngredientRepository postIngredientRepository;
    private final PostNutrientRepository postNutrientRepository;
    private final S3ImageService s3ImageService;
    private final CookingStepService cookingStepService;
    private final MemberRepository memberRepository;
    private final CookingStepRepository cookingStepRepository;
    private final IngredientService ingredientService;
    private final NutrientService nutrientService;
    private final ScrapRepository scrapRepository;
    private final TagService tagService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SurveyRepository surveyRepository;

    @Transactional(readOnly = true)
    @Override
    public Slice<PostSearchResponse> findByKeywords(Long cursorId, String keywords, int size) {

        cursorId = cursorId == null ? 0 : cursorId;

        if (!StringUtils.hasText(keywords)) { // keywords가 null, 빈 문자열, 공백일 경우
            return findAllPosts(cursorId, size); // 전체 조회 메서드 호출
        }

        // 공백 제거 후 쉼표 기준으로 리스트 생성
        String newKeywords = keywords.replaceAll(" ", "");
        StringTokenizer tokenizer = new StringTokenizer(newKeywords, ",");
        List<String> finalKeywords = new ArrayList<>();
        while(tokenizer.hasMoreTokens()) {
            finalKeywords.add(tokenizer.nextToken());
        }

        // Repository 호출
        List<PostSearchResponse> recipeResults = postRepository.findByRecipeName(cursorId, finalKeywords, size);
        List<PostSearchResponse> ingredientResults = postRepository.findByIngredient(cursorId, finalKeywords, size);

        // 합친 후 중복 제거
        Set<PostSearchResponse> mergedResultsSet = new LinkedHashSet<>(recipeResults);
        mergedResultsSet.addAll(ingredientResults);

        Member member = isLoggedIn();

        // 스크랩 여부 확인
        List<Long> scrappedPostIds;
        if (member != null) {
            scrappedPostIds = isScrapped(mergedResultsSet, member.getId());
        } else {
            scrappedPostIds = List.of();
        }


        List<PostSearchResponse> finalResults = mergedResultsSet.stream().map(psr -> {
                    psr.setIsScrapped(scrappedPostIds.contains(psr.getId()));
                    return psr;
                })
                .collect(Collectors.toList());

        // 페이징 처리 및 Slice 반환
        boolean hasNext = finalResults.size() > size;
        if (hasNext) {
            finalResults = finalResults.subList(0, size); // 초과 데이터 제거
        }

        return new SliceImpl<>(finalResults, Pageable.ofSize(size), hasNext);
    }

    @Transactional(readOnly = true)
    @Override
    public Slice<PostSearchResponse> findAllPosts(Long cursorId, int size) {
        List<PostSearchResponse> posts = postRepository.findByRecipeName(cursorId, null, size);

        Member member = isLoggedIn();

        // 스크랩 여부 확인
        List<Long> scrappedPostIds;
        if (member != null) {
            scrappedPostIds = isScrapped(posts, member.getId());
        } else {
            scrappedPostIds = List.of();
        }


//        List<PostSearchResponse> finalResults = posts.stream()
//                .map(post -> PostSearchResponse.builder()
//                        .post(post)
//                        .isScrapped(scrappedPostIds.contains(post.getId()))
//                        .build())
//                .toList();
        List<PostSearchResponse> finalResults = posts.stream().map(post -> {
                    post.setIsScrapped(scrappedPostIds.contains(post.getId()));
                    return post;
                })
                .collect(Collectors.toList());


        boolean hasNext = finalResults.size() > size;
        if (hasNext) {
            finalResults = finalResults.subList(0, size); // 초과 데이터 제거
        }

        return new SliceImpl<>(finalResults, Pageable.ofSize(size), hasNext);
    }

    // 레디스 활용한 게시물 상세보기
    @Transactional(readOnly = true)
    @Override
    public PostResponse getOnePost(Long postId) {
        String redisPostKey = "post:details:" + postId;
        String redisViewKey = "post:view:" + postId;

        // ObjectMapper for JSON serialization
        ObjectMapper objectMapper = new ObjectMapper();

        // Try to get cached post details from Redis
        PostResponse postResponse = null;
        try {
            String cachedPostResponse = (String) redisTemplate.opsForValue().get(redisPostKey);
            if (cachedPostResponse != null) {
                postResponse = objectMapper.readValue(cachedPostResponse, PostResponse.class);
            }
        } catch (RedisException e) {
            log.error("Redis에서 캐시된 게시물 데이터를 가져오던 중 오류 발생. postId: {}, 오류: {}", postId, e.getMessage());
            // todo throw 안 넣어도 되겠는지.
        } catch (JsonProcessingException e) {
            log.error("캐시된 게시물 데이터를 JSON으로 변환 중 오류 발생. postId: {}, 오류: {}", postId, e.getMessage());
        } catch (Exception e) {
            log.error("Redis로부터 로드 하는 작업중 에러가 발생했습니다.", e);
        }

        // If not cached, retrieve from database
        if (postResponse == null) {
            postResponse = postRepository.getOnePost(postId).orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

            List<PostIngredient> ingredients = postRepository.getIngredients(postId);
            List<PostNutrient> nutrients = postRepository.getNutrients(postId);
            List<PostTag> postTag = postRepository.getPostTags(postId);
            List<CookingStep> cookingStep = postRepository.getCookingSetps(postId);

            postResponse.getIngredients().addAll(ingredients.stream()
                    .map(i -> PostIngredientResponse.builder().postIngredient(i).build())
                    .toList());
            postResponse.getNutrients().addAll(nutrients.stream()
                    .map(n -> PostNutrientResponse.builder().postNutrient(n).build())
                    .toList());
            postResponse.getTagName().addAll(postTag.stream().map(pt -> pt.getTag().getName()).toList());
            postResponse.getCookingSteps().addAll(cookingStep.stream()
                    .map(cs -> CookingStepResponse.builder().cookingStep(cs).build())
                    .toList());

            try {
                String jsonPostResponse = objectMapper.writeValueAsString(postResponse);
                redisTemplate.opsForValue().set(redisPostKey, jsonPostResponse, 1, TimeUnit.HOURS);
            } catch (JsonProcessingException e) {
                log.error("게시물 데이터를 JSON으로 변환 중 오류 발생. postId: {}, 데이터: {}", postId, postResponse, e);
            } catch (RedisException e) {
                log.warn("Redis에 게시물 데이터를 저장하는 중 오류 발생. postId: {}, 오류: {}", postId, e.getMessage());
            }
        }

        List<Scrap> scraps = postRepository.getScraps(postId);
        List<Review> reviews = postRepository.getReviews(postId); // batch 쓸지, 이거 쓸지
        postResponse.setReview(reviews);
        postResponse.setScrapCount(scraps.size());

        try {
            Integer redisViewCount = (Integer) redisTemplate.opsForValue().get(redisViewKey);
            if (redisViewCount == null) {
                redisViewCount = postResponse.getViewCount();
                redisTemplate.opsForValue().set(redisViewKey, redisViewCount);
            }
            // Redis 조회수 증가
            Long incrementedViewCount = redisTemplate.opsForValue().increment(redisViewKey);
            postResponse.setViewCount(incrementedViewCount != null ? incrementedViewCount.intValue() : postResponse.getViewCount());
        } catch (RedisException e) {
            log.warn("조회수를 Redis에서 처리하는 중 오류 발생. 기본 조회수로 설정. postId: {}, 오류: {}", postId, e.getMessage());
        }

        Member member = isLoggedIn();
        Boolean isScrapped = member == null ? false : scrapRepository.existsByMemberIdAndPostId(member.getId(), postId);

        postResponse.setIsScrapped(isScrapped);

        return postResponse;
    }

//     레디스 사용 안 한 게시물 상세보기
//    @Transactional(readOnly = true)
//    @Override
//    public PostResponse getOnePost(Long postId) {
//        String redisViewKey = "post:view:" + postId;
//
//        PostResponse postResponse = postRepository.getOnePost(postId).orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
//
//        try {
//            Integer redisViewCount = (Integer) redisTemplate.opsForValue().get(redisViewKey);
//            if (redisViewCount == null) {
//                redisViewCount = postResponse.getViewCount();
//                redisTemplate.opsForValue().set(redisViewKey, redisViewCount);
//            }
//
//            Long incrementedViewCount = redisTemplate.opsForValue().increment(redisViewKey);
//            postResponse.setViewCount(incrementedViewCount != null ? incrementedViewCount.intValue() : postResponse.getViewCount());
//        } catch (RedisException e) {
//            log.warn("조회수를 Redis에서 처리하는 중 오류 발생. 기본 조회수로 설정. postId: {}, 오류: {}", postId, e.getMessage());
//        }
//
//        List<PostIngredient> ingredients = postRepository.getIngredients(postId);
//        List<PostNutrient> nutrients = postRepository.getNutrients(postId);
//        List<PostTag> postTag = postRepository.getPostTags(postId);
//        List<PostImage> postImage = postRepository.getPostImages(postId);
//        List<Review> reviews = postRepository.getReviews(postId);
//        List<Scrap> scraps = postRepository.getScraps(postId);
//
//        postResponse.getIngredients().addAll(ingredients.stream()
//                .map(i -> PostIngredientResponse.builder().postIngredient(i).build())
//                .toList());
//        postResponse.getNutrients().addAll(nutrients.stream()
//                .map(n -> PostNutrientResponse.builder().postNutrient(n).build())
//                .toList());
//        postResponse.getTagName().addAll(postTag.stream().map(pt -> pt.getTag().getName()).toList());
//        postResponse.getPostImages().addAll(postImage.stream()
//                .map(pi -> PostImageResponse.builder().postImage(pi).build())
//                .toList());
//        postResponse.setReview(reviews);
//        postResponse.setScrapCount(scraps.size());
//
//        Member member = isLoggedIn();
//        Boolean isScrapped = member == null ? false : scrapRepository.existsByMemberIdAndPostId(member.getId(), postId);
//
//        postResponse.setIsScrapped(isScrapped);
//
//        return postResponse;
//    }

    @Transactional
    @Override
    public Long savePost(PostRequest postRequest, MemberDetails memberDetails) {

        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .title(postRequest.getTitle())
                .description(postRequest.getDescription())
                .cookingTime(postRequest.getCookingTime())
                .calories(postRequest.getCalories())
                .servings(postRequest.getServings())
                .thumbnailUrl(postRequest.getThumbnailUrl())
                .youtubeUrl(postRequest.getYoutubeUrl())
                .isApproved(false)
                .member(member)
                .build();

        postRepository.save(post);

        ingredientService.registerIngredient(postRequest.getIngredients(), post);
        nutrientService.registerNutrient(postRequest.getNutrients(), post);
        tagService.registerTags(postRequest.getTags(), post);
        cookingStepService.registerCookingSteps(postRequest.getCookingSteps(), post);

        return post.getId();
    }

    @Transactional
    @Override
    public PostResponse updatePost(Long postId, PostRequest postRequest, MemberDetails memberDetails) {
        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 게시물 기본 정보 수정
        post.setTitle(postRequest.getTitle());
        post.setDescription(postRequest.getDescription());
        post.setCookingTime(postRequest.getCookingTime());
        post.setCalories(postRequest.getCalories());
        post.setServings(postRequest.getServings());
        post.setThumbnailUrl(postRequest.getThumbnailUrl());
        post.setYoutubeUrl(postRequest.getYoutubeUrl());

        // 재료 업데이트
        ingredientService.registerIngredient(postRequest.getIngredients(), post);

        // 영양 정보 업데이트
        nutrientService.registerNutrient(postRequest.getNutrients(), post);

        // 태그 업데이트
        tagService.registerTags(postRequest.getTags(), post);

        // 이미지 업데이트
        cookingStepService.updateCookingSteps(postRequest.getCookingSteps(), post);

        Boolean isScrapped = member == null ? false : scrapRepository.existsByMemberIdAndPostId(member.getId(), postId);

        // redis에 있는 데이터 삭제
        String redisPostKey = "post:details:" + postId;
        String redisViewKey = "post:view:" + postId;

        try {// todo 이벤트 리스너로 뺄 지 고민, 여기서 삭제 혹은 조회수 저장하다가 데이터 날아가면 조회수 누락됨.
            Integer viewCount = (Integer) redisTemplate.opsForValue().get(redisViewKey);
            if (viewCount != null) {
                post.setViewCount(viewCount);
            }

            redisTemplate.delete(redisPostKey);
            redisTemplate.delete(redisViewKey);
        } catch (RedisException e) {
            log.warn("Redis에 게시물 데이터를 삭제하는 중 오류 발생. postId: {}, 오류: {}", postId, e.getMessage());
        }

        return PostResponse.builder()
                .post(post)
                .isScrapped(isScrapped)
                .build();
    }

    @Transactional
    @Override
    public void deletePost(Long postId, MemberDetails memberDetails) { // TODO 사용자 검증 추가
        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        s3ImageService.deleteFile(post.getThumbnailUrl());
        post.getCookingSteps().forEach(cookingStep -> s3ImageService.deleteFile(cookingStep.getImageUrl()));

        postRepository.deleteById(postId);
    }

    private Member isLoggedIn() { //TODO 유저 합쳐지면 이거 바꿔야함   * 그리고 이거 고민인게 이렇게 스크랩 한 게시물 찾는게 별로 안 좋은것 같음.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return memberRepository.findByMemberEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        }

        return null; // 인증되지 않았을 경우
    }

    private List<Long> isScrapped(Collection<PostSearchResponse> posts, Long memberId) {

        List<Long> postIds = posts.stream().map(PostSearchResponse::getId).toList();
        List<Long> scrappedPostIds = scrapRepository.findScrappedPostIds(memberId, postIds);

        return scrappedPostIds;
    }


    @Scheduled(fixedRate = 3600000) // 매 1시간마다 실행
    public void syncViewCounts() {
        Set<String> keys = redisTemplate.keys("post:view:*");
        if (keys != null) {
            for (String key : keys) {
                Long postId = Long.valueOf(key.split(":")[2]);
                Integer viewCount = Integer.parseInt(redisTemplate.opsForValue().get(key).toString());

                // 데이터베이스에 반영
                Post post = postRepository.findById(postId).orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
                post.setViewCount(viewCount); // 엔티티에 viewCount 필드 추가 필요
                postRepository.save(post);

                // Redis 데이터 초기화
                redisTemplate.delete(key);
            }
        }
    }

    // 관련 추천 게시물 리스트
    @Override
    @Transactional
    public List<PostSearchResponse> findRelatedPosts(Long id) {
        Post getPost = postRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        Ingredient ingredient = getPost.getIngredients().get(1).getIngredient();
        List<Post> posts = postRepository.findAllByIngredient(ingredient);

        List<PostSearchResponse> postSearchResponseList = new ArrayList<>();

        for (Post post : posts) {
            postSearchResponseList.add(new PostSearchResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getThumbnailUrl(),
                    post.getCookingTime(),
                    post.getCalories(),
                    post.getServings()
            ));
        }

        return postSearchResponseList;
    }

    // 설문지 토대로 밀프랩 추천
    @Override
    @Transactional
    public PostResponse getSurveyPost(MemberDetails memberDetails) {

        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Survey survey = surveyRepository.findByMemberId(member.getId()).orElseThrow(() -> new ApiException(ErrorCode.SURVEY_NOT_FOUND));
        List<String> totalList = survey.getTotalList();
        String surveyResult ="";
        for(String s : totalList){
            surveyResult += s+",";
        }

        String diet = defineDiets().get(surveyResult);
        PostResponse postResponse = postRepository.findOneByKeyword(diet).orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        return postResponse;
    }

    @Override
    @Transactional
    public List<PostRedis> getRecommendPosts() {
        List<Post> posts = postRepository.findRecommendPosts();
        List<PostRedis> postRedisList = new ArrayList<>();

         posts.stream().forEach(post ->
                postRedisList.add(
                PostRedis.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .thumbnailUrl(post.getThumbnailUrl())
                .cookingTime(post.getCookingTime())
                .calories(post.getCalories())
                .servings(post.getServings())
                .youtubeUrl(post.getYoutubeUrl())
                .viewCount(post.getViewCount())
                .ingredients(PostRedis.builder().build().getIngredients())
                .nutrients(PostRedis.builder().build().getNutrients())
                .cookingSteps(PostRedis.builder().build().getCookingSteps())
                .reviews(PostRedis.builder().build().getReviews())
                .build()
                )
         );

        redisTemplate.opsForValue().set("post:view:", postRedisList);


        return postRedisList;
    }

    // 조건 정의
    public static Map<String, String> defineDiets() {
        Map<String, String> dietMap = new HashMap<>();

        dietMap.put("소화문제", "밀프랩1");
        dietMap.put("체중문제", "밀프랩2");
        dietMap.put("피부모발", "밀프랩3");
        dietMap.put("심혈관혈압", "밀프랩4");
        dietMap.put("면역감염", "밀프랩5");
        dietMap.put("소화문제,체중문제", "밀프랩6");
        dietMap.put("소화문제,피부모발", "밀프랩7");
        dietMap.put("소화문제,심혈관혈압", "밀프랩8");
        dietMap.put("소화문제,면역감염", "밀프랩9");
        dietMap.put("체중문제,피부모발", "밀프랩10");
        dietMap.put("체중문제,심혈관혈압", "밀프랩11");
        dietMap.put("체중문제,면역감염", "밀프랩12");
        dietMap.put("피부모발,심혈관혈압", "밀프랩13");
        dietMap.put("피부모발,면역감염", "밀프랩14");
        dietMap.put("심혈관혈압,면역감염", "밀프랩15");
        dietMap.put("소화문제,체중문제,피부모발", "밀프랩16");
        dietMap.put("소화문제,체중문제,심혈관혈압", "밀프랩17");
        dietMap.put("소화문제,체중문제,면역감염", "밀프랩18");
        dietMap.put("소화문제,피부모발,심혈관혈압", "밀프랩19");
        dietMap.put("소화문제,피부모발,면역감염", "밀프랩20");
        dietMap.put("소화문제,심혈관혈압,면역감염", "밀프랩21");
        dietMap.put("체중문제,피부모발,심혈관혈압", "밀프랩22");
        dietMap.put("체중문제,피부모발,면역감염", "밀프랩23");
        dietMap.put("체중문제,심혈관혈압,면역감염", "밀프랩24");
        dietMap.put("피부모발,심혈관혈압,면역감염", "밀프랩25");
        dietMap.put("소화문제,체중문제,피부모발,심혈관혈압", "밀프랩26");
        dietMap.put("소화문제,체중문제,피부모발,면역감염", "밀프랩27");
        dietMap.put("소화문제,체중문제,심혈관혈압,면역감염", "밀프랩28");
        dietMap.put("소화문제,피부모발,심혈관혈압,면역감염", "밀프랩29");
        dietMap.put("체중문제,피부모발,심혈관혈압,면역감염", "밀프랩30");
        dietMap.put("소화문제,체중문제,피부모발,심혈관혈압,면역감염", "밀프랩31");

        return dietMap;
    }
}
