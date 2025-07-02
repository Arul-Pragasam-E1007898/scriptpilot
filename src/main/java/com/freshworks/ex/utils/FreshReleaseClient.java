package com.freshworks.ex.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FreshReleaseClient {

	private static final OkHttpClient client = new OkHttpClient();
	private static final Gson gson = new Gson();

	private final String url;
	private final String projectKey;
	private final String token;
	private final String baseUrl;
	private final Map<String, String> headers;

	public FreshReleaseClient() {
		this.url = "https://freshworks.freshrelease.com";
		this.projectKey = "FS";
		this.token = System.getenv("FRESHRELEASE_API_TOKEN"); // get from env variable
		this.baseUrl = url + "/" + projectKey;

		if (token == null || token.isBlank()) {
			throw new IllegalStateException("Environment variable FRESHRELEASE_API_TOKEN is not set");
		}

		this.headers = Map.of("Content-Type", "application/json", "Accept", "application/json", "Authorization",
				"Token token=" + token);
	}

	private JsonObject makeApiCall(String reqType, String path, JsonObject body, Map<String, String> queryParams,
			String methodName) throws IOException {
		String fullPath = baseUrl + path;
		fullPath = fullPath.replace("FS/FS", "FS");

		HttpUrl.Builder urlBuilder = HttpUrl.parse(fullPath).newBuilder();
		if (queryParams != null) {
			queryParams.forEach(urlBuilder::addQueryParameter);
		}

		Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());
		headers.forEach(requestBuilder::addHeader);

		if ("POST".equalsIgnoreCase(reqType) || "PUT".equalsIgnoreCase(reqType) || "DELETE".equalsIgnoreCase(reqType)) {
			RequestBody requestBody = RequestBody.create(gson.toJson(body), MediaType.parse("application/json"));
			switch (reqType.toUpperCase()) {
			case "POST":
				requestBuilder.post(requestBody);
				break;
			case "PUT":
				requestBuilder.put(requestBody);
				break;
			case "DELETE":
				requestBuilder.delete(requestBody);
				break;
			}
		} else {
			requestBuilder.get();
		}

		try (Response response = client.newCall(requestBuilder.build()).execute()) {
			String responseBody = response.body().string();
			if (!response.isSuccessful()) {
				throw new IOException(
						"Unexpected API Response for " + methodName + ": " + response.code() + " - " + responseBody);
			}
			return JsonParser.parseString(responseBody).getAsJsonObject();
		}
	}

	public List<TestCase> fetchTestCasesWithSteps() throws IOException {
		List<TestCase> testCases = new ArrayList<>();
		Map<String, String> queryParams = Map.of("query_hash[2][condition]", "base_tags.name",
				"query_hash[2][operator]", "is_in", "query_hash[2][value][]", "SP_Demo");

		JsonObject response = makeApiCall("GET", "/test_cases?per_page=250&page=1&include=custom_field,test_case", null,
				queryParams, "get_all_cases");

		JsonArray casesArray = null;
		if (response.has("test_cases") && !response.get("test_cases").isJsonNull()) {
			casesArray = response.getAsJsonArray("test_cases");
		}

		if (casesArray == null) {
			throw new IOException("Response missing 'test_cases' array");
		}

		for (JsonElement element : casesArray) {
			JsonObject obj = element.getAsJsonObject();
			String id = obj.get("id").getAsString();
			String key = obj.get("key").getAsString();
			String steps = null;
			if (obj.has("steps") && !obj.get("steps").isJsonNull()) {
				steps = obj.get("steps").getAsString();
			}
			testCases.add(new TestCase(id, key, steps));
		}
		return testCases;
	}

	// Simple POJO class for test case
	public static class TestCase {
		private final String id;
		private final String key;
		private final String steps;

		public TestCase(String id, String key, String steps) {
			this.id = id;
			this.key = key;
			this.steps = steps;
		}

		public String getId() {
			return id;
		}

		public String getKey() {
			return key;
		}

		public String getSteps() {
			return steps;
		}
	}
}