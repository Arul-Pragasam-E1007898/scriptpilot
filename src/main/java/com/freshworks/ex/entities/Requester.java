package com.freshworks.ex.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Requester {
    @JsonProperty("first_name")
    String firstName;

    @JsonProperty("last_name")
    String lastName;

    @JsonProperty("job_title")
    String jobTitle;

    @JsonProperty("primary_email")
    String primaryEmail;

    @JsonProperty("secondary_emails")
    List<String> secondaryEmails;

    @JsonProperty("work_phone_number")
    Long workPhoneNumber;

    @JsonProperty("mobile_phone_number")
    Long mobilePhoneNumber;

    @JsonProperty("department_ids")
    List<Long> departmentIds;

    @JsonProperty("can_see_all_tickets_from_associated_departments")
    Boolean canSeeAllTicketsFromAssociatedDepartments;

    @JsonProperty("reporting_manager_id")
    Long reportingManagerId;

    @JsonProperty("address")
    String address;

    @JsonProperty("time_zone")
    String timeZone;

    @JsonProperty("time_format")
    String timeFormat;

    @JsonProperty("language")
    String language;

    @JsonProperty("location_id")
    Long locationId;

    @JsonProperty("background_information")
    String backgroundInformation;

    @JsonProperty("custom_fields")
    Map<String, Object> customFields;

    /**
     * Creates a Requester with basic required information.
     * This factory method provides a convenient way to create a Requester with minimal required fields.
     *
     * @param firstName The first name of the requester
     * @param primaryEmail The primary email address
     * @param workPhoneNumber The work phone number
     * @param mobilePhoneNumber The mobile phone number
     * @return A new Requester instance with default values for optional fields
     */
    public static Requester of(String firstName, String primaryEmail, Long workPhoneNumber, Long mobilePhoneNumber) {
        return Requester.builder()
                .firstName(firstName)
                .primaryEmail(primaryEmail)
                .workPhoneNumber(workPhoneNumber)
                .mobilePhoneNumber(mobilePhoneNumber)
                .build();
    }

    /**
     * Creates a Requester with basic information including name and contact details.
     * This factory method is useful when you have both first and last names.
     *
     * @param firstName The first name of the requester
     * @param lastName The last name of the requester
     * @param primaryEmail The primary email address
     * @param workPhoneNumber The work phone number
     * @param mobilePhoneNumber The mobile phone number
     * @return A new Requester instance with default values for other optional fields
     */
    public static Requester withName(String firstName, String lastName, String primaryEmail, 
            Long workPhoneNumber, Long mobilePhoneNumber) {
        return Requester.builder()
                .firstName(firstName)
                .lastName(lastName)
                .primaryEmail(primaryEmail)
                .workPhoneNumber(workPhoneNumber)
                .mobilePhoneNumber(mobilePhoneNumber)
                .build();
    }

    /**
     * Creates a Requester with professional information.
     * This factory method is useful for creating a requester with job title and department information.
     *
     * @param firstName The first name of the requester
     * @param lastName The last name of the requester
     * @param jobTitle The job title
     * @param primaryEmail The primary email address
     * @param workPhoneNumber The work phone number
     * @param mobilePhoneNumber The mobile phone number
     * @param departmentIds List of department IDs the requester belongs to
     * @return A new Requester instance with professional information
     */
    public static Requester withProfessionalInfo(String firstName, String lastName, String jobTitle,
            String primaryEmail, Long workPhoneNumber, Long mobilePhoneNumber, List<Long> departmentIds) {
        return Requester.builder()
                .firstName(firstName)
                .lastName(lastName)
                .jobTitle(jobTitle)
                .primaryEmail(primaryEmail)
                .workPhoneNumber(workPhoneNumber)
                .mobilePhoneNumber(mobilePhoneNumber)
                .departmentIds(departmentIds)
                .build();
    }

    /**
     * Validates the time format if provided.
     * This method is called after object construction.
     */
    public void validateTimeFormat() {
        if (timeFormat != null && !timeFormat.equals("12h") && !timeFormat.equals("24h")) {
            throw new IllegalArgumentException("Time format must be either '12h' or '24h'");
        }
    }
} 