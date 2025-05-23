package com.freshworks.ex.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.ex.utils.RestClient;
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
        this.restClient = new RestClient(domain);
        logger.debug("AbstractProxy initialization completed");
    }

    protected JsonNode parse(Response response) throws IOException {
        Optional<ResponseBody> body = Optional.ofNullable(response.body());
        if(body.isPresent()) {
            String jsonStr = body.get().string();
            return serializer.parse(jsonStr);
        }
        return null;
    }
}
