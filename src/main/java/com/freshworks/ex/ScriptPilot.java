package com.freshworks.ex;

import com.freshworks.ex.proxy.AgentProxy;
import com.freshworks.ex.proxy.DepartmentProxy;
import com.freshworks.ex.proxy.RequesterProxy;
import com.freshworks.ex.scenarios.TestCase;
import com.freshworks.ex.scenarios.TestCaseLoader;
import com.freshworks.ex.utils.HtmlReportGenerator;
import com.freshworks.ex.utils.SystemPromptLoader;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.cloudverse.CloudVerseModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class ScriptPilot {
    private static final Logger logger = LoggerFactory.getLogger(ScriptPilot.class);
    private static final String key = System.getenv("CLOUDVERSE_TOKEN");
    private static final String domain = System.getenv("FS_DOMAIN");

    // Load system prompt
    private static final String SYSTEM_PROMPT = SystemPromptLoader.loadSystemPrompt();
    private static final int INTERVAL = 2000;
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    interface Assistant {
        String execute(String userMessage);
    }

    public static void main(String[] args) throws IOException {
        logger.info("Starting ScriptPilot application");

        TestCaseLoader testCaseLoader = new TestCaseLoader();
        List<TestCase> testCases = testCaseLoader.fetch();
        testCases.sort(Comparator.comparing(TestCase::getKey));

        execute(testCases);
        
        // Generate HTML report after all test cases are executed
        HtmlReportGenerator.generateReport(testCases);
    }

    private static void execute(List<TestCase> testCases) {
        Assistant assistant = init();
        int i=0;
        for (TestCase testcase : testCases) {
        	logger.info("\u001B[34mRunning testcase {}\u001B[0m", testcase.getKey());
            if(i<5)
                execute(testcase, assistant);
            i++;
        }
    }

    private static void execute(TestCase testcase, Assistant assistant) {
        long start = System.currentTimeMillis();
        String results = assistant.execute(testcase.getSteps());
        long end = System.currentTimeMillis();
        log(testcase, results.contains("TESTCASE_STATUS: PASSED"), end-start);
        sleep();
    }

    private static void sleep() {
        try {
            Thread.sleep(INTERVAL);
        } catch (InterruptedException e) {
            //suppress
        }
    }

    private static void log(TestCase tc, boolean status, long elapsed) {
        tc.setStatus(status);
        tc.setDuration(elapsed/1000);
        if (status) {
            System.out.println(GREEN + "Execution Status: ✅" + RESET);
        } else {
            System.out.println(RED + "Execution Status: ❌" + RESET);
        }
    }

    private static Assistant init() {
        // Initialize the proxies
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
