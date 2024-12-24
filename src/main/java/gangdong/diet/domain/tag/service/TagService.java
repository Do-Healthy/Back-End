package gangdong.diet.domain.tag.service;


import gangdong.diet.domain.post.entity.Post;

public interface TagService {

    void registerTags(String tags, Post post);

}
