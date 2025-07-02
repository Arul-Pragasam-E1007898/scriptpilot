package com.freshworks.ex.proxy;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.Tool;
import okhttp3.Response;

public class RequesterProxy extends AbstractProxy {
	private static final Logger logger = LoggerFactory.getLogger(RequesterProxy.class);
	private static final String REQUESTERS = "/requesters";
	private static final String FIELDS = "/requester_fields";

	public RequesterProxy(String domain) {
		super(domain);
		logger.debug("Initialized RequesterProxy with domain: {}", domain);
	}


	@Tool(name = "listRequesters")
	public JsonNode listRequesters() throws IOException {
		logger.info("Listing all requesters");
		return handleResponse(restClient.get(REQUESTERS), "listRequesters");
	}

	@Tool(name = "getRequester")
	public JsonNode getRequester(Long id) throws IOException {
		logger.info("Fetching requester {}", id);
		return handleResponse(restClient.get(REQUESTERS + "/" + id), "getRequester");
	}

	@Tool(name = "createRequesterOrContact")
	public JsonNode createRequesterOrContact(String email) throws IOException {
		String firstName = "Test" + UUID.randomUUID().toString().substring(0, 5);
		String lastName = "User" + UUID.randomUUID().toString().substring(0, 3);
		logger.info("Creating requester with email: {}", email);

		Map<String, Object> contact = new HashMap<>();
		if (email != null)
			contact.put("primary_email", email);
		contact.put("first_name", firstName);
		contact.put("last_name", lastName);
		contact.put("work_phone_number", "9999999999");
		contact.put("mobile_phone_number", "9999999999");
		contact.put("job_title", "Test Automation Engineer");
		contact.put("language", "en");
		contact.put("time_format", "12h");
		contact.put("time_zone", "UTC");
		contact.put("background_information", "Automated test contact");

		String jsonBody = serializer.serialize(contact);
		return handleResponse(restClient.post(REQUESTERS, jsonBody), "createRequesterOrContact");
	}

	@Tool(name = "updateRequester")
	public JsonNode updateRequester(Long id, Map<String, Object> requesterData) throws IOException {
		logger.info("Updating requester {} with {}", id, requesterData);
		String payload = serializer.serialize(requesterData);
		return handleResponse(restClient.put(REQUESTERS + "/" + id, payload), "updateRequester");
	}

	@Tool(name = "convertRequesterToAgent")
	public JsonNode convertRequesterToAgent(Long id) throws IOException {
		logger.info("Converting requester {} to agent", id);
		return handleResponse(restClient.put(REQUESTERS + "/" + id + "/convert_to_agent", ""),
				"convertRequesterToAgent");
	}

	@Tool(name = "mergeRequesters")
	public JsonNode mergeRequesters(Long primaryId, List<Long> secondaryIds) throws IOException {
		logger.info("Merging secondary requesters {} into primary requester {}", secondaryIds, primaryId);
		String secondaryIdsParam = secondaryIds.stream().map(String::valueOf).collect(Collectors.joining(","));
		String url = REQUESTERS + "/" + primaryId + "/merge?secondary_requesters=" + secondaryIdsParam;
		return handleResponse(restClient.put(url, ""), "mergeRequesters");
	}

	@Tool(name = "listRequesterFields")
	public JsonNode listRequesterFields() throws IOException {
		logger.info("Listing requester fields");
		return handleResponse(restClient.get(FIELDS), "listRequesterFields");
	}

	@Tool(name = "getRequesterAssignmentHistory")
	public JsonNode getRequesterAssignmentHistory(Long id) throws IOException {
		logger.info("Fetching assignment history for user/requester {}", id);
		return handleResponse(restClient.get("users/" + id + "/assignment-history"), "getRequesterAssignmentHistory");
	}

	@Tool(name = "forgetRequester")
	public JsonNode forgetRequester(Long id) throws IOException {
		logger.info("Forgetting requester/contact {}", id);
		Response response = restClient.delete(REQUESTERS + "/" + id + "/forget");
		if (!response.isSuccessful()) {
			return handleResponse(response, "forgetRequester");
		}
		Map<String, Object> result = Map.of("status", "success", "message", "Requester forgotten successfully",
				"requester_id", id);
		return serializer.parse(serializer.serialize(result));
	}


	@Tool(name = "deactivateRequester")
	public JsonNode deactivateRequester(Long id) throws IOException {
		logger.info("Deactivating requester {}", id);
		return handleResponse(restClient.delete(REQUESTERS + "/" + id), "deactivateRequester");
	}

	@Tool(name = "reactivateRequester")
	public JsonNode reactivateRequester(Long id) throws IOException {
		logger.info("Reactivating requester {}", id);
		return handleResponse(restClient.put(REQUESTERS + "/" + id + "/reactivate", ""), "reactivateRequester");
	}
}