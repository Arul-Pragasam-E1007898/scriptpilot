package com.freshworks.ex.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.ex.utils.clients.FsClient;
import com.freshworks.ex.utils.clients.FsPrivateClient;
import com.freshworks.ex.utils.clients.RestClient;
import com.freshworks.ex.utils.Serializer;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class AbstractProxy {
    private static final Logger logger = LoggerFactory.getLogger(AbstractProxy.class);
    protected final Serializer serializer;
    protected final RestClient restClient;

    public AbstractProxy(String domain) {
        logger.debug("Initializing AbstractProxy with domain: {}", domain);
        this.serializer = new Serializer();
        this.restClient = new FsClient(domain);
        logger.debug("AbstractProxy initialization completed");
    }

    public AbstractProxy(String domain, String email, String password) {
        logger.debug("Initializing AbstractProxy (Private) with domain: {}, user: {}", domain, email);
        String baseUrl = "https://" + domain + ".freshcmdb.com";
        this.serializer = new Serializer();
        this.restClient = new FsPrivateClient(baseUrl, email, password);
    }

    protected JsonNode parse(Response response) throws IOException {
        Optional<ResponseBody> body = Optional.ofNullable(response.body());
        if (body.isPresent()) {
            String jsonStr = body.get().string();
            return serializer.parse(jsonStr);
        }
        return null;
    }

    protected JsonNode handleResponse(Response resp, String action) throws IOException {
        String body = resp.body().string();
        if (!resp.isSuccessful()) {
            logger.warn("{} failed: {} - {}", action, resp.code(), body);
            return serializer.parse("{\"error\": \"" + action + " failed\", \"code\": " + resp.code() + "}");
        }
        return serializer.parse(body);
    }
}
