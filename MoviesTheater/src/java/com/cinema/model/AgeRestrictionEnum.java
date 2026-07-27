package com.cinema.model;

public enum AgeRestrictionEnum {
    P(0, "P - Phổ biến"),
    C13(13, "C13 - Khán giả từ 13 tuổi"),
    C16(16, "C16 - Khán giả từ 16 tuổi"),
    C18(18, "C18 - Khán giả từ 18 tuổi");

    private final int value;
    private final String displayName;

    AgeRestrictionEnum(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
