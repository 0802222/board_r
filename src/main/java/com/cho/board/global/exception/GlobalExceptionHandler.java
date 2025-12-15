package com.cho.board.global.exception;

import com.cho.board.global.response.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========== 비즈니스 예외 ==========

    // 리소스를 찾을 수 없을 때
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
        ResourceNotFoundException ex) {
        logError(ex);

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 중복된 리소스
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(
        DuplicateResourceException ex) {
        logError(ex);

        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 비즈니스 로직에서 발생하는 커스텀 예외
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(
        BusinessException e) {
        logError(e);

        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = buildErrorResponse(errorCode, e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
            .body(ApiResponse.error(response, e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
        UnauthorizedException e) {
        logError(e);
        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    // ========== Validation 예외 ==========

    // @Valid 검증 실패 시 발생 (Request Body 검증)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e) {
        logError(e);

        Map<String, String> errors = extractFieldErrors(e.getBindingResult());

        return buildValidationErrorResponse(errors);
    }

    // @ModelAttribute 검증 실패 시 발생 (Query Parameter 검증)
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(
        BindException e) {
        logError(e);

        Map<String, String> errors = extractFieldErrors(e.getBindingResult());

        return buildValidationErrorResponse(errors);
    }

    // ========== HTTP 관련 예외 ==========

    // 지원하지 않는 HTTP Method 호출 시 발생
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException e) {
        logError(e);

        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드 입니다.");
    }

    // 타입이 맞지않는 경우 발생 (PathVariable, RequestParam)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e) {
        logError(e);

        String message = String.format("%s 의 타입이 올바르지 않습니다.", e.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    // JSON 파싱 에러 (타입 불일치 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e) {
        logError(e);

        String message = extractReadableErrorMessage(e);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    // ========== 파일 관련 예외 ==========

    // 파일 저장 예외
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileStorageException(
        FileStorageException ex) {
        logError(ex);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ========== Auth 관련 예외 ==========
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
        BadCredentialsException e) {
        logError(e);

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(
        UsernameNotFoundException e) {
        logError(e);

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다.");
    }

    @ExceptionHandler(CustomJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomJwtException(CustomJwtException e) {
        log.warn("JWT 예외 발생 : {}", e.getMessage());

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(e.getMessage()));
    }
    // ========== 기타 예외 ==========

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);

        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다."
        );
    }

    // ========== 헬퍼 메서드 ==========

    /**
     * 에러 로깅
     */
    private void logError(Exception e) {
        log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
    }

    /**
     * 단순 에러 응답 생성
     */
    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
        HttpStatus status,
        String message) {
        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(message));
    }

    /**
     * ErrorCode 기반 ErrorResponse 생성
     */
    private ErrorResponse buildErrorResponse(ErrorCode errorCode, String message) {
        return ErrorResponse.of(
            errorCode.getStatus().value(),
            errorCode.getStatus().getReasonPhrase(),
            errorCode.getCode(),
            message
        );
    }

    /**
     * BindingResult 에서 필드 에러 추출
     */
    private Map<String, String> extractFieldErrors(
        org.springframework.validation.BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return errors;
    }

    /**
     * Validation 에러 응답 생성
     */
    private ResponseEntity<ApiResponse<Map<String, String>>> buildValidationErrorResponse(
        Map<String, String> errors) {
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(errors, "입력값 검증에 실패했습니다."));
    }

    /**
     * HttpMessageNotReadableException 에서 읽기 쉬운 메시지 추출
     */
    private String extractReadableErrorMessage(HttpMessageNotReadableException e) {
        String defaultMessage = "요청 본문을 읽을 수 없습니다.";

        if (e.getCause() instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath().isEmpty()
                ? "알 수 없음"
                : ife.getPath().get(0).getFieldName();
            return String.format(
                "'%s' 필드의 타입이 올바르지 않습니다. (입력값: %s)",
                fieldName,
                ife.getValue()
            );
        }

        return defaultMessage;
    }
}
