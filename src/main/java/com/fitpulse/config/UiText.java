package com.fitpulse.config;

import com.fitpulse.model.dto.WeeklyMenu;
import com.fitpulse.model.entity.UserMealPlan;
import com.fitpulse.model.entity.UserProgram;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component("uiText")
public class UiText {
    private static final String CATALOG_PREFIX = "catalog.";
    private final Map<String, String> interfaceKeys;
    private final Map<String, String> catalogKeys;
    private final MessageSource messages;
    private final List<String> clientKeys;

    public UiText(MessageSource messages) {
        this.messages = messages;
        var source = ResourceBundle.getBundle("messages", Locale.forLanguageTag("bg"));
        clientKeys = List.of(source.getString("client.keys").split(","));
        var interfaceIndex = new HashMap<String, String>();
        var catalogIndex = new HashMap<String, String>();
        for (String key : source.keySet()) {
            if (key.startsWith(CATALOG_PREFIX)) {
                catalogIndex.put(source.getString(key), key);
            } else if (!"client.keys".equals(key)) {
                interfaceIndex.putIfAbsent(source.getString(key), key);
            }
        }
        interfaceKeys = Map.copyOf(interfaceIndex);
        catalogKeys = Map.copyOf(catalogIndex);
    }

    public String label(String source) {
        return translate(source, interfaceKeys);
    }

    public String catalogLabels(String source) {
        if (source == null || !"en".equals(LocaleContextHolder.getLocale().getLanguage())) return source;
        return Arrays.stream(source.split(", ")).map(this::catalog).collect(Collectors.joining(", "));
    }

    public Map<String, String> clientMessages() {
        Locale locale = LocaleContextHolder.getLocale();
        return clientKeys.stream().collect(Collectors.toMap(key -> key,
                key -> messages.getMessage(key, null, locale)));
    }

    public String catalog(String source) {
        return translate(source, catalogKeys);
    }

    public String programCopyName(UserProgram program) {
        var template = program.getSourceTemplate();
        if (template != null && program.getDisplayName().equals(template.getDisplayName())) {
            return catalog(program.getDisplayName());
        }
        return program.getDisplayName();
    }

    public String mealCopyName(UserMealPlan plan) {
        var template = plan.getSourceTemplate();
        if (template != null && plan.getName().equals(template.getName())) {
            return catalog(plan.getName());
        }
        return plan.getName();
    }

    public String programCopyText(UserProgram program, String source) {
        return program != null && program.getSourceTemplate() != null ? catalog(source) : source;
    }

    public String programCopyInstructions(UserProgram program, String source) {
        return program != null && program.getSourceTemplate() != null ? instructions(source) : source;
    }

    public String instructions(String source) {
        if (source == null || !"en".equals(LocaleContextHolder.getLocale().getLanguage())) return source;
        if (catalogKeys.containsKey(source)) return catalog(source);
        String[] steps = source.split("(?<=[.!?])\\s+(?=[А-ЯA-Z])");
        if (!Arrays.stream(steps).allMatch(catalogKeys::containsKey)) return source;
        return Arrays.stream(steps)
                .map(this::catalog)
                .collect(Collectors.joining(" "));
    }

    public String quantity(WeeklyMenu.Ingredient ingredient) {
        if (!"en".equals(LocaleContextHolder.getLocale().getLanguage())) return ingredient.getQuantity();
        String amount = BigDecimal.valueOf(ingredient.grams()).stripTrailingZeros().toPlainString();
        return amount + messages.getMessage("ui.g", null, Locale.ENGLISH);
    }

    public String mealDescription(WeeklyMenu.Meal meal) {
        Locale locale = LocaleContextHolder.getLocale();
        if (!"en".equals(locale.getLanguage())) return meal.getDescription();
        String ingredients = meal.ingredients().stream().map(WeeklyMenu.Ingredient::name)
                .map(this::catalog).limit(3).collect(Collectors.joining(", "));
        return messages.getMessage("catalog.meal.description.example", new Object[]{ingredients}, locale);
    }

    private String translate(String source, Map<String, String> keys) {
        Locale locale = LocaleContextHolder.getLocale();
        if (source == null || !"en".equals(locale.getLanguage())) return source;
        String key = keys.get(source);
        return key == null ? source : messages.getMessage(key, null, source, locale);
    }
}
