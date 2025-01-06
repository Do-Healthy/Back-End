package gangdong.diet.domain.survey.service;

import gangdong.diet.domain.survey.dto.SurveyDTO;
import gangdong.diet.domain.survey.entity.Survey;

public interface SurveyService {
    public void createSurvey(SurveyDTO surveyDTO);
}
