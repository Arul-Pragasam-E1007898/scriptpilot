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