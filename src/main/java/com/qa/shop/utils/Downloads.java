package com.qa.shop.utils;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.FluentWait;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

/** Waits for a browser download to finish (no partial .crdownload / .part file left). */
public final class Downloads {

    private Downloads() { }

    public static Path waitForFile(Path dir, String extension, Duration timeout) {
        try {
            return new FluentWait<>(dir)
                    .withTimeout(timeout)
                    .pollingEvery(Duration.ofMillis(250))
                    .until(d -> completedFile(d, extension).orElse(null));
        } catch (TimeoutException e) {
            throw new TimeoutException("No completed *" + extension + " download in " + dir + " after " + timeout, e);
        }
    }

    public static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Optional<Path> completedFile(Path dir, String extension) {
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.toString().endsWith(extension))
                    .filter(p -> !p.toString().endsWith(".crdownload") && !p.toString().endsWith(".part"))
                    .filter(p -> p.toFile().length() > 0)
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
