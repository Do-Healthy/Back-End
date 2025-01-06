package gangdong.diet.domain.survey.entity;

import gangdong.diet.domain.BaseEntity;
import gangdong.diet.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey")
public class Survey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // 어떤 질환이 있는지에 대한 리스트
    @Getter
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "survey_total_list", joinColumns = @JoinColumn(name = "survey_id"))
    private List<String> totalList = new ArrayList<>();

    @OneToOne
    private Member member;


    @Builder
    public Survey(Integer digestive, Long id, Integer weightEnergy, Integer skinHair, Integer cardioBp, Integer immunity, List<String> totalList, Member member) {
        this.digestive = digestive;
        this.id = id;
        this.weightEnergy = weightEnergy;
        this.skinHair = skinHair;
        this.cardioBp = cardioBp;
        this.immunity = immunity;
        this.totalList = totalList;
        this.member = member;
    }
}
