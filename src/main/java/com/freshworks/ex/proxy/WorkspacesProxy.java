package com.freshworks.ex.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

public class WorkspacesProxy extends AbstractProxy {
    private static final Logger logger = LoggerFactory.getLogger(WorkspacesProxy.class);
    private static final String WORKSPACES = "/api/_/workspaces";

    public WorkspacesProxy(String domain, String email, String password) {
        super(domain, email, password);
        logger.debug("Initialized Workspaces proxy for domain: {}", domain);
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    @Tool(name = "createOrGenerateOrRandomOrWorkspaceName")
    public String generateRandomWorkspaceName() {
        String randomString = generateRandomString(8);
        return "workspace" + "_" + randomString;
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

    @Tool(name = "createWorkspace")
    public JsonNode createWorkspace(
            @P("template type, default is 'hr'") String template,
            @P("workspace name") String name,
            @P("ticket type, default is 'Issue'") String ticketType) throws IOException {

        if (template == null || template.isBlank()) {
            template = "hr";
        }

        if (ticketType == null || ticketType.isBlank()) {
            ticketType = "Issue";
        }

        if (name == null || name.isBlank()) {
            name = "TestWorkspace-" + UUID.randomUUID().toString().substring(0, 5);
        }

        Map<String, Object> workspace = new HashMap<>();
        workspace.put("name", name);
        workspace.put("description", "Test Desc");
        workspace.put("state", "active");
        workspace.put("template_type", template);
        workspace.put("primary", false);

        Map<String, Object> meta = new HashMap<>();
        meta.put("ticket_type", ticketType);

        Map<String, Object> payload = new HashMap<>();
        payload.put("workspace", workspace);
        payload.put("meta", meta);

        String jsonBody = serializer.serialize(payload);
        logger.info("Sending create workspace request with body: {}", jsonBody);

        return handleResponse(restClient.post(WORKSPACES, jsonBody), "createWorkspace");
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