package gangdong.diet.domain.post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.time.Duration;
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
    public Slice<PostSearchResponse> findByKeywords(String category, Long cursorId, String keywords, int size) {

        cursorId = cursorId == null ? 0 : cursorId;

//        if (!StringUtils.hasText(keywords)) { // keywords가 null, 빈 문자열, 공백일 경우
//            return findAllPosts(cursorId, size); // 전체 조회 메서드 호출
//        }

        // 쉼표로 단어를 나누고 양 끝의 공백 제거
        List<String> finalKeywords = Arrays.stream(keywords.split(","))
                .map(String::trim) // 각 단어의 양 끝 공백 제거
                .filter(s -> !s.isEmpty()) // 비어있는 단어 제거
                .collect(Collectors.toList());

        // Repository 호출
        List<PostSearchResponse> searchResults = new ArrayList<>();

        if ("recipe".equals(category) || !StringUtils.hasText(keywords)) {
            searchResults = postRepository.findByRecipeName(cursorId, finalKeywords, size);
        } else if ("ingredient".equals(category)) {
            searchResults = postRepository.findByIngredient(cursorId, finalKeywords, size);
        } else {
            throw new ApiException(ErrorCode.INVALID_CATEGORY);
        }

        // 중복 제거
        searchResults = searchResults.stream()
                .distinct()
                .collect(Collectors.toList());

        Member member = isLoggedIn();

        // 스크랩 여부 확인
        List<Long> scrappedPostIds;
        if (member != null) {
            scrappedPostIds = isScrapped(searchResults, member.getId());
        } else {
            scrappedPostIds = List.of();
        }


        List<PostSearchResponse> finalResults = searchResults.stream().map(sr -> {
                    sr.setIsScrapped(scrappedPostIds.contains(sr.getId()));
                    return sr;
                })
                .collect(Collectors.toList());

        // 페이징 처리 및 Slice 반환
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

        // JSON 직렬화를 위한 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // 레디스에서 게시물 가져오기
        PostResponse postResponse = null;
        try { // todo 에러에 대한 대비
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

        // 캐싱 되어있지 않은 경우 DB로 부터 받아옴.
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
            // 조회수 증가를 원자적으로 수행
            Long incrementedViewCount = redisTemplate.opsForValue().increment(redisViewKey);

            if (incrementedViewCount == 1) {
                // Redis에 해당 키가 처음 생성된 경우, 기존 DB 조회수를 반영해야 함
                redisTemplate.opsForValue().set(redisViewKey, postResponse.getViewCount() + 1);
                incrementedViewCount = postResponse.getViewCount() + 1;
            }

            postResponse.setViewCount(incrementedViewCount);

        } catch (Exception e) {
            log.warn("조회수를 Redis에서 처리하는 중 오류 발생. 기본 조회수로 설정. postId: {}, 오류: {}", postId, e.getMessage());
            postResponse.setViewCount(postResponse.getViewCount()); // 예외 발생 시 기존 조회수 유지
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

    @Override
    @Transactional // Todo 나중에 지워야함.
    public Long savePostAll(List<PostRequest> postRequests, MemberDetails memberDetails) {

        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        for (PostRequest postRequest : postRequests) {
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
        }

        return null;
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
            Long viewCount = (Long) redisTemplate.opsForValue().get(redisViewKey);
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
    public void deletePost(Long postId, MemberDetails memberDetails) {
        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        try {
            s3ImageService.deleteFile(post.getThumbnailUrl());
            cookingStepService.deleteCookingSteps(post);
        } catch (Exception e) {
            log.error("S3 이미지 삭제 중 오류 발생: {}", e.getMessage());
            throw new ApiException(ErrorCode.S3_DELETE_FAILED);
        }

        postRepository.delete(post);
    }

    @Override
    public List<PostSearchResponse> getPopularPosts() {
        String redisPopularPostsKey = "post:popular";

        ObjectMapper objectMapper = new ObjectMapper();

        List<PostSearchResponse> postSearchResponses = new ArrayList<>();

        try { // todo 에러에 대한 대비
            String cachedPostResponse = (String) redisTemplate.opsForValue().get(redisPopularPostsKey);
            if (cachedPostResponse != null) {
                postSearchResponses = objectMapper.readValue(cachedPostResponse, new TypeReference<List<PostSearchResponse>>() {});
            }
        } catch (RedisException e) {
            log.error("Redis에서 캐시된 인기게시물 데이터를 가져오던 중 오류 발생. 오류: {}", e.getMessage());
            postSearchResponses = postRepository.getPopularPosts();
            // todo throw 안 넣어도 되겠는지.
        } catch (JsonProcessingException e) {
            log.error("캐시된 인기게시물 데이터를 JSON으로 변환 중 오류 발생. 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Redis로부터 로드 하는 작업중 에러가 발생했습니다.", e);
        }

        return postSearchResponses;

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


    @Scheduled(fixedRate = 21600000) // 매 6시간마다 실행
    public void syncViewCounts() {
        Set<String> keys = redisTemplate.keys("post:view:*");
        if (keys != null) {
            for (String key : keys) {
                Long postId = Long.valueOf(key.split(":")[2]);
                Object viewCountObj = redisTemplate.opsForValue().get(key);
                Long viewCount = (viewCountObj != null) ? Long.parseLong(viewCountObj.toString()) : 0L;


                // 데이터베이스에 반영
                Post post = postRepository.findById(postId).orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
                post.setViewCount(viewCount);
                postRepository.save(post);

                // Redis 데이터 초기화
                redisTemplate.delete(key);

            }
        }
        log.info("조회수 DB와 동기화");
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void updatePopularPostsCache() {
        String redisPopularPostsKey = "post:popular";
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<PostSearchResponse> popularPosts = postRepository.getPopularPosts();
            String serializedData = objectMapper.writeValueAsString(popularPosts);
            redisTemplate.opsForValue().set(redisPopularPostsKey, serializedData, Duration.ofDays(1)); // 24시간 유지
            log.info("인기 게시물 캐시 업데이트 완료.");
        } catch (JsonProcessingException e) {
            log.error("인기 게시물 데이터를 JSON으로 변환하는 중 오류 발생.", e);
        } catch (Exception e) {
            log.error("인기 게시물 캐시 업데이트 중 오류 발생.", e);
        }

        log.info("인기 게시물 캐싱 완료");
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
