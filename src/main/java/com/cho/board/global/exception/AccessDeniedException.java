package com.cho.board.global.exception;

public class AccessDeniedException extends BusinessException {

    public AccessDeniedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AccessDeniedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
