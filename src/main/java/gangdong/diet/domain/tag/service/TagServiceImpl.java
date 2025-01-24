package gangdong.diet.domain.tag.service;

import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.post.entity.PostTag;
import gangdong.diet.domain.post.repository.PostTagRepository;
import gangdong.diet.domain.tag.entity.Tag;
import gangdong.diet.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;


    @Override
    public void registerTags(String tags, Post post) {

        String newTags = tags.replaceAll(" ", "");
        Set<String> finalTagNames = Arrays.stream(newTags.split(","))
                .collect(Collectors.toSet()); // 중복 제거

        post.getPostTags().clear();
        finalTagNames.forEach(name -> {
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));

            PostTag postTag = PostTag.builder()
                    .post(post)
                    .tag(tag)
                    .build();

            post.getPostTags().add(postTag);
        });

    }

}
