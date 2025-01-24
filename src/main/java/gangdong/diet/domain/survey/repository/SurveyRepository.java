package gangdong.diet.domain.survey.repository;

import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    public Optional<Survey> findByMemberId(Long memberId);
    public Optional<Survey> findByMember(Member member);
}
