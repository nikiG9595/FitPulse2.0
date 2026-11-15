package com.fitpulse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.*;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

@Service
public class SupportImageStorage {
    private final Path directory;
    public SupportImageStorage(@Value("${fitpulse.support.storage-dir}") String directory) {
        this.directory=Path.of(directory).toAbsolutePath().normalize();
    }
    public String store(MultipartFile file) throws IOException {
        if(file==null || file.isEmpty()) return null;
        if(file.getSize()>5L*1024*1024) throw new IllegalArgumentException("Снимката може да е до 5 MB.");
        // Never trust the filename, extension or browser-provided Content-Type.
        try(var input=new MemoryCacheImageInputStream(file.getInputStream())) {
            var readers=ImageIO.getImageReaders(input);
            if(!readers.hasNext()) throw invalid();
            var reader=readers.next();
            try {
                if(!Set.of("jpeg","jpg","png","webp").contains(reader.getFormatName().toLowerCase(Locale.ROOT))) throw invalid();
                reader.setInput(input,true,true);
                int width=reader.getWidth(0),height=reader.getHeight(0);
                if(width<1 || height<1 || width>8192 || height>8192 || (long)width*height>10_000_000)
                    throw new IllegalArgumentException("Снимката трябва да е до 10 мегапиксела и до 8192 пиксела на страна.");
                var image=reader.read(0);
                if(image==null) throw invalid();
                // Re-encoding removes metadata, scripts, trailing payloads and original filenames.
                if(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"))
                    Files.createDirectories(directory,PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
                else Files.createDirectories(directory);
                String key=UUID.randomUUID()+".png";
                Path destination=path(key);
                try {
                    if(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"))
                        Files.createFile(destination,PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
                    else Files.createFile(destination);
                    try(var out=Files.newOutputStream(destination)) {
                        if(!ImageIO.write(image,"png",out)) throw new IOException("PNG encoder unavailable");
                    }
                    return key;
                } catch(IOException|RuntimeException e) { Files.deleteIfExists(destination); throw e; }
            } catch(javax.imageio.IIOException e) { throw invalid(); }
            finally { reader.dispose(); }
        }
    }
    private IllegalArgumentException invalid(){return new IllegalArgumentException("Качи валидна снимка JPG, PNG или WebP.");}
    private Path path(String key) {
        if(key==null || !key.matches("[a-f0-9-]{36}\\.png")) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Path result=directory.resolve(key).normalize();
        if(!result.getParent().equals(directory)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return result;
    }
    public Resource read(String key) {
        Path file=path(key);
        if(!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return new FileSystemResource(file);
    }
    public void delete(String key) throws IOException { if(key!=null) Files.deleteIfExists(path(key)); }
}
