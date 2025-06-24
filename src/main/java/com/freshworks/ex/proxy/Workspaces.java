package com.freshworks.ex.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.Tool;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Workspaces extends AbstractProxy {
	private static final Logger logger = LoggerFactory.getLogger(Workspaces.class);
	private static final String WORKSPACES = "/api/_/workspaces";

	public Workspaces(String domain, String email, String password) {
		super(domain, email, password);
		logger.debug("Initialized Workspaces proxy for domain: {}", domain);
	}

	@Tool(name = "listWorkspaces")
	public JsonNode listWorkspaces() throws IOException {
		logger.info("Fetching all workspaces");

		String fullPath = WORKSPACES;
		Response response = restClient.get(fullPath);
		if (!response.isSuccessful()) {
			String errorBody = response.body() != null ? response.body().string() : "no body";
			logger.error("Failed to fetch workspaces. Status: {}, Body: {}", response.code(), errorBody);
			throw new IOException("Failed to fetch workspaces: " + response.code());
		}

		return parse(response);
	}

	@Tool(name = "deleteAllNonPrimaryWorkspaces")
	public JsonNode deleteAllNonPrimaryWorkspaces() throws IOException {
		logger.info("Deleting all non-primary workspaces");
		JsonNode workspacesNode = listWorkspaces().get("workspaces");

		List<Integer> deleted = new ArrayList<>();
		if (workspacesNode != null && workspacesNode.isArray()) {
			for (JsonNode ws : workspacesNode) {
				boolean isPrimary = ws.path("primary").asBoolean(false);
				if (!isPrimary) {
					int wsId = ws.path("id").asInt();
					String path = WORKSPACES + "/" + wsId;
					String payload = "{\"state\": \"archived\"}";

					Response resp = restClient.put(path, payload);
					if (!resp.isSuccessful()) {
						logger.warn("Failed to archive workspace {}: {}", wsId, resp.code());
						continue;
					}
					deleted.add(wsId);
					logger.info("Archived workspace ID: {}", wsId);
				}
			}
		}

		Map<String, Object> result = Map.of("status", "success", "archived_count", deleted.size(),
				"archived_workspace_ids", deleted);
		return serializer.parse(serializer.serialize(result));
	}
}