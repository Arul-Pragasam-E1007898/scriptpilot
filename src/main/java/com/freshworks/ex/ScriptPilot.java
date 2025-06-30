package com.freshworks.ex;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freshworks.ex.proxy.AgentProxy;
import com.freshworks.ex.proxy.DepartmentFieldsProxy;
import com.freshworks.ex.proxy.DepartmentProxy;
import com.freshworks.ex.proxy.RequesterProxy;
import com.freshworks.ex.proxy.Workspaces;
import com.freshworks.ex.scenarios.Testcase;
import com.freshworks.ex.scenarios.TestcaseRepository;

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

	public static void main(String[] args) throws InterruptedException {
		logger.info("Starting ScriptPilot application");

		// Load and filter non-empty API keys
		String[] allKeys = { System.getenv("GOOGLE_API_KEY_1"), System.getenv("GOOGLE_API_KEY_2"),
				System.getenv("GOOGLE_API_KEY_3") };

		List<String> validKeys = new ArrayList<>();
		for (String key : allKeys) {
			if (key != null && !key.isBlank()) {
				validKeys.add(key);
			}
		}

		if (validKeys.isEmpty()) {
			logger.error("No valid API keys found. Exiting.");
			System.exit(1);
		}

		int keyIndex = 0;

		for (Testcase testcase : TestcaseRepository.load()) {
			if (!testcase.enabled()) {
				logger.info("Skipping disabled testcase {}", testcase.id());
				continue;
			}

			String prompt = testcase.steps();
			if (prompt == null || prompt.isBlank()) {
				logger.warn("Skipping testcase {} due to empty prompt", testcase.id());
				continue;
			}

			String apiKey = validKeys.get(keyIndex);
			keyIndex = (keyIndex + 1) % validKeys.size(); // round-robin

			String results = null;
			boolean success = false;

			try {
				logger.info("Running testcase {} using API key {}", testcase.id(), maskKey(apiKey));
				Assistant assistant = initWithKey(apiKey);

				results = assistant.execute(prompt);

				if (results == null || results.isBlank()) {
					throw new Exception("Model returned empty response");
				}

				success = true;

			} catch (Exception e) {
				logger.error("Execution failed for testcase {} with key {}: {}", testcase.id(), maskKey(apiKey),
						e.getMessage());
				// Optional: logger.debug("Stack trace:", e);
			}

			if (!success) {
				logger.error("Testcase {} failed using key {}", testcase.id(), maskKey(apiKey));
			}

			System.out.println("Final Output for Testcase " + testcase.id() + ": " + results);
			Thread.sleep(2000);
		}

		logger.info("ScriptPilot application finished.");
	}

	private static Assistant initWithKey(String apiKey) {
		ChatLanguageModel model = GoogleAiGeminiChatModel.builder().apiKey(apiKey).modelName("gemini-2.0-flash")
				.logRequestsAndResponses(true).build();

		MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(1000);

		DepartmentProxy departmentProxy = new DepartmentProxy("obkinfocity17090631");
		RequesterProxy requesterProxy = new RequesterProxy("obkinfocity17090631");
		Workspaces workspaces = new Workspaces("obkinfocity17090631", "fs.test12@gmail.com", "freshservice321");
		DepartmentFieldsProxy depFields = new DepartmentFieldsProxy("obkinfocity17090631", "fs.test12@gmail.com",
				"freshservice321");
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