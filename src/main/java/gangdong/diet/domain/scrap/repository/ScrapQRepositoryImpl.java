package gangdong.diet.domain.scrap.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static gangdong.diet.domain.scrap.entity.QScrap.scrap;

@RequiredArgsConstructor
public class ScrapQRepositoryImpl implements ScrapQRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findScrappedPostIds(Long memberId, List<Long> postIds) {
        return queryFactory.select(scrap.post.id) // 스크랩된 게시물 ID 선택
                .from(scrap)
                .where(
                        scrap.member.id.eq(memberId), // 멤버 ID 조건
                        scrap.post.id.in(postIds)     // 게시물 ID 목록 조건
                )
                .fetch();
    }
}
