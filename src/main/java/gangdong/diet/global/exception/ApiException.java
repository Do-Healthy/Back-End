package gangdong.diet.global.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException{

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}