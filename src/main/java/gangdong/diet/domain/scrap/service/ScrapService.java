package gangdong.diet.domain.scrap.service;

public interface ScrapService {

    public void saveScrap(Long postId, String memberEmail);

    public void deleteScrap(Long postId, String memberEmail);

}
