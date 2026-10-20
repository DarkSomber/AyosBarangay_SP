package com.example.view;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadDir {

    private static volatile Path dir =
            Paths.get(System.getProperty("java.io.tmpdir"), "ayosbarangay-uploads");

    private UploadDir() {
    }

    public static Path get() {
        return dir;
    }

    public static void set(Path newDir) {
        dir = newDir.toAbsolutePath().normalize();
    }
}