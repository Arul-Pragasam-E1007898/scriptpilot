package com.freshworks.ex.utils.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FsClient extends RestClient {

    private static final Logger logger = LoggerFactory.getLogger(FsClient.class);

    /**
     * Constructs a new RestClient instance for a specific Freshservice domain.
     *
     * @param domain The Freshservice domain (e.g., "freshworks299")
     */
    public FsClient(String domain) {
        super("https://" + domain + ".freshcmdb.com/api/v2", System.getenv("FS_API_KEY"));
    }

    @Override
    protected String authorization() {
        logger.debug("Generating authorization header");
        return "Basic " + java.util.Base64.getEncoder().encodeToString((apiKey + ":X").getBytes());
    }
}
