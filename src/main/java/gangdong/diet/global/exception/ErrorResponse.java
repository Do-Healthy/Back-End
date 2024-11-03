package gangdong.diet.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {
    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
    private final Map<String, String> validation;

    @Builder
    public ErrorResponse(int code, HttpStatus httpStatus, String message, Map<String, String> validation) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message == null ? "" : message;
        this.validation = validation == null ? new HashMap<>() : validation;
    }
}
