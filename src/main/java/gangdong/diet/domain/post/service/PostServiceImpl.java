package gangdong.diet.domain.post.service;

import gangdong.diet.domain.image.service.S3ImageService;
import gangdong.diet.domain.ingredient.dto.IngredientRequest;
import gangdong.diet.domain.ingredient.entity.Ingredient;
import gangdong.diet.domain.ingredient.repository.IngredientRepository;
import gangdong.diet.domain.ingredient.service.IngredientService;
import gangdong.diet.domain.member.dto.MemberResponse;
import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.member.repository.MemberRepository;
import gangdong.diet.domain.nutrient.dto.NutrientRequest;
import gangdong.diet.domain.nutrient.entity.Nutrient;
import gangdong.diet.domain.nutrient.repository.NutrientRepository;
import gangdong.diet.domain.nutrient.service.NutrientService;
import gangdong.diet.domain.post.dto.*;
import gangdong.diet.domain.post.entity.*;
import gangdong.diet.domain.post.repository.PostImageRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final PostIngredientRepository postIngredientRepository;
    private final PostNutrientRepository postNutrientRepository;
    private final S3ImageService s3ImageService;
    private final MemberRepository memberRepository;
    private final PostImageRepository postImageRepository;
    private final IngredientService ingredientService;
    private final NutrientService nutrientService;
    private final ScrapRepository scrapRepository;
    private final TagService tagService;
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

    @Transactional(readOnly = true)
    @Override
    public PostResponse getOnePost(Long postId) {
        PostResponse postResponse = postRepository.getOnePost(postId).orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        List<PostIngredient> ingredients = postRepository.getIngredients(postId);
        List<PostNutrient> nutrients = postRepository.getNutrients(postId);
        List<PostTag> postTag = postRepository.getPostTags(postId);
        List<PostImage> postImage = postRepository.getPostImages(postId);
        List<Review> reviews = postRepository.getReviews(postId);
        List<Scrap> scraps = postRepository.getScraps(postId);

        postResponse.getIngredients().addAll(ingredients.stream()
                .map(i -> PostIngredientResponse.builder().postIngredient(i).build())
                .toList());
        postResponse.getNutrients().addAll(nutrients.stream()
                .map(n -> PostNutrientResponse.builder().postNutrient(n).build())
                .toList());
        postResponse.getTagName().addAll(postTag.stream().map(pt -> pt.getTag().getName()).toList());
        postResponse.getPostImages().addAll(postImage.stream()
                .map(pi -> PostImageResponse.builder().postImage(pi).build())
                .toList());
        postResponse.setReview(reviews);
        postResponse.setScrapCount(scraps.size());

        Member member = isLoggedIn();
        Boolean isScrapped = member == null ? false : scrapRepository.existsByMemberIdAndPostId(member.getId(), postId);

        postResponse.setIsScrapped(isScrapped);

        return postResponse;
    }

    @Transactional
    @Override
    public Long savePost(PostRequest postRequest, MultipartFile thumbnail, List<MultipartFile> postImages, MemberDetails memberDetails) {

        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .cookingTime(postRequest.getCookingTime())
                .calories(postRequest.getCalories())
                .servings(postRequest.getServings())
                .youtubeUrl(postRequest.getYoutubeUrl())
                .isApproved(false)
                .member(member)
                .build();

        postRepository.save(post);

        if (thumbnail != null) {
            try {
                post.setThumbnailUrl(s3ImageService.uploadFile(thumbnail, "post/" + post.getId() + "/"));
            } catch (Exception e) {
                throw new ApiException(ErrorCode.THUMBNAIL_UPLOAD_FAIL);
            }
        }

        ingredientService.registerIngredient(postRequest.getIngredients(), post);
        nutrientService.registerNutrient(postRequest.getNutrients(), post);
        tagService.registerTags(postRequest.getTags(), post);

//        try {
//            List<String> imageUrls = s3ImageService.uploadFile(postImagesRequest, "post/" + post.getId() + "/");
//            List<PostImage> postImages = imageUrls.stream()
//                    .map(url -> PostImage.builder().imageUrl(url).post(post).build())
//                    .collect(Collectors.toList());
//            post.getPostImages().addAll(postImages);
//        } catch (Exception e) {
//
//            throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
//        }

        // 이미지 업로드
        if (postImages != null && !postImages.isEmpty()) {
            List<String> uploadedImageUrls = new ArrayList<>();
            try {
                List<PostImage> postImageList = new ArrayList<>();

                for (int i = 0; i < postImages.size(); i++) {
                    MultipartFile image = postImages.get(i);
                    String description = postRequest.getDescriptions().get(i); // 매칭되는 설명 가져오기

                    // 이미지 업로드
                    String imageUrl = s3ImageService.uploadFile(image, "post/" + post.getId() + "/");
                    uploadedImageUrls.add(imageUrl);

                    // PostImage 객체 생성
                    PostImage postImage = PostImage.builder()
                            .imageUrl(imageUrl)
                            .description(description) // 매칭된 설명 추가
                            .post(post)
                            .build();
                    postImageList.add(postImage);
                }

                // Post에 PostImage 리스트 추가
                post.getPostImages().addAll(postImageList);

            } catch (Exception e) {
                // 실패한 경우 업로드된 이미지 삭제
                if (!uploadedImageUrls.isEmpty()) {
                    s3ImageService.cleanupUploadedFiles(uploadedImageUrls);
                }
                throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }

        return post.getId();
    }

    @Transactional
    @Override
    public PostResponse updatePost(Long postId, PostRequest postRequest, MultipartFile thumbnail, List<MultipartFile> postImages, MemberDetails memberDetails) {
        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 게시물 기본 정보 수정
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setCookingTime(postRequest.getCookingTime());
        post.setCalories(postRequest.getCalories());
        post.setServings(postRequest.getServings());
        post.setYoutubeUrl(postRequest.getYoutubeUrl());

        // 썸네일 업데이트
        if (thumbnail != null) {
            try {
                s3ImageService.deleteFile(post.getThumbnailUrl());
                post.setThumbnailUrl(s3ImageService.uploadFile(thumbnail, "post/" + post.getId() + "/"));
            } catch (Exception e) {
                throw new ApiException(ErrorCode.THUMBNAIL_UPLOAD_FAIL);
            }
        }

        // 재료 업데이트
        post.getIngredients().clear();
        ingredientService.registerIngredient(postRequest.getIngredients(), post);

        // 영양 정보 업데이트
        post.getNutrients().clear();
        nutrientService.registerNutrient(postRequest.getNutrients(), post);

        // 태그 업데이트
        post.getPostTags().clear();
        tagService.registerTags(postRequest.getTags(), post);

        // 이미지 업데이트
        if (postImages != null && !postImages.isEmpty()) {
            List<String> uploadedImageUrls = new ArrayList<>();
            try {
                post.getPostImages().forEach(postImage -> s3ImageService.deleteFile(postImage.getImageUrl()));
                post.getPostImages().clear();

                List<PostImage> postImageEntities = new ArrayList<>();

                for (int i = 0; i < postImages.size(); i++) {
                    MultipartFile image = postImages.get(i);
                    String description = postRequest.getDescriptions().get(i); // 매칭되는 설명 가져오기

                    // 이미지 업로드
                    String imageUrl = s3ImageService.uploadFile(image, "post/" + post.getId() + "/");
                    uploadedImageUrls.add(imageUrl);

                    // PostImage 객체 생성
                    PostImage postImage = PostImage.builder()
                            .imageUrl(imageUrl)
                            .description(description) // 매칭된 설명 추가
                            .post(post)
                            .build();
                    postImageEntities.add(postImage);
                }

                // Post에 PostImage 리스트 추가
                post.getPostImages().addAll(postImageEntities);

            } catch (Exception e) {
                // 실패한 경우 업로드된 이미지 삭제
                if (!uploadedImageUrls.isEmpty()) {
                    s3ImageService.cleanupUploadedFiles(uploadedImageUrls);
                }
                throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }
        else {
            post.getPostImages().forEach(postImage -> s3ImageService.deleteFile(postImage.getImageUrl()));
            post.getPostImages().clear();
        }

        Boolean isScrapped = member == null ? false : scrapRepository.existsByMemberIdAndPostId(member.getId(), postId);

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
        post.getPostImages().forEach(postImage -> s3ImageService.deleteFile(postImage.getImageUrl()));

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

    // 관련 추천 게시물 리스트
    @Override
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
    public List<PostResponse> getRecommendPosts() {
        List<Post> posts = postRepository.findRecommendPosts();
        List<PostResponse> postResponseList = new ArrayList<>();
//        for (Post post : posts) {
//            postSearchResponseList.add(new PostSearchResponse(
//                    post.getId(),
//                    post.getTitle(),
//                    post.getThumbnailUrl(),
//                    post.getCookingTime(),
//                    post.getCalories(),
//                    post.getServings()
//            ));
//        }
        return null;
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
