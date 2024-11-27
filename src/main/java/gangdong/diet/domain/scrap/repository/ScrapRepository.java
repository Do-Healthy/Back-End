package gangdong.diet.domain.scrap.repository;

import gangdong.diet.domain.scrap.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByPostIdAndMemberId(Long postId, Long memberId);
}
