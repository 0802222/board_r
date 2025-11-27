package com.cho.board.global.exception;

import com.cho.board.global.response.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.HashMap;
import java.util.Map;
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

    // 리소스를 찾을 수 없을 때
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
        ResourceNotFoundException ex) {

        log.error("ResourceNotFoundException: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // 중복된 리소스
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(
        DuplicateResourceException ex) {

        log.error("DuplicateResourceException: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // 비즈니스 로직에서 발생하는 커스텀 예외
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(
        BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(
            errorCode.getStatus().value(),
            errorCode.getStatus().getReasonPhrase(),
            errorCode.getCode(),
            e.getMessage()
        );
        return ResponseEntity.status(errorCode.getStatus())
            .body(ApiResponse.error(response, e.getMessage()));
    }

    // @Valid 검증 실패 시 발생 (Request Body 검증)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(errors, "입력값 검증에 실패했습니다."));
    }

    // @ModelAttribute 검증 실패 시 발생 (Query Parameter 검증)
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(
        BindException e) {
        log.error("BindException: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(errors, "입력값 검증에 실패했습니다."));
    }

    // 지원하지 않는 HTTP Method 호출 시 발생
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ApiResponse.error("지원하지 않는 HTTP 메서드입니다."));
    }

    // 타입이 맞지않는 경우 발생 (PathVariable, RequestParam)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());

        String message = e.getName() + "의 타입이 올바르지 않습니다.";

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(message));
    }

    // JSON 파싱 에러 (타입 불일치 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
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

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(message));
    }

    // 파일 저장 예외
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileStorageException(
        FileStorageException ex) {
        log.error("FileStorageException: {}", ex.getMessage());

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.getMessage()));
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);

        return ResponseEntity.internalServerError()
            .body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}
