package com.freshworks.ex.proxy;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.ex.utils.RestClient;
import com.freshworks.ex.utils.Serializer;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class AbstractProxy {
    private static final Logger logger = LoggerFactory.getLogger(AbstractProxy.class);
    protected final Serializer serializer;
    protected final RestClient restClient;

    /**
     * Constructor for public API access using API key (no login, no CSRF).
     * @param domain Freshservice domain like "freshworks299"
     */
    public AbstractProxy(String domain) {
        logger.debug("Initializing AbstractProxy (public) with domain: {}", domain);
        this.serializer = new Serializer();
        this.restClient = RestClient.getPublicClient(domain);
    }

    /**
     * Constructor for private API access using email/password login + CSRF.
     * @param domain Freshservice domain like "freshworks299"
     * @param email login email
     * @param password login password
     */
    public AbstractProxy(String domain, String email, String password) {
        logger.debug("Initializing AbstractProxy (private) with domain: {}, user: {}", domain, email);
        this.serializer = new Serializer();
        this.restClient = RestClient.getPrivateClient(domain, email, password);
    }

    protected JsonNode parse(Response response) throws IOException {
        Optional<ResponseBody> body = Optional.ofNullable(response.body());
        if (body.isPresent()) {
            String jsonStr = body.get().string();
            return serializer.parse(jsonStr);
        }
        return null;
    }
}
