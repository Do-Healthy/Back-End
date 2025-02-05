package gangdong.diet.domain.elastic.repository;

import gangdong.diet.domain.elastic.domain.EsPostDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EsPostRepository extends ElasticsearchRepository<EsPostDocument, String> {

    @Query("""
{
  "bool": {
    "should": [
      { "match": { "title": "?0" } },
      { "match": { "ingredients": "?0" } }
    ]
  }
}
""")
    List<EsPostDocument> searchByTitleOrIngredient(String keyword);


}
