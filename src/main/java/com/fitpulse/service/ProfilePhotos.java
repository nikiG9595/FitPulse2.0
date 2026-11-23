package com.fitpulse.service;

import com.fitpulse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfilePhotos {
    private final Path directory;
    private final UserRepository users;

    public ProfilePhotos(@Value("${fitpulse.profile.photo-dir}") String directory, UserRepository users) {
        this.users = users;
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    private Path path(UUID userId) {
        return directory.resolve(userId + ".jpg");
    }

    public boolean existsFor(String username) {
        return users.findByUsername(username).map(user -> exists(user.getId())).orElse(false);
    }

    public boolean exists(UUID userId) {
        return Files.isRegularFile(path(userId), LinkOption.NOFOLLOW_LINKS);
    }

    public Resource read(UUID userId) throws IOException {
        if (!exists(userId)) throw new NoSuchFileException(userId.toString());
        return new ByteArrayResource(Files.readAllBytes(path(userId)));
    }

    public void remove(UUID userId) throws IOException {
        Files.deleteIfExists(path(userId));
    }

    public void save(UUID userId, MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize() > 5L * 1024 * 1024) throw new IllegalArgumentException();
        BufferedImage source;
        try (var stream = file.getInputStream(); var input = new MemoryCacheImageInputStream(stream)) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException();
            var reader = readers.next();
            try {
                if (!Set.of("jpeg", "png", "webp").contains(reader.getFormatName().toLowerCase(Locale.ROOT))) {
                    throw new IllegalArgumentException();
                }
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 8192 || height > 8192 || (long) width * height > 10_000_000) {
                    throw new IllegalArgumentException();
                }
                source = reader.read(0);
                if (source == null) throw new IllegalArgumentException();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            } finally {
                reader.dispose();
            }
        }
        int crop = Math.min(source.getWidth(), source.getHeight());
        int size = Math.min(crop, 384);
        int x = (source.getWidth() - crop) / 2;
        int y = (source.getHeight() - crop) / 2;
        var avatar = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        var graphics = avatar.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(source, 0, 0, size, size, x, y, x + crop, y + crop, Color.WHITE, null);
        } finally {
            graphics.dispose();
        }
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "avatar-", ".tmp");
        try {
            if (!ImageIO.write(avatar, "jpeg", temporary.toFile())) throw new IOException("JPEG encoder unavailable");
            Files.move(temporary, path(userId), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}