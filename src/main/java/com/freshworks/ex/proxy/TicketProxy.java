package com.freshworks.ex.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.ex.proxy.user.RequesterProxy;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TicketProxy extends AbstractProxy {

    private final RequesterProxy requesterProxy;
    private static final Logger logger = LoggerFactory.getLogger(TicketProxy.class);
    private static final String TICKETS = "/tickets";

    public TicketProxy(String domain) {
        super(domain);
        this.requesterProxy = new RequesterProxy(domain);
    }

    private static final Map<String, Integer> PRIORITY_MAP = Map.of(
            "low", 1,
            "medium", 2,
            "high", 3,
            "urgent", 4
    );

    private static final Map<String, Integer> STATUS_MAP = Map.of(
            "open", 2,
            "pending", 3,
            "resolved", 4,
            "closed", 5
    );

    private static final Map<String, Integer> SOURCE_MAP = Map.of(
            "email", 1,
            "portal", 2,
            "phone", 3,
            "chat", 4,
            "feedback widget", 5,
            "yammer", 6,
            "aws cloudwatch", 7,
            "pagerduty", 8,
            "walkup", 9,
            "slack", 10
    );

    @Tool(name = "createTicket")
    public JsonNode createTicket(
            @P("subject") String subject,
            @P("description") String description,
            @P("priority") String priority,
            @P("status") String status,
            @P("email") String email
    ) throws IOException {
        int priorityVal = mapToValue(priority, PRIORITY_MAP); // Default = low
        int statusVal = mapToValue(status, STATUS_MAP);
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("subject", subject != null ? subject : "Test Subject");
        ticket.put("description", description != null ? description : "Test Description");
        ticket.put("priority", priorityVal);
        ticket.put("status", statusVal);
        ticket.put("email", email);
        String payload = serializer.serialize(ticket);
        logger.info("Initialized Create Ticket with payload: {}", payload);
        return handleResponse(restClient.post("/tickets", payload), "createTicket");
    }

    @Tool(name = "updateTicket")
    public JsonNode updateTicket(
            @P("ticket id") Long ticketId,
            @P("subject") String subject,
            @P("description") String description,
            @P("priority") int priority,
            @P("status") int status,
            @P("source") String source,
            @P("tags") String[] tags,
            @P("bypass_mandatory") Boolean bypassMandatory
    ) throws IOException {
        Map<String, Object> ticket = new HashMap<>();
        if (subject != null) ticket.put("subject", subject);
        if (description != null) ticket.put("description", description);
        ticket.put("priority", priority);
        ticket.put("status", status);
        ticket.put("source", mapToValue(source, SOURCE_MAP));
        if (tags != null) ticket.put("tags", tags);

        Map<String, Object> payload = Map.of("ticket", ticket);
        String jsonBody = serializer.serialize(payload);

        String url = TICKETS + "/" + ticketId;
        if (Boolean.TRUE.equals(bypassMandatory)) {
            url += "?bypass_mandatory=true";
        }
        logger.info("Sending update ticket request to {} with payload: {}", url, jsonBody);
        return handleResponse(restClient.put(url, jsonBody), "createTicket");
    }

    private int mapToValue(String key, Map<String, Integer> map) {
        if (key == null) {
            return getDefaultValue(map);
        }
        return map.getOrDefault(key.toLowerCase(), getDefaultValue(map));
    }

    private int getDefaultValue(Map<String, Integer> map) {
        // Customize this per map if needed
        if (map == PRIORITY_MAP) {
            return PRIORITY_MAP.get("low");
        } else if (map == STATUS_MAP) {
            return STATUS_MAP.get("open");
        } else if (map == SOURCE_MAP) {
            return SOURCE_MAP.get("email");
        }
        return -1; // fallback if unknown map
    }

    @Tool(name = "viewTicket")
    public JsonNode viewTicket(@P("ticket ID") Long ticketId, @P("include related data") String include) throws IOException {
        String path = TICKETS + "/" + ticketId;
        if (include != null && !include.isBlank()) {
            path += "?include=" + include;
        }
        logger.info("Fetching ticket {} include={}", ticketId, include);
        return handleResponse(restClient.get(path), "getTicket");
    }

    @Tool(name = "deleteTicket")
    public JsonNode deleteTicket(@P("ticket ID") Long ticketId) throws IOException {
        logger.info("Delete ticket {}", ticketId);
        return handleResponse(restClient.delete(TICKETS + "/" + ticketId), "deleteTicket");
    }
}
