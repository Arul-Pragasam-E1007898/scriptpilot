package com.freshworks.ex.proxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.Tool;
import okhttp3.Response;

public class RolesProxy extends AbstractProxy {

	private static final Logger logger = LoggerFactory.getLogger(RolesProxy.class);
	private static final String ROLES = "/api/admin/roles";

	public RolesProxy(String domain, String email, String password) {
		super(domain, email, password);
		logger.debug("Initialized Workspaces proxy for domain: {}", domain);
	}

	@Tool(name = "createRole")
	public JsonNode createRole(Map<String, Object> roleValues) throws IOException {
		logger.info("Creating role with values: {}", serializer.serialize(roleValues));
		Map<String, Object> payload = Map.of("role", roleValues);

		Response response = restClient.post(ROLES, serializer.serialize(payload));
		if (!response.isSuccessful()) {
			logger.error("Failed to create role. Status: {}", response.code());
			throw new IOException("Failed to create role: " + response.code());
		}

		return parse(response);
	}

	@Tool(name = "updateRole")
	public JsonNode updateRole(Long roleId, Map<String, Object> updates) throws IOException {
		logger.info("Updating role with ID: {}", roleId);
		Map<String, Object> payload = Map.of("role", updates);

		Response response = restClient.put(ROLES + '/' + roleId, serializer.serialize(payload));
		if (!response.isSuccessful()) {
			logger.error("Failed to update role. Status: {}", response.code());
			throw new IOException("Failed to update role: " + response.code());
		}

		return parse(response);
	}

	@Tool(name = "getAllRoles")
	public JsonNode getAllRoles() throws IOException {
		logger.info("Fetching all roles");

		Response response = restClient.get(ROLES);
		if (!response.isSuccessful()) {
			logger.error("Failed to fetch roles. Status: {}", response.code());
			throw new IOException("Failed to fetch roles: " + response.code());
		}

		return parse(response);
	}

	@Tool(name = "getRoleByName")
	public JsonNode getRoleByName(String name) throws IOException {
		logger.info("Searching for role by name: {}", name);
		JsonNode roles = getAllRoles();
		if (roles.isArray()) {
			for (JsonNode role : roles) {
				if (role.has("name") && role.get("name").asText().equalsIgnoreCase(name)) {
					return role;
				}
			}
		}
		logger.warn("Role with name '{}' not found", name);
		return null;
	}

	@Tool(name = "deleteRole")
	public JsonNode deleteRole(String nameOrId) throws IOException {
		logger.info("Deleting role with identifier: {}", nameOrId);
		Long roleId;
		try {
			roleId = Long.parseLong(nameOrId);
		} catch (NumberFormatException e) {
			JsonNode role = getRoleByName(nameOrId);
			if (role == null || !role.has("id")) {
				logger.error("Role not found with name: {}", nameOrId);
				throw new IOException("Role not found with name: " + nameOrId);
			}
			roleId = role.get("id").asLong();
		}

		Response response = restClient.delete(ROLES + '/' + roleId);
		if (!response.isSuccessful()) {
			logger.error("Failed to delete role. Status: {}", response.code());
			throw new IOException("Failed to delete role: " + response.code());
		}

		Map<String, Object> result = new HashMap<>();
		result.put("status", "success");
		result.put("message", "Role deleted successfully");
		result.put("role_id", roleId);
		return serializer.parse(serializer.serialize(result));
	}

	@Tool(name = "deleteAllRoles")
	public JsonNode deleteAllRoles() throws IOException {
		logger.info("Deleting all roles");
		JsonNode roles = getAllRoles();
		int deletedCount = 0;

		if (roles.isArray()) {
			for (JsonNode role : roles) {
				if (role.has("id")) {
					Long roleId = role.get("id").asLong();
					Response response = restClient.delete(ROLES + '/' + roleId);
					if (response.isSuccessful()) {
						deletedCount++;
					} else {
						logger.warn("Failed to delete role ID {}: {}", roleId, response.code());
					}
				}
			}
		}

		Map<String, Object> result = new HashMap<>();
		result.put("status", "success");
		result.put("deleted_count", deletedCount);
		return serializer.parse(serializer.serialize(result));
	}
}
