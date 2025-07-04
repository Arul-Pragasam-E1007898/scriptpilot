# Script Pilot

ScriptPilot aims to leverage Large Language Models (LLMs) for:

- Parsing test cases written in natural language.

- Executing those parsed test cases.

## Features

- Testers become capability creators, not flow designers.


- Test executions become AI-managed services.


- Codebases shrink: orchestration logic becomes prompt-based or AI-generated.


- Not required to write test scripts and make version control. Code driven orchestration is obsolete.


## Prerequisites

- Java 17 or higher
- Maven
- OpenAI API key

## Setup

1. Clone this repository
2. Set your OpenAI API key as an environment variable:
   ```bash
   export OPENAI_API_KEY=your-api-key-here
   ```

## Building the Project

```bash
mvn clean install
```

## Running the Demo

```bash
mvn exec:java -Dexec.mainClass="com.freshworks.ex.ScriptPilot"
```

## Project Structure

```
src/main/java/com/freshworks/ex/
├── entities/         # Custom entity classes
├── proxy/           # Proxy pattern implementations
├── scenarios/       # Example scenarios and use cases
├── utils/           # Utility functions and helpers
└── ScriptPilot.java # Main script execution class
```

## Key Components

1. **Entity Classes**: Custom data structures for the application

2. **Proxy Pattern**: Implementation of the proxy design pattern

3. **Scenarios**: Various example use cases and implementations

4. **Utilities**: Helper functions and common operations

## Dependencies

The project uses the following key dependencies:
- LangChain4j Core (v0.24.0)
- LangChain4j OpenAI Integration
- Lombok
- SLF4J for logging
- OkHttp for HTTP requests
- Jackson for JSON processing

## How it Works

The demo shows how to:
1. Create and register custom functions with LangChain4j
2. Use the functions in conversations with the AI model
3. Handle different types of responses and entities
4. Implement proxy patterns for enhanced functionality
5. Execute various scenarios with the AI model

The AI model will automatically call the appropriate functions when needed.

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is licensed under the MIT License - see the LICENSE file for details. 

## Screenshot
![image](https://github.com/user-attachments/assets/c7eb8379-d61c-412e-8cac-af7fdd047a36)

## Execution Log
```
================================================================================
TEST CASE EXECUTION LOG
================================================================================
Test Case: TC-61757131
Test ID: 1122375
Execution Time: 2025-07-03 12:47:58
Duration: 11 seconds
================================================================================
TEST STEPS:

Create a requester with a valid email address.

Verify that the returned requester has a valid (non-null) requester ID.
Confirm that the email in the returned requester matches the email used for creation.



Delete the requester created in the previous step.
Verify that the deletion is successful.

================================================================================
EXECUTION RESULTS:

TESTCASE_STATUS: FAILED
EXECUTION_SUMMARY: The test case failed at the very first step due to an error in creating the requester, making it impossible to proceed with deletion.

STEPS_EXECUTED:
Step 1: Create a requester with a valid email address. - STATUS: FAILED - RESULT: Error: createRequesterOrContact failed (code 503). Retried once, but the error persisted.
Step 1a: Verify that the returned requester has a valid (non-null) requester ID. - STATUS: FAILED - RESULT: Not executed due to requester creation failure.
Step 1b: Confirm that the email in the returned requester matches the email used for creation. - STATUS: FAILED - RESULT: Not executed due to requester creation failure.
Step 2: Delete the requester created in the previous step. - STATUS: FAILED - RESULT: Not executed because requester was not created.

GENERATED_DATA: 
- Email: testuser_20250703_124750@yopmail.com

FINAL_RESULT: The test case could not be completed because the requester creation API failed with a 503 error, even after retrying. As a result, no requester was created, and subsequent verification and deletion steps could not be performed. Please investigate the requester creation service for availability or configuration issues before re-running this test.
================================================================================

```
