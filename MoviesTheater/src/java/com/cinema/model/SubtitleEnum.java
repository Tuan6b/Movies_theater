package com.cinema.model;

public enum SubtitleEnum {
    PHU_DE_TIENG_VIET("Phụ đề Tiếng Việt"),
    LONG_TIENG_VIET("Lồng Tiếng Việt"),
    PHU_DE_TIENG_ANH("Phụ đề Tiếng Anh"),
    KHONG_PHU_DE("Không phụ đề");

    private final String displayName;

    SubtitleEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
