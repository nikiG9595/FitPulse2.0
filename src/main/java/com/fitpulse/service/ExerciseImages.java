package com.fitpulse.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
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
        return Stream.of("start", "finish", "hold")
                .flatMap(position -> Stream.of("webp", "png")
                        .map(extension -> "/images/exercises/" + code + "-" + position + "." + extension)
                        .filter(path -> new ClassPathResource("static" + path).exists())
                        .limit(1)
                        .map(path -> new Frame(path, "exercise.image." + position)))
                .toList();
    }
}
