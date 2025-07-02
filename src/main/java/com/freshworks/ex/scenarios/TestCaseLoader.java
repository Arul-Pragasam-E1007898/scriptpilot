package com.freshworks.ex.scenarios;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.freshworks.ex.utils.Serializer;
import com.freshworks.ex.utils.clients.FrClient;

import okhttp3.Response;

public class TestCaseLoader {

	private static final FrClient client = new FrClient();
	private static final Serializer serializer = new Serializer();

	private static final Map<String, String> queryParams = Map.of(
			"query_hash[2][condition]", "base_tags.name",
			"query_hash[2][operator]", "is_in",
			"query_hash[2][value][]", "SP_Demo",
			"per_page", "250",
			"page", "1",
			"include", "custom_field,test_case");

	public List<TestCase> fetch() throws IOException {
		Response response = client.get("/FS/test_cases" + "?" + queryParams());
		String payload = response.body().string();
		JsonNode responseJson = serializer.parse(payload);
		return parse(responseJson);
	}

	@NotNull
	private List<TestCase> parse(JsonNode responseJson) {
		ArrayNode testCases = (ArrayNode) responseJson.get("test_cases");
		List<TestCase> testCaseList = new ArrayList<>();
		for (JsonNode testCase : testCases) {
			testCaseList.add(convert(testCase));
		}
		return testCaseList;
	}

	private TestCase convert(JsonNode testCase) {
		return new TestCase(testCase.get("id").asText(),
				testCase.get("key").asText(),
				testCase.get("steps").asText());
	}

	private String queryParams() {
		StringBuilder params = new StringBuilder();
		for (Map.Entry<String, String> entry : queryParams.entrySet()) {
			if (!params.isEmpty()) {
				params.append("&");
			}
			params.append(URLEncoder.encode(entry.getKey())).append("=").append(entry.getValue());
		}
		return params.toString();
	}

}