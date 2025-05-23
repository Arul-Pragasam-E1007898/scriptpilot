package com.freshworks.ex.utils;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * HTTP client for making REST API calls to Freshservice.
 * This class provides methods for making HTTP requests (GET, POST, PUT, DELETE)
 * to the Freshservice API with proper authentication and JSON handling.
 * It uses OkHttp as the underlying HTTP client library.
 */
public class RestClient {
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);
    
    /** HTTP header name for authorization */
    private static final String AUTHORIZATION = "Authorization";
    
    /** Media type for JSON content */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /** Base URL for the Freshservice API */
    private final String baseUrl;
    
    /** API key for authentication */
    private final String apiKey;
    
    /** OkHttp client instance for making HTTP requests */
    private final OkHttpClient client;

    /**
     * Constructs a new RestClient instance for a specific Freshservice domain.
     * 
     * @param domain The Freshservice domain (e.g., "freshworks299")
     */
    public RestClient(String domain) {
        this.baseUrl = "https://" + domain + ".freshservice.com/api/v2";
        this.apiKey = System.getenv("FS_API_KEY");
        this.client = new OkHttpClient();
        logger.debug("Initialized RestClient with baseUrl: {}", baseUrl);
    }

    /**
     * Performs a GET request to the specified path.
     * 
     * @param path The API endpoint path to request
     * @return Response object containing the server's response
     * @throws IOException if the request fails or cannot be executed
     */
    public Response get(String path) throws IOException {
        logger.debug("Preparing GET request to path: {}", path);
        Request request = new Request.Builder().url(baseUrl + path)
                .addHeader(AUTHORIZATION, authorization()).get()
                .build();
        logger.debug("GET request built with URL: {}", request.url());
        return execute(request);
    }

    /**
     * Performs a POST request to the specified path with a JSON payload.
     * 
     * @param path The API endpoint path to request
     * @param payload The JSON string to send in the request body
     * @return Response object containing the server's response
     * @throws IOException if the request fails or cannot be executed
     */
    public Response post(String path, String payload) throws IOException {
        logger.debug("Preparing POST request to path: {} with payload: {}", path, payload);
        Request request = new Request.Builder().url(baseUrl + path)
                .addHeader(AUTHORIZATION, authorization())
                .post(RequestBody.create(payload, JSON))
                .build();
        logger.debug("POST request built with URL: {}", request.url());
        return execute(request);
    }

    /**
     * Performs a PUT request to the specified path with a JSON payload.
     * 
     * @param path The API endpoint path to request
     * @param payload The JSON string to send in the request body
     * @return Response object containing the server's response
     * @throws IOException if the request fails or cannot be executed
     */
    public Response put(String path, String payload) throws IOException {
        logger.debug("Preparing PUT request to path: {} with payload: {}", path, payload);
        Request request = new Request.Builder().url(baseUrl + path)
                .addHeader(AUTHORIZATION, authorization())
                .post(RequestBody.create(payload, JSON))
                .build();
        logger.debug("PUT request built with URL: {}", request.url());
        return execute(request);
    }

    /**
     * Performs a DELETE request to the specified path.
     * 
     * @param path The API endpoint path to request
     * @return Response object containing the server's response
     * @throws IOException if the request fails or cannot be executed
     */
    public Response delete(String path) throws IOException {
        logger.debug("Preparing DELETE request to path: {}", path);
        Request request = new Request.Builder().url(baseUrl + path)
                .addHeader(AUTHORIZATION, authorization()).delete()
                .build();
        logger.debug("DELETE request built with URL: {}", request.url());
        return execute(request);
    }

    /**
     * Executes an HTTP request and returns the response.
     * This method is used internally by all HTTP method-specific methods.
     * 
     * @param request The HTTP request to execute
     * @return Response object containing the server's response
     * @throws IOException if the request fails or cannot be executed
     */
    public Response execute(Request request) throws IOException {
        logger.debug("Executing {} request to: {}", request.method(), request.url());
        Response response = client.newCall(request).execute();
        logger.debug("Received response with status code: {} for {} request to: {}", 
            response.code(), request.method(), request.url());
        return response;
    }

    /**
     * Generates the Basic Authentication header value using the API key.
     * The API key is combined with ":X" and Base64 encoded as per Freshservice's requirements.
     * 
     * @return The Basic Authentication header value
     */
    private String authorization() {
        logger.debug("Generating authorization header");
        return "Basic " + java.util.Base64.getEncoder().encodeToString((apiKey + ":X").getBytes());
    }
}
