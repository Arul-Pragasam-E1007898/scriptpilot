package com.freshworks.ex;

import com.freshworks.ex.proxy.AgentProxy;
import com.freshworks.ex.proxy.DepartmentProxy;
import com.freshworks.ex.proxy.RequesterProxy;
import com.freshworks.ex.scenarios.TestCase;
import com.freshworks.ex.scenarios.TestCaseLoader;
import com.freshworks.ex.utils.SystemPromptLoader;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.cloudverse.CloudVerseModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ScriptPilot {
    private static final Logger logger = LoggerFactory.getLogger(ScriptPilot.class);
    private static final String key = System.getenv("CLOUDVERSE_TOKEN");
    private static final String domain = System.getenv("FS_DOMAIN");

    // Load system prompt at class level
    private static final String SYSTEM_PROMPT = SystemPromptLoader.loadSystemPrompt();

    interface Assistant {
        String execute(String userMessage);
    }

    public static void main(String[] args) throws IOException {
        logger.info("Starting ScriptPilot application");

        TestCaseLoader testCaseLoader = new TestCaseLoader();
        List<TestCase> testCases = testCaseLoader.fetch();

        execute(testCases);
    }

    private static void execute(List<TestCase> testCases) {
        Assistant assistant = init();
        for (TestCase testcase : testCases) {
            logger.info("Running testcase {}", testcase.getKey());
            String results = assistant.execute(testcase.getSteps());
            System.out.println("Testcase : " + testcase.getKey() + ": " + results);
        }
    }

    private static Assistant init() {
        // Initialize the contact service

        DepartmentProxy departmentProxy = new DepartmentProxy(domain);
        RequesterProxy requesterProxy = new RequesterProxy(domain);
        AgentProxy agentProxy = new AgentProxy(domain);

        // Create the chat model
        ChatModel chatModel = CloudVerseModel.builder()
                .baseUrl("https://cloudverse.freshworkscorp.com/api/v2")
                .modelName("Azure-GPT-4.1")
                .apiKey(key)
                .build();
        logger.debug("Initialized chat model");

        // Create the assistant with function calling capability and chat memory
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .tools(departmentProxy, requesterProxy, agentProxy)
                .systemMessageProvider(chatMemoryId -> SYSTEM_PROMPT)
                .build();
        logger.info("Assistant service initialized successfully");
        return assistant;
    }
}
