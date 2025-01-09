package gangdong.diet.domain.scrap.service;

public interface ScrapService {

    void saveScrap(Long postId, String memberEmail);

    void deleteScrap(Long postId, String memberEmail);

    void editScrap(Long postId, String memberEmail);

}
