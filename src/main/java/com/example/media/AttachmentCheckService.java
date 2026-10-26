package com.example.media;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.model.ReportImage;

/**
 * Performs bounded, advisory checks on stored report attachments. The current
 * upload UI permits five files of up to 20 MiB each, with PNG, JPEG, and MP4
 * extensions.
 */
@Service
public class AttachmentCheckService {

    static final int MAX_FILES = 5;
    static final long MAX_FILE_BYTES = 20L * 1024 * 1024;
    static final long MAX_IMAGE_PIXELS = 12_000_000;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg");

    private final List<Path> allowedRoots;

    @Autowired
    public AttachmentCheckService(@Value("${ayos.upload-dir:./uploads}") String uploadDir) {
        this(Paths.get(uploadDir),
                Paths.get(System.getProperty("java.io.tmpdir"), "ayosbarangay-uploads"));
    }

    AttachmentCheckService(Path uploadRoot, Path legacyRoot) {
        this.allowedRoots = List.of(uploadRoot, legacyRoot).stream()
                .map(path -> path.toAbsolutePath().normalize())
                .toList();
    }

    public AttachmentAssessment inspect(List<ReportImage> attachments) {
        if (attachments.isEmpty()) {
            return new AttachmentAssessment(false, List.of(), Set.of());
        }

        List<AttachmentFinding> findings = new ArrayList<>();
        Set<String> validPaths = new HashSet<>();
        if (attachments.size() > MAX_FILES) {
            findings.add(new AttachmentFinding("Attachments",
                    "This report has " + attachments.size() + " files; the upload limit is " + MAX_FILES + "."));
        }
        for (ReportImage attachment : attachments) {
            if (inspectOne(attachment, findings)) {
                validPaths.add(attachment.imagePath());
            }
        }
        return new AttachmentAssessment(true, findings, validPaths);
    }

    private boolean inspectOne(ReportImage attachment, List<AttachmentFinding> findings) {
        String fileName;
        Path path;
        try {
            path = Paths.get(attachment.imagePath()).toAbsolutePath().normalize();
            fileName = path.getFileName().toString();
        } catch (InvalidPathException | NullPointerException exception) {
            findings.add(new AttachmentFinding("Attachment", "Stored file path is invalid."));
            return false;
        }

        String extension = extension(fileName);
        if (!IMAGE_EXTENSIONS.contains(extension) && !extension.equals(".mp4")) {
            findings.add(new AttachmentFinding(fileName, "Unsupported file type; expected PNG, JPEG, or MP4."));
            return false;
        }

        try {
            if (!Files.isRegularFile(path)) {
                findings.add(new AttachmentFinding(fileName, "File is missing or is not a regular file."));
                return false;
            }
            if (!isInsideAllowedRoot(path)) {
                findings.add(new AttachmentFinding(fileName, "File is outside the configured upload folders."));
                return false;
            }
            long size = Files.size(path);
            if (size == 0) {
                findings.add(new AttachmentFinding(fileName, "File is empty."));
                return false;
            }
            if (size > MAX_FILE_BYTES) {
                findings.add(new AttachmentFinding(fileName, "File exceeds the 20 MiB upload limit."));
                return false;
            }
            if (extension.equals(".mp4")) {
                if (!hasMp4Signature(path, size)) {
                    findings.add(new AttachmentFinding(fileName,
                            "File does not have a recognizable MP4 container signature."));
                    return false;
                }
                return true;
            } else {
                return inspectImage(path, fileName, extension, findings);
            }
        } catch (IOException | SecurityException exception) {
            findings.add(new AttachmentFinding(fileName, "File could not be read."));
            return false;
        }
    }

    private boolean inspectImage(Path path, String fileName, String extension,
                                 List<AttachmentFinding> findings) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(path.toFile())) {
            if (input == null) {
                findings.add(new AttachmentFinding(fileName, "File is not a readable PNG or JPEG image."));
                return false;
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                findings.add(new AttachmentFinding(fileName, "File is not a readable PNG or JPEG image."));
                return false;
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                boolean matchingFormat = extension.equals(".png")
                        ? format.equals("png")
                        : format.equals("jpeg") || format.equals("jpg");
                if (!matchingFormat) {
                    findings.add(new AttachmentFinding(fileName,
                            "File contents do not match the image filename extension."));
                    return false;
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_IMAGE_PIXELS) {
                    findings.add(new AttachmentFinding(fileName,
                            "Image dimensions are invalid or exceed the 12-megapixel inspection limit."));
                    return false;
                }

                BufferedImage image = reader.read(0);
                if (image == null) {
                    findings.add(new AttachmentFinding(fileName, "Image data could not be decoded."));
                    return false;
                }
                return true;
            } finally {
                reader.dispose();
            }
        }
    }

    private boolean hasMp4Signature(Path path, long size) throws IOException {
        if (size < 12) {
            return false;
        }
        ByteBuffer header = ByteBuffer.allocate(8);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            while (header.hasRemaining()) {
                if (channel.read(header) < 0) {
                    return false;
                }
            }
        }
        byte[] bytes = header.array();
        return new String(bytes, 4, 4, StandardCharsets.US_ASCII).equals("ftyp");
    }

    private boolean isInsideAllowedRoot(Path file) throws IOException {
        Path realFile = file.toRealPath();
        for (Path root : allowedRoots) {
            if (Files.exists(root) && realFile.startsWith(root.toRealPath())) {
                return true;
            }
        }
        return false;
    }

    private String extension(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        return dot < 0 ? "" : lower.substring(dot);
    }
}
