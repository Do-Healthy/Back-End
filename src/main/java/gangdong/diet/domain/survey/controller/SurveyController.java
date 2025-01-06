package gangdong.diet.domain.survey.controller;

import gangdong.diet.domain.post.dto.PostSearchResponse;
import gangdong.diet.domain.survey.dto.SurveyDTO;
import gangdong.diet.domain.survey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @Operation(summary = "설문지 저장")
    @PostMapping
    public ResponseEntity<String> getHealthPostsBySurvey(@RequestBody SurveyDTO surveyDTO) {
//        System.out.println(surveyDTO);
        surveyService.createSurvey(surveyDTO);
        return ResponseEntity.ok().body("설문지 작성이 완료 되었습니다.");
    }

}
