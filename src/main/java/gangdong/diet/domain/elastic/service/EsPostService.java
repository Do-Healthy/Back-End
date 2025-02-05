package gangdong.diet.domain.elastic.service;

import gangdong.diet.domain.elastic.domain.EsPostDocument;
import gangdong.diet.domain.elastic.repository.EsPostRepository;
import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class EsPostService {

    private final EsPostRepository esPostRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<EsPostDocument> searchPosts(String keyword) {
        return esPostRepository.searchByTitleOrIngredient(keyword);
    }

    @Transactional
    public void saveToEs(Post post) {

        List<String> ingredientNames = post.getIngredients().stream()
                .map(pi -> pi.getIngredient().getName()).toList(); // toList는 자바 16이상, Immutable

        EsPostDocument esPostDocument = EsPostDocument.builder()
                .esPostId(post.getId().toString())
                .title(post.getTitle())
                .thumbnailUrl(post.getThumbnailUrl())
                .cookingTime(post.getCookingTime())
                .calories(post.getCalories())
                .servings(post.getServings())
                .ingredients(ingredientNames)
                .build();

        esPostRepository.save(esPostDocument);
    }

    @Transactional
    public void updatePostInEs(Post post) {
        // 기존 ES 문서 조회
        Optional<EsPostDocument> existingDocument = esPostRepository.findById(post.getId().toString());

        if (existingDocument.isPresent()) {
            EsPostDocument esPostDocument = existingDocument.get();

            // 필요한 필드만 업데이트
            esPostDocument.setTitle(post.getTitle());
            esPostDocument.setThumbnailUrl(post.getThumbnailUrl());
            esPostDocument.setCookingTime(post.getCookingTime());
            esPostDocument.setCalories(post.getCalories());
            esPostDocument.setServings(post.getServings());

            List<String> ingredientNames = post.getIngredients().stream()
                    .map(pi -> pi.getIngredient().getName()).toList();
            esPostDocument.setIngredients(ingredientNames);

            esPostRepository.save(esPostDocument);
        } else {
            // ES 문서가 없으면 새로 저장
            saveToEs(post);
        }
    }


}
