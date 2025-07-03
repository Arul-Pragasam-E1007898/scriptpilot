package com.freshworks.ex.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freshworks.ex.proxy.AgentProxy;
import com.freshworks.ex.proxy.DepartmentProxy;
import com.freshworks.ex.proxy.EmailTool;
import com.freshworks.ex.proxy.RequesterProxy;
import com.freshworks.ex.scenarios.TestCase;
import com.freshworks.ex.utils.SystemPromptLoader;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.cloudverse.CloudVerseModel;
import dev.langchain4j.service.AiServices;

public class ScriptRunner {
    private static final String domain = System.getenv("FS_DOMAIN");

    // Load system prompt
    private static final String SYSTEM_PROMPT = SystemPromptLoader.loadSystemPrompt();
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    private static final Logger logger = LoggerFactory.getLogger(ScriptRunner.class);
    private static final String key = System.getenv("CLOUDVERSE_TOKEN");

    // Create the chat model
    private static final ChatModel chatModel = CloudVerseModel.builder()
            .baseUrl("https://cloudverse.freshworkscorp.com/api/v2")
            .modelName("Azure-GPT-4.1")
            .apiKey(key)
            .build();
    private final ScriptLogger scriptLogger;

    public ScriptRunner() {
        this.scriptLogger = new ScriptLogger();
    }

    public void execute(List<TestCase> testCases) {
        Assistant assistant = init();
        for (TestCase testcase : testCases) {
            logger.info("\u001B[34mRunning testcase {}\u001B[0m", testcase.getKey());
            execute(testcase, assistant);
        }
    }

    private void execute(TestCase testcase, Assistant assistant) {
        long start = System.currentTimeMillis();
        String results = assistant.execute(testcase.getSteps());
        long end = System.currentTimeMillis();

        // Log execution results to file
        scriptLogger.log(testcase, results, (end - start) / 1000);

        log(testcase, results.contains("TESTCASE_STATUS: PASSED"), end - start);
    }

    private void log(TestCase tc, boolean status, long elapsed) {
        tc.setStatus(status);
        tc.setDuration(elapsed / 1000);
        if (status) {
            System.out.println(GREEN + "Execution Status: ✅" + RESET);
        } else {
            System.out.println(RED + "Execution Status: ❌" + RESET);
        }
    }

    private Assistant init() {
        // Initialize the proxies
        DepartmentProxy departmentProxy = new DepartmentProxy(domain);
        RequesterProxy requesterProxy = new RequesterProxy(domain);
        AgentProxy agentProxy = new AgentProxy(domain);
        EmailTool email = new EmailTool();


        logger.debug("Initialized chat model");

        // Create the assistant with function calling capability and chat memory
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .tools(departmentProxy, requesterProxy, agentProxy, email)
                .systemMessageProvider(chatMemoryId -> SYSTEM_PROMPT)
                .build();
        logger.info("Assistant service initialized successfully");
        return assistant;
    }
}
