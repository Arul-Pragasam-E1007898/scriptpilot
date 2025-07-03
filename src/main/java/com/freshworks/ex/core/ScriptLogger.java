package com.freshworks.ex.core;

import com.freshworks.ex.ScriptPilot;
import com.freshworks.ex.scenarios.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScriptLogger {

    private static final String LOGS_FOLDER = "logs";
    private static final Logger logger = LoggerFactory.getLogger(ScriptLogger.class);

    public ScriptLogger() {
        createLogsDirectory();
    }

    public void log(TestCase testcase, String results, long duration) {
        try {
            String filename = String.format("%s/%s.log", LOGS_FOLDER, testcase.getKey());

            try (FileWriter writer = new FileWriter(filename)) {
                log(testcase, results, duration, writer);
            }

            logger.debug("Execution results logged to: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to log execution results for test case: {}", testcase.getKey(), e);
        }
    }

    private static void log(TestCase testcase, String results, long duration, FileWriter writer) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        writer.write("=".repeat(80) + "\n");
        writer.write("TEST CASE EXECUTION LOG\n");
        writer.write("=".repeat(80) + "\n");
        writer.write(String.format("Test Case: %s\n", testcase.getKey()));
        writer.write(String.format("Test ID: %s\n", testcase.getId()));
        writer.write(String.format("Execution Time: %s\n", timestamp));
        writer.write(String.format("Duration: %d seconds\n", duration));
        writer.write("=".repeat(80) + "\n");
        writer.write("TEST STEPS:\n");
        writer.write("-".repeat(40) + "\n");
        writer.write(testcase.getSteps() + "\n");
        writer.write("\n");
        writer.write("=".repeat(80) + "\n");
        writer.write("EXECUTION RESULTS:\n");
        writer.write("-".repeat(40) + "\n");
        writer.write(results + "\n");
        writer.write("=".repeat(80) + "\n");
    }

    private void createLogsDirectory() {
        try {
            Path logsPath = Paths.get(LOGS_FOLDER);
            if (!Files.exists(logsPath)) {
                Files.createDirectories(logsPath);
                logger.info("Created logs directory: {}", logsPath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create logs directory", e);
        }
    }
}
