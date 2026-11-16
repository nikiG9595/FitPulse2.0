package com.fitpulse.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.model.entity.ProgramTemplate;
import com.fitpulse.model.entity.ProgramTemplateItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard content for empty templates; saved personal programs never enter this path.
 */
final class StandardTrainingPlans {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Item(String exerciseName, int setsCount, String repetitions, int restSeconds, String instructions) {
    }

    record Day(String title, List<Item> items) {
    }

    record Plan(String name, String goal, String experience, int days, String location, List<Day> sessions) {
    }

    private static final List<Plan> PLANS = load();

    private static List<Plan> load() {
        try (var stream = StandardTrainingPlans.class.getResourceAsStream("/content/training-plans.json")) {
            if (stream == null) throw new IllegalStateException("Липсват стандартните тренировъчни програми.");
            return List.of(new ObjectMapper().readValue(stream, Plan[].class));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Тренировъчните програми не могат да се прочетат.", e);
        }
    }

    static List<ProgramTemplateItem> outline(ProgramTemplate template) {
        var plan = PLANS.stream().filter(p -> p.name().equals(template.getName())
                        && p.goal().equals(template.getGoal().name()) && p.experience().equals(template.getExperience().name())
                        && p.location().equals(template.getLocation().name()) && p.days() == template.getTrainingDays())
                .findFirst().orElse(null);
        if (plan == null) return null;
        List<ProgramTemplateItem> result = new ArrayList<>();
        for (int d = 0; d < plan.sessions().size(); d++) {
            var day = plan.sessions().get(d);
            for (int n = 0; n < day.items().size(); n++) {
                var source = day.items().get(n);
                var item = new ProgramTemplateItem();
                item.setDayNumber(d + 1);
                item.setSessionTitle(day.title());
                item.setSortOrder(n + 1);
                item.setExerciseName(source.exerciseName());
                item.setSetsCount(source.setsCount());
                item.setRepetitions(source.repetitions());
                item.setRestSeconds(source.restSeconds());
                item.setInstructions(source.instructions());
                result.add(item);
            }
        }
        return result;
    }
}
