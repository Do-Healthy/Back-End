package gangdong.diet.domain.tag.entity;

import gangdong.diet.domain.post.entity.PostTag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private Integer searchCnt;

    @Builder
    private Tag(String name, Integer searchCnt) {
        this.name = name;
        this.searchCnt = searchCnt;
    }
}
