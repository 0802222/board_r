package com.cho.board.global.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // BusinessException 처리 (비즈니스 로직에서 발생하는 커스텀 예외)
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(
            errorCode.getStatus().value(),
            errorCode.getStatus().getReasonPhrase(),
            errorCode.getCode(),
            e.getMessage()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // @Valid 검증 실패 시 발생 (Request Body 검증)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.builder()
                .field(error.getField())
                .value(error.getRejectedValue() != null ?
                    error.getRejectedValue().toString() : "")
                .reason(error.getDefaultMessage())
                .build())
            .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ErrorCode.INVALID_TYPE_VALUE.getCode(),
            ErrorCode.INVALID_INPUT_VALUE.getMessage(),
            fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    // @ModelAttribute 검증 실패 시 발생 (Query Parameter 검증)
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.error("BindException: {}", e.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.builder()
                .field(error.getField())
                .value(error.getRejectedValue() != null ?
                    error.getRejectedValue().toString() : "")
                .reason(error.getDefaultMessage())
                .build())
            .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ErrorCode.INVALID_INPUT_VALUE.getCode(),
            ErrorCode.INVALID_INPUT_VALUE.getMessage(),
            fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    // 지원하지 않는 HTTP Method 호출 시 발생
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.of(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
            ErrorCode.METHOD_NOT_ALLOWED.getCode(),
            ErrorCode.METHOD_NOT_ALLOWED.getMessage()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // 타입이 맞지않는 경우 발생 (PathVariable, RequestParam)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ErrorCode.INVALID_TYPE_VALUE.getCode(),
            e.getName() + "의 타입이 올바르지 않습니다."
        );
        return ResponseEntity.badRequest().body(response);
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);

        ErrorResponse response = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            ErrorCode.INVALID_INPUT_VALUE.getMessage()
        );
        return ResponseEntity.internalServerError().body(response);
    }

    // JSON 파싱 에러 (타입 불일치 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: {}", e.getMessage());

        String message = "요청 본문을 읽을 수 없습니다.";

        // 타입 변환 실패인 경우 상세 메시지
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) e.getCause();
            String fieldName = ife.getPath().isEmpty() ? "알 수 없음"
                : ife.getPath().get(0).getFieldName();
            message = String.format("'%s' 필드의 타입이 올바르지 않습니다. (입력값: %s)",
                fieldName, ife.getValue());
        }

        ErrorResponse response = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ErrorCode.INVALID_TYPE_VALUE.getCode(),
            message
        );

        return ResponseEntity.badRequest().body(response);
    }
}
