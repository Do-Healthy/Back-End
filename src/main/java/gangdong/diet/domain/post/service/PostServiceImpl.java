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
import gangdong.diet.domain.post.dto.PostRequest;
import gangdong.diet.domain.post.dto.PostResponse;
import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.post.entity.PostImage;
import gangdong.diet.domain.post.entity.PostIngredient;
import gangdong.diet.domain.post.entity.PostNutrient;
import gangdong.diet.domain.post.repository.PostImageRepository;
import gangdong.diet.domain.post.repository.PostIngredientRepository;
import gangdong.diet.domain.post.repository.PostNutrientRepository;
import gangdong.diet.domain.post.repository.PostRepository;
import gangdong.diet.domain.scrap.repository.ScrapRepository;
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

    @Transactional(readOnly = true)
    @Override
    public Slice<PostResponse> findByKeywords(Long cursorId, String keywords, int size) {

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
        List<Post> recipeResults = postRepository.findByRecipeName(cursorId, finalKeywords, size);
        List<Post> ingredientResults = postRepository.findByIngredient(cursorId, finalKeywords, size);

        // 합친 후 중복 제거
        Set<Post> mergedResultsSet = new LinkedHashSet<>(recipeResults);
        mergedResultsSet.addAll(ingredientResults);

        Member member = isLoggedIn();

        // 스크랩 여부 확인
        List<Long> scrappedPostIds;
        if (member != null) {
            scrappedPostIds = isScrapped(mergedResultsSet, member.getId());
        } else {
            scrappedPostIds = List.of();
        }


        List<PostResponse> finalResults = mergedResultsSet.stream().map(p -> PostResponse.builder().post(p)
                        .isScrapped(scrappedPostIds.contains(p.getId()))
                        .build())
                .toList();

        // 페이징 처리 및 Slice 반환
        boolean hasNext = finalResults.size() > size;
        if (hasNext) {
            finalResults = finalResults.subList(0, size); // 초과 데이터 제거
        }

        return new SliceImpl<>(finalResults, Pageable.ofSize(size), hasNext);
    }

    @Transactional(readOnly = true)
    @Override
    public Slice<PostResponse> findAllPosts(Long cursorId, int size) {
        List<Post> posts = postRepository.findByRecipeName(cursorId, null, size);

        Member member = isLoggedIn();

        // 스크랩 여부 확인
        List<Long> scrappedPostIds;
        if (member != null) {
            scrappedPostIds = isScrapped(posts, member.getId());
        } else {
            scrappedPostIds = List.of();
        }


        List<PostResponse> finalResults = posts.stream()
                .map(post -> PostResponse.builder()
                        .post(post)
                        .isScrapped(scrappedPostIds.contains(post.getId()))
                        .build())
                .toList();

        boolean hasNext = finalResults.size() > size;
        if (hasNext) {
            finalResults = finalResults.subList(0, size); // 초과 데이터 제거
        }

        return new SliceImpl<>(finalResults, Pageable.ofSize(size), hasNext);
    }

    @Transactional(readOnly = true)
    @Override
    public PostResponse getOnePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        Member member = isLoggedIn();
        Boolean isScrapped = member == null ? false : scrapRepository.existsByMemberIdAndPostId(member.getId(), postId);

        return PostResponse.builder()
                .post(post)
                .isScrapped(isScrapped)
                .build();
    }

    @Transactional
    @Override
    public Long savePost(PostRequest postRequest, MultipartFile thumbnail, List<MultipartFile> postImagesRequest, MemberDetails memberDetails) {

        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
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

        try {
            List<String> imageUrls = s3ImageService.uploadFile(postImagesRequest, "post/" + post.getId() + "/");
            List<PostImage> postImages = imageUrls.stream()
                    .map(url -> PostImage.builder().imageUrl(url).post(post).build())
                    .collect(Collectors.toList());
            post.getPostImages().addAll(postImages);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        return post.getId();
    }

    @Transactional
    @Override
    public PostResponse updatePost(Long postId, PostRequest postRequest, MultipartFile thumbnail, List<MultipartFile> postImagesRequest, MemberDetails memberDetails) {
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
        post.setYoutubeUrl(postRequest.getYoutubeUrl());

        // 썸네일 업데이트
        if (thumbnail != null) {
            try {
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

        post.getPostTags().clear();
        tagService.registerTags(postRequest.getTags(), post);

        // 이미지 업데이트
        try {
            post.getPostImages().forEach(postImage -> s3ImageService.deleteFile(postImage.getImageUrl()));
            post.getPostImages().clear();
            List<String> imageUrls = s3ImageService.uploadFile(postImagesRequest, "post/" + post.getId() + "/");
            List<PostImage> postImages = imageUrls.stream()
                    .map(url -> PostImage.builder().imageUrl(url).post(post).build())
                    .collect(Collectors.toList());
            post.getPostImages().addAll(postImages);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        Boolean isScrapped = member == null ? false : scrapRepository.existsByMemberIdAndPostId(member.getId(), postId);

        return PostResponse.builder()
                .post(post)
                .isScrapped(isScrapped)
                .build();
    }

    @Transactional
    @Override
    public void deletePost(Long id) { // TODO 사용자 검증 추가
        postRepository.deleteById(id);
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

    private List<Long> isScrapped(Collection<Post> posts, Long memberId) {

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> scrappedPostIds = scrapRepository.findScrappedPostIds(memberId, postIds);

        return scrappedPostIds;
    }


}
