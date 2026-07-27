package com.cinema.model;

public enum CountryEnum {
    MY("Mỹ"),
    VIET_NAM("Việt Nam"),
    HAN_QUOC("Hàn Quốc"),
    NHAT_BAN("Nhật Bản"),
    TRUNG_QUOC("Trung Quốc"),
    KHAC("Khác");

    private final String displayName;

    CountryEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
