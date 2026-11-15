package com.fitpulse.model.enums;

public enum SupportStatus {
    NEW("Ново"), IN_PROGRESS("В процес"), RESOLVED("Решено");
    private final String displayName;
    SupportStatus(String label) { displayName = label; }
    public String getDisplayName() { return displayName; }
}
