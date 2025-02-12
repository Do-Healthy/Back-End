package gangdong.diet.domain.survey.service;

import gangdong.diet.domain.member.entity.Member;
import gangdong.diet.domain.member.repository.MemberRepository;
import gangdong.diet.domain.survey.dto.SurveyDTO;
import gangdong.diet.domain.survey.entity.Survey;
import gangdong.diet.domain.survey.repository.SurveyRepository;
import gangdong.diet.global.auth.MemberDetails;
import gangdong.diet.global.exception.ApiException;
import gangdong.diet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {
    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;

    @Override
    public void createSurvey(SurveyDTO surveyDTO, MemberDetails memberDetails) {

        Member member = memberRepository.findByMemberEmail(memberDetails.getUsername()).orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Map<String, Integer> surveyScore = new HashMap<>();
        surveyScore.put("소화문제",surveyDTO.getDigestive());
        surveyScore.put("체중및에너지문제",surveyDTO.getWeightEnergy());
        surveyScore.put("피부모발",surveyDTO.getSkinHair());
        surveyScore.put("심혈관혈압",surveyDTO.getCardioBp());
        surveyScore.put("면역감염",surveyDTO.getImmunity());

        List<String> resultTypes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : surveyScore.entrySet()) {
            if (entry.getValue() > 15) { // 15점 초과일 경우
                resultTypes.add(entry.getKey());
            }
        }

        Survey survey = surveyRepository.findByMember(member)
                .orElse(Survey.builder()
                        .member(member)
                        .build());

        survey.setDigestive(surveyDTO.getDigestive());
        survey.setWeightEnergy(surveyDTO.getWeightEnergy());
        survey.setSkinHair(surveyDTO.getSkinHair());
        survey.setCardioBp(surveyDTO.getCardioBp());
        survey.setImmunity(surveyDTO.getImmunity());
        survey.setTotalList(resultTypes);

        surveyRepository.save(survey);
    }

}
