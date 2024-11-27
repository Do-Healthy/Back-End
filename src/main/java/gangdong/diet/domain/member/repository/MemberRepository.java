package gangdong.diet.domain.member.repository;

import gangdong.diet.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    public Optional<Member> findByUsername(String username);
}
