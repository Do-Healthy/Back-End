package gangdong.diet.domain.survey.dto;


import lombok.Data;

@Data
public class SurveyDTO {
    // 소화 문제
    private Integer digestive;
    // 체중 변화 및 에너지 문제
    private Integer weightEnergy;
    // 피부 및 모발 상태
    private Integer skinHair;
    // 심혈관 및 혈압 문제
    private Integer cardioBp;
    //면역 및 감염 문제
    private Integer immunity;

    private Long memberId;
}
