package com.freshworks.ex.scenarios;

import static com.freshworks.ex.scenarios.Testcase.newCase;

import java.util.Arrays;
import java.util.List;

public class TestcaseRepository {

	private static final String DEPT_SYSTEM_MESSAGE = """
			    You are a helpful Freshservice assistant capable of orchestrating sequences of API actions using a configured toolbox.

			    - When creating a resource (e.g., department, contact, requester), include only mandatory fields unless optional fields are explicitly provided by the user.
			    - For example, to create a department, the only mandatory field is 'name'. All others like 'description', 'domains', or 'head_user_id' must be omitted unless the user explicitly mentions them.
			    - If the user says to "skip optional fields", strictly include ONLY the mandatory field(s) in the request payload.
			    - When generating a name (e.g., for a department), use the current time for ensure uniqueness.
			    - Even if optional fields are shown in examples, do not include them unless the user has explicitly asked for them.
			    - Always return the exact JSON payload that was sent to the API for transparency.
			    - Execute multi-step tasks sequentially and summarize the result of each step with a clear Pass/Fail status.
			""";

	private static final String DEPT_SYSTEM_MESSAGE_EXTENDED = """
			You are a helpful Freshservice assistant capable of creating and managing departments using the Freshservice API.

			- When creating a department, always include only the fields that are explicitly provided or required.
			- The only mandatory field is `name`. If not provided, generate a name in the format: Dept_YYYYMMDD_HHmmss.
			- For `head_user_id` and `prime_user_id`, you must:
			  - First call the requester list/search API to retrieve valid requester IDs.
			  - Use any two distinct requester IDs as head and prime users.
			- For `domains`, generate realistic domains such as "ops.example.org" or "cloud.dept.gov".
			- For `description`, auto-generate something meaningful (e.g., “Handles all finance operations”).
			- Do not include `custom_fields` unless explicitly instructed.
			- Always log and return:
			  - The final JSON payload used in the request
			  - The sequence of steps taken (e.g., fetching requesters)
			  - The final API response.
			""";

	private static List<Testcase> testcases = Arrays.asList(
            newCase(1, """
                    Append timestamp with the given email address test@yopmail.com, 
                    create a contact with the mail created in first step email, and then retrieve the newly created contact.
                    """, Category.Requester, false),
            newCase(2, """
                    Append timestamp with the given email address test@yopmail.com, 
                    create a contact with that email, and then delete the newly created contact.
                    """, Category.Requester, false),
            newCase(3, """
                    Append timestamp with the given email address test@yopmail.com, 
                    create a contact with that email, and then forget the newly created contact.
                    """, Category.Requester, false),
			newCase(4,
					"""
					Create a department with only the mandatory field 'name' set to a random timestamped name like 'Dept_20250616_1701'. Do not include any other optional fields.
					""",
					Category.Requester, DEPT_SYSTEM_MESSAGE, false),
			newCase(5, """
					Create a department with name, description, domains, head_user_id, and prime_user_id.
					Get requester IDs from API to use for head and prime users.
					Do not include custom_fields.
					""", Category.Requester, DEPT_SYSTEM_MESSAGE_EXTENDED, false), 
			newCase(6,
					"""
					### ✅ Positive Test Cases
					
					1. Create a new requester with a random email and name.
					   - Assert that the response contains a valid requester ID.

					2. Fetch the newly created requester by ID from step 1.
					   - Assert that the ID and names match what was created.

					3. Update the requester by appending "_updated" to the first_name.
					   - Assert that the update was successful by fetching and verifying the updated name.

					4. Convert the requester to an agent.
					   - Assert that the conversion response is successful.

					5. Delete the converted requester.
					   - Assert that the delete status is "success".

					6. List all requesters.
					   - Assert that the deleted requester is no longer present.

					7. Create two new requesters for merge testing.
					   - Merge one into the other using the `mergeRequesters` API.
					   - Assert that the response confirms the merge.

					8. List requester fields using `listRequesterFields`.
					   - Assert that the response includes common fields like `first_name`, `email`, etc.
					""",
					Category.Requester, false),
			newCase(7,
					"""
					You are testing a Java-based LangChain agent with tools for managing Freshservice requesters (contacts). Use the following sequence to validate the behavior of the tools from `RequesterProxy`. Ensure every step includes:
					- Step name
					- Input details
					- Output/result
					- Whether the step passed or failed
					- Any assertion or error message if applicable

					---

					1. **Create a requester with valid email**
					   - Verify that a valid requester ID is returned and the email appears correctly in the result

					2. **View the created requester**
					   - Fetch the requester using the ID from step 1
					   - Check that the name and email match the ones used during creation

					3. **List all requesters**
					   - Confirm that the newly created requester appears in the list

					4. **Update the requester**
					   - Change the first name by appending "_updated"
					   - Verify that the name has changed correctly by retrieving the requester again

					5. **Deactivate the requester**
					   - Deactivate the requester and verify that it is no longer marked as active

					6. **Reactivate the requester**
					   - Reactivate the requester and verify that it is marked as active

					7. **Convert the requester to an agent**
					   - Perform the conversion
					   - Verify that the system acknowledges the requester as an agent or reports a successful conversion

					8. **Convert the agent to an requester**
					   - Perform the conversion
					   - Verify that the system acknowledges the requester as an requester or reports a successful conversion

					9. **Check assignment history**
					   - View the asset assignment history for this requester
					   - Confirm that the response includes an assignment history list

					10. **Create a second requester with random name and random email**
					    - Use a different random name and email
					    - Ensure this second requester is created successfully

					11. **Merge the second requester into the first**
					    - Perform the merge and verify that the system returns a success
					    - Check that the merged requester is no longer retrievable

					12. **Forget all created requesters regardless of any prior failures**
					    - Request that the system forgets this requester
					    - Confirm that the system responds with a successful data deletion/forget confirmation

					"Finally, forget the requester or agent using the created ID if any failures occurred in the previous steps; otherwise, skip this step."

					---

					### ✅ Output Format

					For each step, return a structured result in JSON:

					```json
					{
					  "step": "Create a new requester",
					  "input": {
					    "first_name": "John",
					    "email": "john.doe@example.com"
					  },
					  "output": {
					    "id": 12345,
					    "status": "created"
					  },
					  "assertion": "passed"
					}
					""",
					Category.Requester, true),
			newCase(8,
					"""
					You are testing a Java-based LangChain agent with tools for managing Freshservice requesters (contacts). Use the following sequence to validate the behavior of the tools from `RequesterProxy`. Ensure every step includes:
						- Step name
						- Input details
						- Output/result
						- Whether the step passed or failed
						- Any assertion or error message if applicable

					---
					### ❌ Negative Test Cases

					1. Try fetching a requester with a non-existent ID (e.g., -999 or 999999999).
					   - Assert that the API returns an error or a "not found" status.

					2. Try updating a requester with invalid data (e.g., missing `first_name`).
					   - Assert that the update fails and an appropriate error is returned.

					3. Try creating a requester with an invalid email and invalid name.
					   - Assert that the API rejects the request.

					4. Try forgetting a requester with an invalid ID.
					   - Assert that the deletion fails and provides an appropriate message.

					5. Try merging requesters with invalid or non-existent IDs.
					   - Assert that the API returns an error message about invalid requester IDs.

					6. Create a requester without valid email.
					   - Assert that the API rejects it with a validation error.

					7. Create a requester with an invalid email format ex: test@xyz.
					   - Assert rejection due to invalid format.

					8. Try updating a requester with an empty string as first_name.
					   - Assert that the update fails.

					9. Try fetching a requester using a string ID (e.g., "abc").
					   - Assert that the API returns a type/format error.

					10. Merge requesters with invalid IDs (non-existent or malformed).
					    - Assert proper error handling for both primary and secondary IDs.

					11. Convert a non-existent requester to an agent.
					    - Assert that the API returns "not found".

					12. Try creating a requester using a duplicate email (if uniqueness is enforced).
					    - Assert that the second attempt fails.

					13. Perform a delete on a malformed ID (e.g., null or "").
					    - Assert that the API returns an appropriate error message.
					""",
					Category.Requester, true),
			newCase(9,
					"""
					You are testing a Java-based LangChain agent with tools for managing Freshservice requesters (contacts). Use the following sequence to validate the behavior of the tools from `RequesterProxy`. Ensure every step includes:
						- Step name
						- Input details
						- Output/result
						- Whether the step passed or failed
						- Any assertion or error message if applicable

					---
					### 🔄 Consistency and Data Integrity Test Cases

					1. List requesters before and after creating one with valid email.
					   - Assert that the count increases by 1 by calculating for all the requester.

					2. Convert the created requester to agent, then delete them.
					   - Assert both actions complete successfully and data is cleaned up.
					
					""",
					Category.Requester, true),
			newCase(91,
					"""
					Create an agent with valid email address.
					
					""",
					Category.Requester, false),
			newCase(10,
					"""
					You are testing a Java-based LangChain, use the following sequence to validate the behavior of the tools from `AgentProxy`
					
					1. Create an agent with valid email address (use unique 10 chars in emailname to keep it unique).
					   - Verify that a valid agent ID is returned and email matches the input.
					
					2. Get the created agent by ID.
					   - Verify the returned agent details match what was created.
					
					3. List agents using a filter with a query string formatted like "email:'testagent_yfbnxzqytr@test.com'"
					    - Example query: email:'testagent_yfbnxzqytr@test.com'
						- Ensure the created agent appears in the filtered results.
					
					4. Deactivate the agent by ID.
					   - Confirm the agent is marked as inactive.
					
					5. Reactivate the same agent.
					   - Confirm the agent is active again.
					
					6. Convert the agent to a requester.
					   - Verify the conversion is successful.
					
					7. Convert the requester back to an agent.
					   - Verify the conversion is successful.
					
					8. Forget the agent (delete personal data).
					   - Confirm the system responds with successful data deletion.
					
					---
					
					""",
					Category.Requester, false),
			newCase(11,
					"""
					You are testing a Java-based LangChain, use the following sequence to validate the behavior of the tools from `AgentProxy`. Use the following sequence to validate error handling with invalid inputs and invalid operations. For each step, provide:
					
					1. Attempt to create an agent without an email.
					   - Verify the request is rejected with a validation error.
					
					2. Attempt to create an agent with invalid email format (e.g. "invalid-email").
					   - Verify rejection with an appropriate error message.
					
					3. Fetch an agent with a non-existent ID (e.g. 999999999).
					   - Confirm the system returns a "not found" error.
					
					4. Update an agent with empty or invalid fields.
					   - Confirm update fails with an error.
					
					5. Deactivate an agent with a non-existent ID.
					   - Confirm proper error response.
					
					6. Reactivate an agent with invalid or already active ID.
					   - Confirm correct error handling or idempotent success.
					
					7. Convert a non-existent agent to requester.
					   - Confirm "not found" error.
					
					8. Forget an agent with invalid or malformed ID (e.g. null, negative).
					   - Confirm proper validation error.
					
					""",
					Category.Requester, false),
			newCase(11,
					"""
					You are testing a Java-based LangChain, use the following sequence to validate the behavior of the tools from `AgentProxy`. Use the following sequence to validate error handling with invalid inputs and invalid operations. For each step, provide:

					---
					
					1. List all agents before creation and record the count.
					
					2. Create a new agent with valid details.
					   - Confirm creation success and capture agent ID.
					
					3. List agents again and verify count increased by 1.
					
					4. Deactivate the newly created agent.
					   - Confirm agent is marked inactive.
					
					5. Reactivate the agent.
					   - Confirm agent is active again.
					
					6. Forget the agent.
					   - Confirm data is removed or agent is no longer retrievable.
					
					7. List agents again and verify count decreased by 1 or agent is absent.
					
					---

					""",
					Category.Requester, false)
			);

	public static List<Testcase> load() {
		return testcases;
	}
}
