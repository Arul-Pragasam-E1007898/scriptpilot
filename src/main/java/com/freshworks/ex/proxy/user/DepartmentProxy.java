package com.freshworks.ex.proxy.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.ex.proxy.AbstractProxy;
import dev.langchain4j.agent.tool.Tool;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepartmentProxy extends AbstractProxy {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentProxy.class);
    private static final String DEPARTMENTS = "/departments";

    public DepartmentProxy(String domain) {
        super(domain);
        logger.debug("Initialized DepartmentProxy with domain: {}", domain);
    }

    @Tool(name = "createDepartment")
    public JsonNode createDepartment(String name,
                                     String description,
                                     Long headUserId,
                                     Long primeUserId,
                                     List<String> domains,
                                     Map<String, String> customFields) throws IOException {

        logger.info("Creating department with name: {}", name);
        Map<String, Object> department = new HashMap<>();

        // Mandatory
        department.put("name", name);

        // Optional - add only if non-null
        if (description != null) department.put("description", description);
        if (headUserId != null && headUserId != 0) department.put("head_user_id", headUserId);
        if (primeUserId != null && primeUserId != 0) department.put("prime_user_id", primeUserId);
        if (domains != null && !domains.isEmpty()) department.put("domains", domains);
        if (customFields != null && !customFields.isEmpty()) department.put("custom_fields", customFields);

        Map<String, Object> payload = Map.of("department", department);
        logger.info("Department creation payload: {}", serializer.serialize(payload));

        Response response = restClient.post(DEPARTMENTS, serializer.serialize(payload));
        if (!response.isSuccessful()) {
            logger.error("Failed to create department. Status: {}", response.code());
            throw new IOException("Department creation failed: " + response.code());
        }

        return parse(response);
    }


    /**
     * Update an existing department.
     *
     * @param departmentId ID of the department to update
     * @param name         New name for the department
     * @return JSON representation of the updated department
     * @throws IOException if API call fails
     */
    @Tool(name = "updateDepartment")
    public JsonNode updateDepartment(Long departmentId, String name) throws IOException {
        logger.info("Updating department with ID: {}", departmentId);
        Map<String, Object> department = new HashMap<>();
        department.put("name", name);

        String jsonBody = serializer.serialize(department);
        logger.debug("Sending update department request: {}", jsonBody);

        Response response = restClient.put(DEPARTMENTS + '/' + departmentId, jsonBody);
        if (!response.isSuccessful()) {
            logger.error("Failed to update department. Status: {}", response.code());
            throw new IOException("Failed to update department: " + response.code());
        }
        return parse(response);
    }

    /**
     * Get a department by ID.
     *
     * @param departmentId ID of the department
     * @return JSON representation of the department
     * @throws IOException if API call fails
     */
    @Tool(name = "getDepartment")
    public JsonNode getDepartment(Long departmentId) throws IOException {
        logger.info("Fetching department with ID: {}", departmentId);
        Response response = restClient.get(DEPARTMENTS + '/' + departmentId);
        if (!response.isSuccessful()) {
            logger.error("Failed to get department. Status: {}", response.code());
            throw new IOException("Failed to get department: " + response.code());
        }
        return parse(response);
    }

    /**
     * Delete a department by ID.
     *
     * @param departmentId ID of the department
     * @return JSON indicating deletion result
     * @throws IOException if API call fails
     */
    @Tool(name = "deleteDepartment")
    public JsonNode deleteDepartment(Long departmentId) throws IOException {
        logger.info("Deleting department with ID: {}", departmentId);
        Response response = restClient.delete(DEPARTMENTS + '/' + departmentId);
        if (!response.isSuccessful()) {
            logger.error("Failed to delete department. Status: {}", response.code());
            throw new IOException("Failed to delete department: " + response.code());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Department deleted successfully");
        result.put("department_id", departmentId);
        return serializer.parse(serializer.serialize(result));
    }

    /**
     * List all departments.
     *
     * @return JSON list of departments
     * @throws IOException if API call fails
     */
    @Tool(name = "listDepartments")
    public JsonNode listDepartments() throws IOException {
        logger.info("Fetching list of all departments");
        Response response = restClient.get(DEPARTMENTS);
        if (!response.isSuccessful()) {
            logger.error("Failed to list departments. Status: {}", response.code());
            throw new IOException("Failed to list departments: " + response.code());
        }
        return parse(response);
    }
}