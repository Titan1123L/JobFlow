package com.jobflow.worker.processor;

import com.jobflow.common.entity.Job;
import com.jobflow.common.security.SsrfGuard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Component
public class ImageResizeJobProcessor implements JobProcessor {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${jobflow.storage.resized-image-path}")
    private String storagePath;

    @Override
    public void process(Job job) throws Exception {
        String sourceUrl = (String) job.getPayload().get("sourceUrl");
        int targetWidth = ((Number) job.getPayload().get("targetWidth")).intValue();
        int targetHeight = ((Number) job.getPayload().get("targetHeight")).intValue();

        // Re-check immediately before fetching, same DNS-rebinding protection as WEBHOOK
        SsrfGuard.assertSafe(sourceUrl);

        byte[] imageBytes = downloadImage(sourceUrl);

        BufferedImage original = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
        if (original == null) {
            throw new IllegalArgumentException("sourceUrl did not return a readable image");
        }

        BufferedImage resized = resize(original, targetWidth, targetHeight);

        Path outputDir = Path.of(storagePath);
        Files.createDirectories(outputDir);
        File outputFile = outputDir.resolve(job.getId() + ".png").toFile();
        ImageIO.write(resized, "png", outputFile);
    }

    private byte[] downloadImage(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to download image, status " + response.statusCode());
        }
        return response.body();
    }

    private BufferedImage resize(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(original, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }
}