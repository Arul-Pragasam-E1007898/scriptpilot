package com.freshworks.ex.proxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.Tool;
import okhttp3.FormBody;
import okhttp3.Response;

public class DepartmentFieldsProxy extends AbstractProxy {

	private static final Logger logger = LoggerFactory.getLogger(DepartmentFieldsProxy.class);
	private static final String DEPARTMENT_FIELDS_ADMIN = "/admin/department_fields";

	public DepartmentFieldsProxy(String domain, String email, String password) {
		super(domain, email, password);
		logger.debug("Initialized DepartmentFieldsProxy for domain: {}", domain);
	}

	@Tool(name = "createDepartmentFields")
	public JsonNode createDepartmentFields() throws IOException {

		String jsonData = "[{\"type\": \"text\", \"label\": \"STF\", \"field_type\": \"custom_text\", \"id\": null, \"field_options\": null, \"action\": \"create\", \"custom_field_choices_attributes\": [], \"required\": false}]";

		FormBody formBody = new FormBody.Builder()
				.add("jsonData", jsonData).build();
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		Response response = restClient.post(DEPARTMENT_FIELDS_ADMIN, formBody, headers);

		if (!response.isSuccessful()) {
			String errorBody = response.body() != null ? response.body().string() : "no body";
			logger.error("Failed to create department fields. Status: {}, Body: {}", response.code(), errorBody);
			throw new IOException("Failed to create department fields: " + response.code());
		}

		return parse(response);
	}
}
