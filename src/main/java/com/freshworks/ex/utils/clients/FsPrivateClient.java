package com.freshworks.ex.utils.clients;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;

public class FsPrivateClient extends RestClient {

    private static final Logger logger = LoggerFactory.getLogger(FsPrivateClient.class);

    private String basicAuth;
    private String csrfToken;
    private boolean useCsrf = false;

    public FsPrivateClient(String domain, String email, String password) {
        super(domain, email, password);
        this.basicAuth = generateBasicAuth(email, password);
        loginAndFetchCsrf();
    }

    private String generateBasicAuth(String username, String password) {
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        logger.debug("Basic auth set for user: {}", username);
        return "Basic " + encoded;
    }

    private void loginAndFetchCsrf() {
        String loginUrl = baseUrl.replace("/api/v2", "") + "/api/_/bootstrap/me";

        Request request = new Request.Builder()
                .url(loginUrl)
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
                    logger.debug("CSRF token retrieved and will be used");
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

    @Override
    protected String authorization() {
        return basicAuth;
    }
}
