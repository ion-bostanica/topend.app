# ADR-0002: In-process tool calling over an MCP server

Date: 2026-08-19 · Status: Accepted

## Context
The food-search capability (USDA FoodData Central) must be callable by the extraction agent.
Options: expose it as an MCP server (Spring AI MCP server starter) or register it as an
in-process tool definition on the chat call.

## Decision
In-process `@Tool` definitions. The only consumer today is our own agent running in the same JVM;
an MCP server adds a network protocol, a transport, and deployment surface for zero current benefit.

## Consequences
- Simplest possible wiring; Spring AI translates the same `@Tool` into Anthropic `tools` and
  OpenAI function definitions.
- If an external client (e.g. Claude Desktop) ever needs food search, wrap the same tool bean
  with `spring-ai-starter-mcp-server-webflux` — the tool code does not change.
