package com.fitpulse.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component("exerciseImages")
public class ExerciseImages {
    public record Frame(String path, String labelKey) {}

    private final ConcurrentHashMap<String, List<Frame>> frames = new ConcurrentHashMap<>();

    public List<Frame> forCode(String code) {
        if (code == null || !code.matches("[a-z0-9-]+")) return List.of();
        return frames.computeIfAbsent(code, ExerciseImages::findFrames);
    }

    private static List<Frame> findFrames(String code) {
        return List.of(
                new Frame("/images/exercises/" + code + "-start.webp", "exercise.image.start"),
                new Frame("/images/exercises/" + code + "-finish.webp", "exercise.image.finish"),
                new Frame("/images/exercises/" + code + "-hold.webp", "exercise.image.hold"))
                .stream()
                .filter(frame -> new ClassPathResource("static" + frame.path()).exists())
                .toList();
    }
}
