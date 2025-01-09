package gangdong.diet.domain.scrap.service;

import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.member.repository.MemberRepository;
import gangdong.diet.domain.post.entity.Post;
import gangdong.diet.domain.post.repository.PostRepository;
import gangdong.diet.domain.scrap.entity.Scrap;
import gangdong.diet.domain.scrap.repository.ScrapRepository;
import gangdong.diet.global.exception.ApiException;
import gangdong.diet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@RequiredArgsConstructor
@Service
public class ScrapServiceImpl implements ScrapService {

    private final ScrapRepository scrapRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public void saveScrap(Long postId, String memberEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Scrap scrap = Scrap.builder()
                .post(post)
                .member(member)
                .build();

        post.getScraps().add(scrap);
        member.getScraps().add(scrap);

        //유저쪽도 add
    }

    @Transactional
    @Override
    public void deleteScrap(Long postId, String memberEmail) {
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        Scrap scrap = scrapRepository.findByPostIdAndMemberId(postId, member.getId()).orElseThrow(); // 에러 만들어야하나..?

        if (scrap.getMember().getId().equals(member.getId())) {
            scrapRepository.delete(scrap); // deletebyid와의 차이.
        }
    }

    @Transactional
    @Override
    public void editScrap(Long postId, String memberEmail) {
        Member member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<Scrap> scrap = scrapRepository.findByPostIdAndMemberId(postId, member.getId());

        if (scrap.isPresent()) {
            scrapRepository.delete(scrap.get());
        }
        else {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
            Scrap newScrap = Scrap.builder()
                    .post(post)
                    .member(member)
                    .build();

            post.getScraps().add(newScrap);
            member.getScraps().add(newScrap);
        }
    }

}
