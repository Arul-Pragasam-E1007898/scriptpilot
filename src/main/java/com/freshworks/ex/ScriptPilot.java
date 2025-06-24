package com.freshworks.ex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freshworks.ex.proxy.AgentGroupProxy;
import com.freshworks.ex.proxy.AgentProxy;
import com.freshworks.ex.proxy.DepartmentFieldsProxy;
import com.freshworks.ex.proxy.DepartmentProxy;
import com.freshworks.ex.proxy.RequesterProxy;
import com.freshworks.ex.proxy.TicketProxy;
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
				    You are a helpful Freshservice assistant capable of orchestrating sequences of API actions using a configured toolbox.
				    - When a user request involves multiple steps, execute them sequentially, ensuring each step succeeds before continuing.
				    - If any required parameters are missing, use Javadoc to infer appropriate defaults or generate realistic placeholder values.
				    - For any creation tasks (e.g., creating a requester, workspace, etc.), generate and use a unique, random name unless specified.
				    - Always return clear, structured output summarizing each step and indicating whether it passed or failed.
				""")
		String execute(String userMessage);
	}

	public static void main(String[] args) {
		logger.info("Starting ScriptPilot application");
		Assistant assistant = init();
		for (Testcase testcase : TestcaseRepository.load()) {
			String results = null;
			try {
				String prompt = testcase.steps();
				if (prompt == null || prompt.isBlank()) {
					logger.warn("Skipping testcase {} due to empty prompt", testcase.id());
					continue;
				}
				logger.info("Executing test case {}: {}", testcase.id(), prompt);
				results = assistant.execute(prompt);
				if (results == null || results.isBlank()) {
					throw new Exception("Model returned empty response");
				}
				System.out.println("✅ Testcase Result: " + results);
			} catch (Exception e) {
				logger.error("Assistant execution failed for testcase {}: {}", testcase.id(), e.getMessage(), e);
			}
			System.out.println("Final Output for Testcase " + testcase.id() + ": " + results);
		}
	}

	private static Assistant init() {
		// Initialize the services
//		ContactProxy contactProxy = new ContactProxy("assinfocity13090501");
		String email = "fs.test12@gmail.com";
		String password = "freshservice321";
		AgentProxy agentProxy = new AgentProxy("assinfocity13090501");
		AgentGroupProxy agentGroupProxy = new AgentGroupProxy("assinfocity13090501");
		TicketProxy ticketProxy = new TicketProxy("assinfocity13090501");
		DepartmentProxy departmentProxy = new DepartmentProxy("assinfocity13090501");
		RequesterProxy requesterProxy = new RequesterProxy("assinfocity13090501");
		Workspaces workspaces = new Workspaces("assinfocity13090501", email, password);
		DepartmentFieldsProxy depFields = new DepartmentFieldsProxy("assinfocity13090501", email, password);

		// Create the chat model (replace with your OpenAI API key)
		ChatLanguageModel model = GoogleAiGeminiChatModel.builder().apiKey(System.getenv("OPENAI_API_KEY"))
				.modelName("gemini-2.0-flash").logRequestsAndResponses(true).build();
		logger.info("Initialized OpenAI chat model" + model);

		// Create chat memory with a window of 10 messages
		MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(50);
		logger.debug("Created chat memory with window size: 50");

		// Create the assistant with function calling capability and chat memory
		Assistant assistant = AiServices.builder(Assistant.class).chatLanguageModel(model).chatMemory(chatMemory)
				.tools(requesterProxy, agentProxy, agentGroupProxy, ticketProxy, departmentProxy, workspaces, depFields)
				.build();
		logger.info("Assistant service initialized successfully");
		return assistant;
	}
}
