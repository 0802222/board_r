package com.cho.board.global.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.coyote.Response;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // 성공 응답 (데이터 + 메시지)
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    // 성공 응답 (데이터)
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "요청이 성공했습니다.");
    }

    // 성공 응답 (메시지)
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(null)
            .build();
    }

    // 실패 응답
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .data(null)
            .build();
    }

    // 실패 응답 (데이터 포함)
    public static <T> ApiResponse<T> error(T data, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .data(data)
            .build();
    }

}
