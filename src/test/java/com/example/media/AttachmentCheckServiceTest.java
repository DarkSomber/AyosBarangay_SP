package com.example.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.model.ReportImage;
import com.example.model.ImageType;

class AttachmentCheckServiceTest {

    @TempDir
    Path uploadRoot;

    private AttachmentCheckService service;

    @BeforeEach
    void setUp() {
        service = new AttachmentCheckService(uploadRoot, uploadRoot.resolve("legacy"));
    }

    @Test
    void acceptsReadablePngAndJpegImages() throws IOException {
        Path png = uploadRoot.resolve("valid.png");
        Path jpeg = uploadRoot.resolve("valid.jpg");
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", png.toFile());
        ImageIO.write(image, "jpeg", jpeg.toFile());

        AttachmentAssessment assessment = service.inspect(List.of(
                attachment(png), attachment(jpeg)));

        assertTrue(assessment.attachmentProvided());
        assertFalse(assessment.hasWarnings());
    }

    @Test
    void warnsForUnsupportedFileExtensions() throws IOException {
        Path file = Files.writeString(uploadRoot.resolve("notes.txt"), "report attachment");

        AttachmentAssessment assessment = service.inspect(List.of(attachment(file)));

        assertTrue(assessment.hasWarnings());
        assertTrue(assessment.findings().getFirst().reason().contains("Unsupported"));
    }

    @Test
    void warnsForEmptyFiles() throws IOException {
        Path file = Files.createFile(uploadRoot.resolve("empty.png"));

        AttachmentAssessment assessment = service.inspect(List.of(attachment(file)));

        assertTrue(assessment.hasWarnings());
        assertTrue(assessment.findings().getFirst().reason().contains("empty"));
    }

    @Test
    void warnsForUnreadableImageContentsAndMissingFiles() throws IOException {
        Path corruptImage = Files.writeString(uploadRoot.resolve("corrupt.jpg"), "not an image");
        Path missing = uploadRoot.resolve("missing.jpg");

        AttachmentAssessment assessment = service.inspect(List.of(
                attachment(corruptImage), attachment(missing)));

        assertEquals(2, assessment.findings().size());
        assertTrue(assessment.findings().get(0).reason().contains("not a readable"));
        assertTrue(assessment.findings().get(1).reason().contains("missing"));
    }

    @Test
    void distinguishesNoAttachmentFromInvalidAttachment() {
        AttachmentAssessment assessment = service.inspect(List.of());

        assertFalse(assessment.attachmentProvided());
        assertFalse(assessment.hasWarnings());
    }

    @Test
    void warnsWhenFileIsOutsideConfiguredUploadFolders() throws IOException {
        Path outside = Files.createTempFile("outside-attachment", ".png");
        try {
            ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", outside.toFile());

            AttachmentAssessment assessment = service.inspect(List.of(attachment(outside)));

            assertTrue(assessment.hasWarnings());
            assertTrue(assessment.findings().getFirst().reason().contains("outside"));
            assertTrue(assessment.validPaths().isEmpty());
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    @Test
    void acceptsRecognizableMp4AndWarnsForInvalidMp4() throws IOException {
        Path validMp4 = uploadRoot.resolve("clip.mp4");
        byte[] header = ByteBuffer.allocate(16)
                .putInt(16)
                .put("ftyp".getBytes(StandardCharsets.US_ASCII))
                .put("isom".getBytes(StandardCharsets.US_ASCII))
                .putInt(0)
                .array();
        Files.write(validMp4, header);
        Path invalidMp4 = Files.writeString(uploadRoot.resolve("invalid.mp4"), "not an mp4 file");

        AttachmentAssessment validAssessment = service.inspect(List.of(attachment(validMp4)));
        AttachmentAssessment invalidAssessment = service.inspect(List.of(attachment(invalidMp4)));

        assertFalse(validAssessment.hasWarnings());
        assertTrue(invalidAssessment.hasWarnings());
        assertTrue(invalidAssessment.findings().getFirst().reason().contains("MP4 container signature"));
    }

    @Test
    void warnsWhenAttachmentExceedsConfiguredUploadConstraints() throws IOException {
        Path oversized = uploadRoot.resolve("oversized.png");
        try (var channel = java.nio.channels.FileChannel.open(oversized,
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.WRITE)) {
            channel.position(AttachmentCheckService.MAX_FILE_BYTES);
            channel.write(ByteBuffer.wrap(new byte[] {1}));
        }
        List<ReportImage> tooMany = java.util.stream.IntStream.range(0, 6)
                .mapToObj(index -> ReportImage.of(1, uploadRoot.resolve("part" + index + ".txt").toString(),
                        ImageType.ORIGINAL))
                .toList();

        AttachmentAssessment oversizedAssessment = service.inspect(List.of(attachment(oversized)));
        AttachmentAssessment countAssessment = service.inspect(tooMany);

        assertTrue(oversizedAssessment.findings().getFirst().reason().contains("20 MiB"));
        assertTrue(countAssessment.findings().stream()
                .anyMatch(finding -> finding.reason().contains("upload limit is 5")));
    }

    @Test
    void warnsWhenImageContentDoesNotMatchItsExtension() throws IOException {
        Path file = uploadRoot.resolve("image.png");
        ImageIO.write(new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB), "jpeg", file.toFile());

        AttachmentAssessment assessment = service.inspect(List.of(attachment(file)));

        assertTrue(assessment.hasWarnings());
        assertTrue(assessment.findings().getFirst().reason().contains("do not match"));
    }

    private ReportImage attachment(Path path) {
        return ReportImage.of(1, path.toString(), ImageType.ORIGINAL);
    }
}
