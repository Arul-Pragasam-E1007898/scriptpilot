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

	private static final String DEPT_FIELDS = """
				You are an assistant that helps manage department field configurations. When the `createDepartmentFields` tool is invoked, it does not require any input arguments.
				- If the user provides no input or gives an ambiguous prompt, still proceed to call `createDepartmentFields()` to generate default department fields. Do not ask for additional input unless explicitly required. Always assume default values can be used to create department fields unless the user specifies otherwise.
			""";

	private static List<Testcase> testcases = Arrays.asList(
//            newCase(1, """
//                    Append timestamp with the given email address test@yopmail.com, 
//                    create a contact with the mail created in first step email, and then retrieve the newly created contact.
//                    """, Category.Requester),
//            newCase(2, """
//                    Append timestamp with the given email address test@yopmail.com, 
//                    create a contact with that email, and then delete the newly created contact.
//                    """, Category.Requester),
//            newCase(3, """
//                    Append timestamp with the given email address test@yopmail.com, 
//                    create a contact with that email, and then forget the newly created contact.
//                    """, Category.Requester)
			newCase(1,
					"""
					Create a department with only the mandatory field 'name' set to a random timestamped name like 'Dept_20250616_1701'. Do not include any other optional fields.
					""",
					Category.Requester, DEPT_SYSTEM_MESSAGE),
			newCase(2, """
					Create a department with name, description, domains, head_user_id, and prime_user_id.
					Get requester IDs from API to use for head and prime users.
					Do not include custom_fields.
					""", Category.Requester, DEPT_SYSTEM_MESSAGE_EXTENDED), 
			newCase(3, """
					List all the workspaces.
					""", Category.Requester),
			newCase(4,
					"""
							Create a new requester with random with a random first_name with current timestamp, ex: 'fn_20250616_1701', last_name and primary_email,
							Update the name of the newly created requester by appending "_updated".
							Fetch the details of the updated requester using a GET call.
							Convert the requester to an agent.
							Delete the converted requester.
							List all requesters again after deletion.
							Finally, return the result or status of each step in a structured JSON format.
							""",
					Category.Requester),
			newCase(5, """
					Create department fields.
					""", Category.Requester, DEPT_FIELDS)
	);

	public static List<Testcase> load() {
		return testcases;
	}
}
