package com.fitpulse.model.enums;

public enum SupportStatus {
    NEW("Ново"), IN_PROGRESS("Прочетено"), RESOLVED("Приключено");
    private final String displayName;
    SupportStatus(String label) { displayName = label; }
    public String getDisplayName() { return displayName; }
}
