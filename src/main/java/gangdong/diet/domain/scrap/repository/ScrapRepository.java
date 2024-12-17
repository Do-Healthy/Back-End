package gangdong.diet.domain.scrap.repository;

import gangdong.diet.domain.scrap.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapQRepository {
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);
    Optional<Scrap> findByPostIdAndMemberId(Long postId, Long memberId);
}
