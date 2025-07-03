package com.freshworks.ex.proxy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import dev.langchain4j.agent.tool.Tool;

public class EmailTool {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Tool(name = "generateOrCreateOrNewOrRandomEmail")
    public String generateYopmailEmail() {
        String timestamp = LocalDateTime.now().format(formatter);
        return "testuser_" + timestamp + "@yopmail.com";
    }
}
