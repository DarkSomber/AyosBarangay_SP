package com.example.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.view.UploadDir;

/** Reads ayos.upload-dir once at startup and creates the folder if needed. */
@Component
public class UploadDirConfig {

    private static final Logger log = LoggerFactory.getLogger(UploadDirConfig.class);

    public UploadDirConfig(@Value("${ayos.upload-dir:./uploads}") String configured) {
        Path dir = Paths.get(configured);
        UploadDir.set(dir);
        try {
            Files.createDirectories(UploadDir.get());
        } catch (IOException ex) {
            log.warn("Could not create upload folder {}: {}", UploadDir.get(), ex.getMessage());
        }
        log.info("Report uploads are stored in {}", UploadDir.get());
    }
}