package com.cho.board.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(Include.NON_NULL)
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String code;
    private final String message;
    private final List<FieldError> fieldErrors;

    @Getter
    @Builder
    public static class FieldError {

        private final String field;
        private final String value;
        private final String reason;
    }

    // 기본 에러 응답
    public static ErrorResponse of(int status, String error, String code, String message) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .error(error)
            .code(code)
            .message(message)
            .build();
    }

    // Validation 에러 응답
    public static ErrorResponse of(int status, String error, String code, String message,
        List<FieldError> fieldErrors) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .error(error)
            .code(code)
            .message(message)
            .fieldErrors(fieldErrors)
            .build();
    }
}
