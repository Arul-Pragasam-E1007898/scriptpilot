package com.freshworks.ex;

import java.io.IOException;
import java.util.List;

import com.freshworks.ex.proxy.ContactProxy;
import com.freshworks.ex.utils.FreshReleaseClient;
import com.freshworks.ex.utils.FreshReleaseClient.TestCase;
import com.freshworks.ex.utils.SystemPromptLoader;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.cloudverse.CloudVerseModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ScriptPilot {
    private static final Logger logger = LoggerFactory.getLogger(ScriptPilot.class);
    private static final String key = System.getenv("CLOUDVERSE_TOKEN");

    // Load system prompt at class level
    private static final String SYSTEM_PROMPT = SystemPromptLoader.loadSystemPrompt();

    interface Assistant {
        String execute(String userMessage);
    }

    public static void main(String[] args) throws IOException {
        logger.info("Starting ScriptPilot application");

        Assistant assistant = init();
        FreshReleaseClient freshReleaseClient = new FreshReleaseClient();
        List<TestCase> testCases = freshReleaseClient.fetchTestCasesWithSteps();
        if (testCases.isEmpty()) {
            logger.warn("No test cases found. Exiting.");
            System.exit(0);
        }
        for (TestCase testcase : testCases) {
            logger.info("Running testcase {}", testcase.getKey());
            String results = assistant.execute(testcase.getSteps());
            System.out.println("Testcase : " + testcase.getKey() + ": " + results);
        }
    }

    private static Assistant init() {
        // Initialize the contact service
        ContactProxy contactProxy = new ContactProxy("freshworks299");

        // Create the chat model (replace with your OpenAI API key)
        ChatModel chatModel = CloudVerseModel.builder()
                .baseUrl("https://cloudverse.freshworkscorp.com/api/v2")
                .modelName("Azure-GPT-4.1")
                .apiKey(key)
                .build();
        logger.debug("Initialized OpenAI chat model");
        logger.debug("Initialized OpenAI chat model");


        // Create the assistant with function calling capability and chat memory
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .tools(contactProxy)
                .systemMessageProvider(chatMemoryId -> SYSTEM_PROMPT)
                .build();
        logger.info("Assistant service initialized successfully");
        return assistant;
    }
}
