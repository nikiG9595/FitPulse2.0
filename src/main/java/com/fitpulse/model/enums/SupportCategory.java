package com.fitpulse.model.enums;

public enum SupportCategory {
    PROBLEM("Докладвай проблем"), QUESTION("Въпрос"), SUGGESTION("Предложение");
    private final String displayName;
    SupportCategory(String label) { displayName = label; }
    public String getDisplayName() { return displayName; }
}
