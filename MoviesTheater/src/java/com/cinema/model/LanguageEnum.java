package com.cinema.model;

public enum LanguageEnum {
    TIENG_ANH("Tiếng Anh"),
    TIENG_VIET("Tiếng Việt"),
    TIENG_HAN("Tiếng Hàn"),
    TIENG_NHAT("Tiếng Nhật"),
    TIENG_TRUNG("Tiếng Trung"),
    KHAC("Khác");

    private final String displayName;

    LanguageEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
