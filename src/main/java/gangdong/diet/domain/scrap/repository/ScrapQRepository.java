package gangdong.diet.domain.scrap.repository;


import java.util.List;

public interface ScrapQRepository {

    List<Long> findScrappedPostIds(Long memberId, List<Long> postIds);

}
