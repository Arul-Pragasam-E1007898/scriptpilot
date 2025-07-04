package com.freshworks.ex.utils.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class FsPrivateClient extends RestClient {

    private static final Logger logger = LoggerFactory.getLogger(FsPrivateClient.class);

    private final String basicAuth;

    public FsPrivateClient(String domain, String email, String password) {
        super(domain, email, password);
        this.basicAuth = generateBasicAuth(email, password);
    }

    private String generateBasicAuth(String username, String password) {
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        logger.debug("Basic auth set for user: {}", username);
        return "Basic " + encoded;
    }

    protected String authorization() {
        return basicAuth;
    }
}
