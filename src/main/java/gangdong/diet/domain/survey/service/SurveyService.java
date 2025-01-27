package gangdong.diet.domain.survey.service;

import gangdong.diet.domain.survey.dto.SurveyDTO;
import gangdong.diet.domain.survey.entity.Survey;
import gangdong.diet.global.auth.MemberDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

public interface SurveyService {
    public void createSurvey(SurveyDTO surveyDTO, MemberDetails memberDetails);
}
