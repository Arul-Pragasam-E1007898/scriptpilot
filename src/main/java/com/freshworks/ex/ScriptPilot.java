package com.freshworks.ex;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freshworks.ex.proxy.AgentProxy;
import com.freshworks.ex.proxy.DepartmentFieldsProxy;
import com.freshworks.ex.proxy.DepartmentProxy;
import com.freshworks.ex.proxy.RequesterProxy;
import com.freshworks.ex.proxy.Workspaces;
import com.freshworks.ex.utils.FreshReleaseClient;
import com.freshworks.ex.utils.FreshReleaseClient.TestCase;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

public class ScriptPilot {

	private static final Logger logger = LoggerFactory.getLogger(ScriptPilot.class);

	interface Assistant {
		@SystemMessage("""
				You are a reliable Freshservice assistant designed to orchestrate and execute full test cases via API actions in a sequential manner using a configured toolbox.
				- When a user request involves multiple steps, execute them sequentially, ensuring each step succeeds before continuing.
				- If any required parameters are missing, use Javadoc to infer appropriate defaults or generate realistic placeholder values.
				- For any creation tasks (e.g., creating a requester, workspace, etc.), generate and use a unique, random name unless specified.
				- Always return clear, structured output summarizing each step and indicating whether it passed or failed.
				- Use the configured tools to run API actions as requested.
				""")
		String execute(String userMessage);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		logger.info("Starting ScriptPilot application");

		FreshReleaseClient freshReleaseClient = new FreshReleaseClient();
		List<TestCase> testCases = freshReleaseClient.fetchTestCasesWithSteps();
		if (testCases.isEmpty()) {
			logger.warn("No test cases found. Exiting.");
			System.exit(0);
		}
		String[] allKeys = { System.getenv("GOOGLE_API_KEY_1"), System.getenv("GOOGLE_API_KEY_2"),
				System.getenv("GOOGLE_API_KEY_3") };
		int keyIndex = 0;
		for (TestCase testcase : testCases) {
			if (testcase.getSteps() == null || testcase.getSteps().isBlank()) {
				logger.warn("Skipping testcase {} due to empty steps", testcase.getKey());
				continue;
			}
			String apiKey = null;
			while (keyIndex < allKeys.length) {
				if (allKeys[keyIndex] != null && !allKeys[keyIndex].isBlank()) {
					apiKey = allKeys[keyIndex];
					break;
				}
				keyIndex++;
			}
			if (apiKey == null) {
				logger.error("No valid API keys found. Exiting.");
				System.exit(1);
			}
			keyIndex = (keyIndex + 1) % allKeys.length;
			try {
				logger.info("Running testcase {} using API key {}", testcase.getKey(), maskKey(apiKey));
				Assistant assistant = initWithKey(apiKey);
				String results = assistant.execute(testcase.getSteps());
				if (results == null || results.isBlank()) {
					throw new Exception("Model returned empty response");
				}
				System.out.println("Final Output for Testcase " + testcase.getKey() + ": " + results);
			} catch (Exception e) {
				logger.error("Execution failed for testcase {}: {}", testcase.getKey(), e.getMessage());
			}
			Thread.sleep(2000); // delay between runs
		}
		logger.info("ScriptPilot application finished.");
	}

	private static Assistant initWithKey(String apiKey) {
		ChatLanguageModel model = GoogleAiGeminiChatModel.builder().apiKey(apiKey).modelName("gemini-2.0-flash")
				.maxRetries(2).maxOutputTokens(500).logRequestsAndResponses(false).build();
		MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(100);
		DepartmentProxy departmentProxy = new DepartmentProxy("obkinfocity17090631");
		RequesterProxy requesterProxy = new RequesterProxy("obkinfocity17090631");
		Workspaces workspaces = new Workspaces("obkinfocity17090631", "fs.test12@gmail.com", "freshservice321");
		DepartmentFieldsProxy depFields = new DepartmentFieldsProxy("obkinfocity17090631", "fs.test12@gmail.com", "freshservice321");
		AgentProxy agentProxy = new AgentProxy("obkinfocity17090631");
		return AiServices.builder(Assistant.class).chatLanguageModel(model).chatMemory(chatMemory)
				.tools(agentProxy, requesterProxy, departmentProxy, workspaces, depFields).build();
	}
	private static String maskKey(String key) {
		if (key == null || key.length() < 6)
			return "****";
		return key.substring(0, 3) + "..." + key.substring(key.length() - 3);
	}
}
