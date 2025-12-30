package com.cho.board.global.constants;

import java.util.List;

public final class FileConstants {

    private FileConstants() {
        throw new AssertionError("Constants class cannot be instantiated");
    }

    // 파일 크기 제한
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    public static final long MAX_REQUEST_SIZE = 50 * 1024 * 1024;

    // 이미지 개수 제한
    public static final int MAX_POST_IMAGES = 10;

    // 허용되는 이미지 확장자
    public static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of(
        "jpg", "jpeg", "png", "gif", "webp", "heic"
    );
}
