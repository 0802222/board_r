package com.cho.board.global.constants;

public final class PageConstants {

    private PageConstants() {
        throw new AssertionError("Constants class cannot be instantiated");
    }

    // 기본 페이지 설정
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    // 정렬 기준
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String SORT_DIRECTION_DESC = "desc";
    public static final String SORT_DIRECTION_ASC = "asc";

    // 최대 페이지 크기
    public static final int MAX_PAGE_SIZE = 100;
}
