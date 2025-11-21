package com.cho.board.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ========== 400 Bad Request ==========
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "4000", "잘못된 입력값입니다"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "4000", "잘못된 타입입니다"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "4000", "비밀번호 형식이 올바르지 않습니다"),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "4000", "새 비밀번호는 기존과 달라야 합니다"),
    DELETED_COMMENT(HttpStatus.BAD_REQUEST, "4000", "삭제된 댓글은 수정할 수 없습니다"),

    // ========== 403 Forbidden ==========
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "4030", "접근 권한이 없습니다"),
    USER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "4030", "사용자에 대한 권한이 없습니다"),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "4030", "게시글에 대한 권한이 없습니다"),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "4030", "댓글에 대한 권한이 없습니다"),

    // ========== 404 Not Found ==========
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "4040", "리소스를 찾을 수 없습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "4040", "사용자를 찾을 수 없습니다"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "4040", "게시글을 찾을 수 없습니다"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "4040", "댓글을 찾을 수 없습니다"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "4040", "카테고리를 찾을 수 없습니다"),

    // ========== 405 Method Not Allowed ==========
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "4050", "허용되지 않은 메서드입니다"),

    // ========== 409 Conflict ==========
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "4090", "이미 존재하는 리소스입니다"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "4090", "이미 존재하는 이메일입니다"),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "4090", "이미 존재하는 닉네임입니다"),

    // ========== 500 Internal Server Error ==========
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "5000", "서버 내부 오류가 발생했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
