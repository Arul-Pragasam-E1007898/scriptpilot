package com.freshworks.ex;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freshworks.ex.proxy.AgentProxy;
import com.freshworks.ex.proxy.DepartmentProxy;
import com.freshworks.ex.proxy.RequesterProxy;
import com.freshworks.ex.scenarios.TestCase;
import com.freshworks.ex.scenarios.TestCaseLoader;
import com.freshworks.ex.utils.SystemPromptLoader;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.cloudverse.CloudVerseModel;
import dev.langchain4j.service.AiServices;

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
        testCases.sort(Comparator.comparing(TestCase::getKey));

        execute(testCases);
    }

    private static void execute(List<TestCase> testCases) {
        Assistant assistant = init();
        for (TestCase testcase : testCases) {
        	logger.info("\u001B[34mRunning testcase {}\u001B[0m", testcase.getKey());
            String results = assistant.execute(testcase.getSteps());
            String RED = "\u001B[31m";
            String GREEN = "\u001B[32m";
            String RESET = "\u001B[0m";
            if (results.contains("TESTCASE_STATUS: FAILED")) {
                System.out.println(RED + "TESTCASE_STATUS: FAILED" + RESET);
            } else if (results.contains("TESTCASE_STATUS: PASSED")) {
                System.out.println(GREEN + "TESTCASE_STATUS: PASSED" + RESET);
            }
            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
