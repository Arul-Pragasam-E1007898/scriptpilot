package com.freshworks.ex.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SystemPromptLoader {
    private static final Logger logger = LoggerFactory.getLogger(SystemPromptLoader.class);
    private static final String SYSTEM_PROMPT_FILE = "/system-prompt.md";
    
    public static String loadSystemPrompt() {
        try (InputStream inputStream = SystemPromptLoader.class.getResourceAsStream(SYSTEM_PROMPT_FILE)) {
            if (inputStream == null) {
                logger.error("System prompt file not found: {}", SYSTEM_PROMPT_FILE);
                throw new IllegalStateException("System prompt file not found: " + SYSTEM_PROMPT_FILE);
            }
            
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            logger.debug("Successfully loaded system prompt from: {}", SYSTEM_PROMPT_FILE);
            return content;
            
        } catch (IOException e) {
            logger.error("Failed to load system prompt from: {}", SYSTEM_PROMPT_FILE, e);
            throw new RuntimeException("Failed to load system prompt", e);
        }
    }
} 