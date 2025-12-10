package com.cho.board.global.exception;

public class FileStorageException extends BusinessException {

    public FileStorageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FileStorageException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // 기존 호환성을 위한 생성자 (선택사항)
    public FileStorageException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
        initCause(cause);
    }

}
