# Java Implementation of Google's A2A Protocol (A2A-Compliant)

This project demonstrates how to build an A2A-compliant multi-agent system using Spring Boot, Ollama, and the Agent-to-Agent (A2A) protocol.

The architecture is based on a Parent Agent that orchestrates multiple Child Agents (e.g., Order Agent, Seller Agent). The parent dynamically discovers child agents via their AgentCard (agent.json) and delegates tasks in a standardized, message-driven way.

## Key Features

**A2A-Compliant Communication** – Agents communicate through the A2A protocol using standardized Message objects and AgentCard descriptors.

**Dynamic Agent Discover**y – The parent automatically fetches AgentCards from configured child agents, ensuring no hardcoding of capabilities.

**LLM-Driven Task Routing** – The parent uses a Large Language Model (via Ollama) to decide which agent(s) should handle a given user query.

**Chained Orchestration** – The parent can call multiple agents in sequence, e.g., fetching order details from the Order Agent and then using the seller ID to fetch additional info from the Seller Agent.

**Response Aggregation** – The parent aggregates responses from multiple agents into a final, natural-language answer for the user.

**Strict Agent Boundaries** – Each agent is independent and exposes its own agent.json, making the system modular and extendable.

## Use Cases

**Case 1**: Get B2C Orders → Only the Order Agent is called.

**Case 2**: Get Seller Info by ID → Only the Seller Agent is called.

**Case 3**: Get B2C Orders with Seller Info → The Order Agent is called first, then the Seller Agent is called using the extracted seller ID(s).

## Flow Diagram

<img width="1100" height="688" alt="MCP SERVER" src="https://github.com/user-attachments/assets/5d9587bc-1b59-46f6-94a4-cc5655eb101f" />

***

<img width="942" height="1144" alt="a2a drawio" src="https://github.com/user-attachments/assets/9074448a-2991-4a02-82d3-aef06f25d257" />

## Testing with Prompts

<img width="1360" height="741" alt="Screenshot 2025-09-03 225131" src="https://github.com/user-attachments/assets/27ed83ae-04eb-4140-ab16-b4859711da63" />

----

<img width="1360" height="614" alt="Screenshot 2025-09-03 224131" src="https://github.com/user-attachments/assets/3c0be40e-8a66-4c15-aa2b-5d6e72ea721f" />

---

<img width="1368" height="879" alt="Screenshot 2025-09-03 221828" src="https://github.com/user-attachments/assets/4e6e96f1-be99-4f25-ac99-cdca8f2f32fd" />



