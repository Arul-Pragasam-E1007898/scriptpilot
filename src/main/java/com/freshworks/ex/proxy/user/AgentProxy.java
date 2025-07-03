package com.freshworks.ex.proxy.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.ex.proxy.AbstractProxy;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AgentProxy extends AbstractProxy {
    private static final Logger logger = LoggerFactory.getLogger(AgentProxy.class);
    private static final String AGENTS = "/agents";

    public AgentProxy(String domain) {
        super(domain);
        logger.debug("Initialized AgentProxy with domain: {}", domain);
    }

    @Tool(name = "createAgent")
    public JsonNode createAgent(@P("email address") String email) throws IOException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required to create an agent.");
        }

        String firstName = "TestAgent" + UUID.randomUUID().toString().substring(0, 5);
        String lastName = "User" + UUID.randomUUID().toString().substring(0, 4);
        logger.info("Creating test agent with email: {}", email);

        Map<String, Object> agent = new HashMap<>();
        agent.put("email", email);
        agent.put("first_name", firstName);
        agent.put("last_name", lastName);
        agent.put("job_title", "Test Automation Agent");
        agent.put("occasional", false);
        agent.put("work_phone_number", "1111111111");
        agent.put("mobile_phone_number", "2222222222");
        agent.put("language", "en");
        agent.put("time_format", "12h");
        agent.put("time_zone", "UTC");
        agent.put("background_information", "Created for automated testing");

        String jsonBody = serializer.serialize(agent);
        logger.info("Sending create agent request with body: {}", jsonBody);

        return handleResponse(restClient.post(AGENTS, jsonBody), "createAgentWithDefaults");
    }

    @Tool(name = "getAgent")
    public JsonNode getAgent(Long id) throws IOException {
        logger.info("Fetching agent with ID: {}", id);
        return handleResponse(restClient.get(AGENTS + "/" + id), "getAgent");
    }

    @Tool(name = "listAgents")
    public JsonNode listAgents(Map<String, String> filters) throws IOException {
        logger.info("Listing agents with filters: {}", filters);

        StringBuilder queryBuilder = new StringBuilder(AGENTS);
        if (filters != null && !filters.isEmpty()) {
            queryBuilder.append("?");
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                queryBuilder.append(key).append("=").append(value).append("&");
            }
            // Remove trailing &
            queryBuilder.setLength(queryBuilder.length() - 1);
        }

        return handleResponse(restClient.get(queryBuilder.toString()), "listAgents");
    }

    @Tool(name = "updateAgent")
    public JsonNode updateAgent(Long agentId, String firstName, String lastName, String email, String jobTitle,
                                String mobilePhoneNumber, String workPhoneNumber, String backgroundInfo)
            throws IOException {

        logger.info("Updating agent with ID: {}", agentId);
        Map<String, Object> agent = new HashMap<>();

        if (firstName != null)
            agent.put("first_name", firstName);
        if (lastName != null)
            agent.put("last_name", lastName);
        if (email != null)
            agent.put("email", email);
        if (jobTitle != null)
            agent.put("job_title", jobTitle);
        if (mobilePhoneNumber != null)
            agent.put("mobile_phone_number", mobilePhoneNumber);
        if (workPhoneNumber != null)
            agent.put("work_phone_number", workPhoneNumber);

        if (backgroundInfo != null)
            agent.put("background_information", backgroundInfo);

        String jsonBody = serializer.serialize(agent);
        logger.info("Sending update agent request with body: {}", jsonBody);

        return handleResponse(restClient.put("/api/v2/agents/" + agentId, jsonBody), "updateAgent");
    }

    @Tool(name = "deactivateAgent")
    public JsonNode deactivateAgent(Long agentId) throws IOException {
        logger.info("Deactivating agent with ID: {}", agentId);
        return handleResponse(restClient.delete("/agents/" + agentId), "deactivateAgent");
    }

    @Tool(name = "reactivateAgent")
    public JsonNode reactivateAgent(Long agentId) throws IOException {
        logger.info("Reactivating agent {}", agentId);
        return handleResponse(restClient.put("/agents/" + agentId + "/reactivate", ""), "reactivateAgent");
    }

    @Tool(name = "forgetAgent")
    public JsonNode forgetAgent(Long agentId) throws IOException {
        logger.info("Forgetting agent {}", agentId);
        return handleResponse(restClient.delete("/agents/" + agentId + "/forget"), "forgetAgent");
    }

    @Tool(name = "convertAgentToRequester")
    public JsonNode convertAgentToRequester(Long agentId) throws IOException {
        logger.info("Converting agent {} to requester", agentId);
        return handleResponse(restClient.put("/agents/" + agentId + "/convert_to_requester", ""),
                "convertAgentToRequester");
    }

}