package gangdong.diet.domain.elastic.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@NoArgsConstructor
@Document(indexName = "es_post_documents")
public class EsPostDocument {

    @Id
    private String esPostId;

    @Field(type = FieldType.Text, analyzer = "korean_nori_analyzer", searchAnalyzer = "korean_nori_analyzer")
    private String title;

    @Field(type = FieldType.Text, index = false)
    private String thumbnailUrl;

    @Field(type = FieldType.Keyword)
    private String cookingTime;

    @Field(type = FieldType.Keyword)
    private String calories;

    @Field(type = FieldType.Keyword)
    private String servings;

    @Field(type = FieldType.Text, analyzer = "korean_nori_analyzer", searchAnalyzer = "korean_nori_analyzer")
    private List<String> ingredients;

    @Builder
    private EsPostDocument(String esPostId, String title, String thumbnailUrl, String cookingTime, String calories, String servings, List<String> ingredients) {
        this.esPostId = esPostId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.cookingTime = cookingTime;
        this.calories = calories;
        this.servings = servings;
        this.ingredients = ingredients;
    }
}
