package com.freshworks.ex.utils;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestClient {
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final String baseUrl;
    private String basicAuth;
    private String csrfToken;
    private boolean useCsrf = false;

    /**
     * Constructor for API key authentication.
     * @param domain Freshservice domain like "freshworks299" — no protocol or slashes
     */
    public RestClient(String domain) {
        this.baseUrl = "https://" + domain + ".freshcmdb.com/api/v2";
        this.client = new OkHttpClient();

        String apiKey = System.getenv("FS_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("FS_API_KEY not found in environment variables!");
        }
        this.basicAuth = "Basic " + Base64.getEncoder().encodeToString((apiKey + ":X").getBytes());
        logger.debug("Initialized RestClient with API key for domain: {}", domain);
    }

    /**
     * Constructor for email/password login + CSRF
     * @param domain Freshservice domain like "freshworks299"
     * @param email login email
     * @param password login password
     */
    public RestClient(String domain, String email, String password) {
        this.baseUrl = domain;
        this.client = new OkHttpClient();
        setBasicAuth(email, password);
        loginAndFetchCsrf();
    }

    private void setBasicAuth(String username, String password) {
        this.basicAuth = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        logger.debug("Basic auth set for user: {}", username);
    }

    private void loginAndFetchCsrf() {
        String url = baseUrl.replace("/api/v2", "") + "/api/_/bootstrap/me";

        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", basicAuth)
            .header("Content-Type", "application/json")
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String bodyStr = response.body().string();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(bodyStr);
                this.csrfToken = root.path("meta").path("csrf_token").asText(null);
                if (csrfToken != null && !csrfToken.isEmpty()) {
                    useCsrf = true;
                    System.out.println("Login successful, CSRF token fetched.");
                } else {
                    logger.warn("CSRF token not found in login response.");
                }
            } else {
                logger.error("Login failed with status: {}", response.code());
            }
        } catch (IOException e) {
            logger.error("Exception during login: {}", e.getMessage(), e);
        }
    }

    private Request.Builder applyHeaders(Request.Builder builder) {
        builder.header("Authorization", basicAuth);
        if (useCsrf && csrfToken != null) {
            builder.header("X-CSRF-Token", csrfToken);
        }
        builder.header("Content-Type", "application/json");
        return builder;
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    public Response get(String path) throws IOException {
        String fullPath = normalizePath(path);
        String url = baseUrl + fullPath;
        logger.debug("GET {}", url);
        Request request = applyHeaders(new Request.Builder().url(url).get()).build();
        return client.newCall(request).execute();
    }

    public Response post(String path, String payload) throws IOException {
        String fullPath = normalizePath(path);
        String url = baseUrl + fullPath;
        logger.debug("POST {} with payload: {}", url, payload);
        RequestBody body = RequestBody.create(payload, JSON);
        Request request = applyHeaders(new Request.Builder().url(url).post(body)).build();
        return client.newCall(request).execute();
    }

    public Response put(String path, String payload) throws IOException {
        String fullPath = normalizePath(path);
        String url = baseUrl + fullPath;
        logger.debug("PUT {} with payload: {}", url, payload);
        RequestBody body = RequestBody.create(payload, JSON);
        Request request = applyHeaders(new Request.Builder().url(url).put(body)).build();
        return client.newCall(request).execute();
    }

    public Response delete(String path) throws IOException {
        String fullPath = normalizePath(path);
        String url = baseUrl + fullPath;
        logger.debug("DELETE {}", url);
        Request request = applyHeaders(new Request.Builder().url(url).delete()).build();
        return client.newCall(request).execute();
    }

    public Response delete(String path, String payload) throws IOException {
        String fullPath = normalizePath(path);
        String url = baseUrl + fullPath;
        logger.debug("DELETE {} with payload: {}", url, payload);
        RequestBody body = RequestBody.create(payload, JSON);
        Request request = applyHeaders(new Request.Builder().url(url).delete(body)).build();
        return client.newCall(request).execute();
    }
    
    public OkHttpClient getOkHttpClient() {
        return client;
    }
    
    public Response post(String path, String payload, Map<String, String> customHeaders) throws IOException {
        String fullPath = normalizePath(path);
        String url = baseUrl + fullPath;
        logger.debug("POST {} with payload: {}", url, payload);
        RequestBody body = RequestBody.create(payload, JSON);

        Request.Builder builder = new Request.Builder().url(url).post(body);

        // Add custom headers
        if (customHeaders != null) {
            for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = applyHeaders(builder).build(); // Still applies default headers
        return client.newCall(request).execute();
    }
    
    public Response post(String path, RequestBody body, Map<String, String> headers) throws IOException {
        String fullPath = normalizePath(path);
        String url = baseUrl + fullPath;
        logger.debug("POST {} with form body and headers: {}", url, headers);

        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);

        // Apply custom headers
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        return client.newCall(request).execute();
    }

}
