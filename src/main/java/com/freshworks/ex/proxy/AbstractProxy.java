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

	public AbstractProxy(String domain) {
		logger.debug("Initializing AbstractProxy (legacy) with domain: {}", domain);
		this.serializer = new Serializer();
		this.restClient = new RestClient(domain);
	}

	public AbstractProxy(String domain, String email, String password) {
		logger.debug("Initializing AbstractProxy (modern) with domain: {}, user: {}", domain, email);
		String baseUrl = "https://" + domain + ".freshcmdb.com";
		this.serializer = new Serializer();
		this.restClient = new RestClient(baseUrl, email, password);
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
