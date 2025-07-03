package com.freshworks.ex.utils.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrClient extends RestClient {

    private static final Logger logger = LoggerFactory.getLogger(FrClient.class);

    private static final String domain = "https://freshworks.freshrelease.com";

    /**
     * Constructs a new RestClient instance for a specific Freshrelease domain.
     */
    public FrClient() {
        super(domain, System.getenv("FR_API_KEY"));
    }


    protected String authorization() {
        logger.debug("Generating authorization header");
        return "Token token=" + this.apiKey;
    }
}
