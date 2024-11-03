package gangdong.diet.global.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "레시피 게시물을 찾을 수 없습니다"),
    SEARCHRESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "검색 결과를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String msg;

}
