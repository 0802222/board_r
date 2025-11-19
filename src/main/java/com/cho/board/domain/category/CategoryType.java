package com.cho.board.domain.category;

public enum CategoryType {
    NOTICE("공지사항"),
    FREE("자유게시판"),
    QNA("질문"),
    TECH("기술");

    private final String description;

    CategoryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
