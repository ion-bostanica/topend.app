# ADR-0001: Spring AI starters over raw provider SDKs

Date: 2026-08-19 · Status: Accepted

## Context
The project goal requires every AI feature to work with both Claude and OpenAI. Article #1 was
originally framed around the raw SDKs (anthropic-sdk-java, openai-java), but the code that shipped
uses Spring AI 2.0 starters (`spring-ai-starter-model-anthropic`, `spring-ai-starter-model-openai`).

## Decision
Standardize on Spring AI. One `@Tool`/`ChatClient` programming model covers both providers;
provider selection stays a config switch (`spring.ai.default`). Provider-specific behavior remains
isolated in per-provider adapters (hexagonal out-adapters), preserving the "no shared
provider-specific logic" rule at the adapter level.

## Consequences
- Tool/function calling, multimodal input, and structured output are written once, run on both providers.
- We depend on Spring AI's abstraction keeping pace with provider features; escape hatch is
  provider-specific options classes (e.g. `AnthropicChatOptions`) inside the respective adapter.
