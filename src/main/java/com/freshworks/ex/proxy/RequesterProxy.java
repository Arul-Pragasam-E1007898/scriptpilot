package com.freshworks.ex.proxy;


import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.Tool;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

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
		Response resp = restClient.get(REQUESTERS);
		if (!resp.isSuccessful())
			throw new IOException("listRequesters failed: " + resp.code());
		return parse(resp);
	}

	@Tool(name = "getRequester")
	public JsonNode getRequester(Long id) throws IOException {
		logger.info("Fetching requester {}", id);
		Response resp = restClient.get(REQUESTERS + "/" + id);
		if (!resp.isSuccessful())
			throw new IOException("getRequester failed: " + resp.code());
		return parse(resp);
	}

	@Tool(name = "createRequester")
	public JsonNode createRequester(Map<String, Object> requesterData) throws IOException {
		logger.info("Creating requester {}", requesterData);
		String payload = serializer.serialize(requesterData);
		Response resp = restClient.post(REQUESTERS, payload);
		if (!resp.isSuccessful())
			throw new IOException("createRequester failed: " + resp.code());
		return parse(resp);
	}

	@Tool(name = "updateRequester")
	public JsonNode updateRequester(Long id, Map<String, Object> requesterData) throws IOException {
		logger.info("Updating requester {} with {}", id, requesterData);
		String payload = serializer.serialize(requesterData);
		Response resp = restClient.put(REQUESTERS + "/" + id, payload);
		if (!resp.isSuccessful())
			throw new IOException("updateRequester failed: " + resp.code());
		return parse(resp);
	}

	@Tool(name = "deleteRequester")
	public JsonNode deleteRequester(Long id) throws IOException {
		logger.info("Deleting requester {}", id);
		Response resp = restClient.delete(REQUESTERS + "/" + id);
		if (!resp.isSuccessful())
			throw new IOException("deleteRequester failed: " + resp.code());
		Map<String, Object> out = Map.of("status", "success", "requester_id", id);
		return serializer.parse(serializer.serialize(out));
	}

	@Tool(name = "convertRequesterToAgent")
	public JsonNode convertRequesterToAgent(Long id) throws IOException {
		logger.info("Converting requester {} to agent", id);
		Response resp = restClient.post(REQUESTERS + "/" + id + "/convert_to_agent", "{}");
		if (!resp.isSuccessful())
			throw new IOException("convertRequesterToAgent failed: " + resp.code());
		return parse(resp);
	}

	@Tool(name = "mergeRequesters")
	public JsonNode mergeRequesters(Long primaryId, List<Long> secondaryIds) throws IOException {
		logger.info("Merging requesters {} into {}", secondaryIds, primaryId);
		Map<String, Object> body = Map.of("primary_requester", primaryId, "secondary_requesters", secondaryIds);
		String payload = serializer.serialize(body);
		Response resp = restClient.post(REQUESTERS + "/merge", payload);
		if (!resp.isSuccessful())
			throw new IOException("mergeRequesters failed: " + resp.code());
		return parse(resp);
	}

	@Tool(name = "listRequesterFields")
	public JsonNode listRequesterFields() throws IOException {
		logger.info("Listing requester fields");
		Response resp = restClient.get(FIELDS);
		if (!resp.isSuccessful())
			throw new IOException("listRequesterFields failed: " + resp.code());
		return parse(resp);
	}
}
