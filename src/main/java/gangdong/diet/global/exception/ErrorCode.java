package gangdong.diet.global.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "레시피 게시물을 찾을 수 없습니다"),
    SEARCHRESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "검색 결과를 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "이미지 업로드를 실패했습니다."),
    FILE_VALID_ERROR(HttpStatus.BAD_GATEWAY, "파일이 존재하지 않습니다."), // 에러명 바꿔야 할 수도
    FILE_FORMAT_ERROR(HttpStatus.BAD_GATEWAY, "파일 형시기이 올바르지 않습니다."),
    THUMBNAIL_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "썸네일 업로드를 실패했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "올바른 요청이 아닙니다."),
    UNAUTHORIZED_ACTION(HttpStatus.BAD_REQUEST, "권한이 없습니다."),

    REDIS_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 서버와의 연결에 실패했습니다."),
    REDIS_COMMAND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 명령어 처리 중 오류가 발생했습니다."),
    REDIS_DATA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis에서 처리된 데이터에 문제가 있습니다.");

    private final HttpStatus httpStatus;
    private final String msg;

}
