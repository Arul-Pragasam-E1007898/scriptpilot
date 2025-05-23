package com.freshworks.ex.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.ex.entities.Requester;
import dev.langchain4j.agent.tool.Tool;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ContactProxy extends AbstractProxy {
    private static final Logger logger = LoggerFactory.getLogger(ContactProxy.class);
    public static final String REQUESTERS = "/requesters";

    public ContactProxy(String domain) {
        super(domain);
        logger.debug("Initialized ContactProxy with domain: {}", domain);
    }

    /**
     * Creates a contact using the given email.
     * This method provides type safety and validation through the Requester record.
     *
     * @param email The Requester primary email
     * @return JsonNode containing the created contact's data
     * @throws IOException if the request fails
     */
    @Tool(name = "createContact")
    public JsonNode createContact(String email, String firstName) throws IOException {
        Requester requester = Requester.builder().primaryEmail(email).firstName(firstName).build();
        logger.info("Creating contact with email: {}", requester.getPrimaryEmail());
        String jsonBody = serializer.serialize(requester);
        logger.debug("Sending create contact request with body: {}", jsonBody);
        Response response = restClient.post(REQUESTERS, jsonBody);
        if (!response.isSuccessful()) {
            logger.error("Failed to create contact. Status code: {}", response.code());
            throw new IOException("Failed to create contact: " + response.code());
        }
        return parse(response);
    }

    @Tool(name = "updateContact")
    public JsonNode updateContact(Long contactId, String name, String email, String phone, String title) throws IOException {
        logger.info("Updating contact with ID: {}", contactId);
        Map<String, Object> contact = new HashMap<>();
        if (name != null) contact.put("first_name", name);
        if (email != null) contact.put("primary_email", email);
        if (phone != null) contact.put("work_phone_number", phone);
        if (title != null) contact.put("job_title", title);

        String jsonBody = serializer.serialize(contact);
        logger.debug("Sending update contact request with body: {}", jsonBody);
        Response response = restClient.put(REQUESTERS + '/' + contactId, jsonBody);
        if (!response.isSuccessful()) {
            logger.error("Failed to update contact. Status code: {}", response.code());
            throw new IOException("Failed to update contact: " + response.code());
        }
        return parse(response);
    }

    @Tool(name = "deleteContact")
    public JsonNode deleteContact(Long contactId) throws IOException {
        logger.info("Deleting contact with ID: {}", contactId);
        Response response = restClient.delete(REQUESTERS + '/' + contactId);
        if (!response.isSuccessful()) {
            logger.error("Failed to delete contact. Status code: {}", response.code());
            throw new IOException("Failed to delete contact: " + response.code());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Contact deleted successfully");
        result.put("contact_id", contactId);
        return serializer.parse(serializer.serialize(result));
    }

    @Tool(name = "forgetContact")
    public JsonNode forgetContact(Long contactId) throws IOException {
        logger.info("Forget contact with ID: {}", contactId);
        Response response = restClient.delete(REQUESTERS + '/' + contactId + "/forget");
        if (!response.isSuccessful()) {
            logger.error("Failed to forget contact. Status code: {}", response.code());
            throw new IOException("Failed to forget contact: " + response.code());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Contact forgotten successfully");
        result.put("contact_id", contactId);
        return serializer.parse(serializer.serialize(result));
    }

    @Tool(name = "getContact")
    public JsonNode getContact(Long contactId) throws IOException {
        logger.info("Fetching contact with ID: {}", contactId);
        Response response = restClient.get(REQUESTERS + '/' + contactId);
        if (!response.isSuccessful()) {
            logger.error("Failed to get contact. Status code: {}", response.code());
            throw new IOException("Failed to get contact: " + response.code());
        }
        return parse(response);
    }
} 