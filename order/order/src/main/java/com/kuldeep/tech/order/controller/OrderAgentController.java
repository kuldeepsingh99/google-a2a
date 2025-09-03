package com.kuldeep.tech.order.controller;


import io.a2a.A2A;
import io.a2a.spec.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrderAgentController {

    private final ChatClient chatClient;

    public OrderAgentController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/.well-known/agent.json")
    public AgentCard getAgentCard() {
        return new AgentCard.Builder()
                .name("order-agent")
                .url("http://localhost:8081")
                .version("1.0.0")
                .description("Provides B2B and B2C order information")
                .capabilities(
                        new AgentCapabilities.Builder().streaming(false).build()
                )
                .defaultInputModes(
                        List.of("text")
                )
                .defaultOutputModes(
                        List.of("json", "text")
                )
                .protocolVersion("1.0")
                .skills(List.of(
                        new AgentSkill.Builder()
                                .id("b2b-orders")
                                .name("B2B Orders")
                                .description("fetches all B2B orders")
                                .tags(List.of("b2b", "orders", "list"))
                                .examples(List.of("Show me all B2B orders", "List B2B orders", "Get B2B orders", "Fetch B2B orders", "Display B2B orders"))
                                .inputModes(List.of("text"))
                                .outputModes(List.of("json", "text"))
                                .build()

                ))
                .build();
    }

    @PostMapping("/task")
    public Message handleTask(@RequestBody Message message) {
        // Extract plain text task from the incoming A2A message
        String task = extractText(message);

        // Let Ollama + Spring AI decide which @Tool to call
        String result = chatClient.prompt()
                .user(task)
                .call()
                .content();

        // Wrap result into an A2A-compliant agent message
        return A2A.toAgentMessage(result);
    }

    private String extractText(Message message) {
        if (message.getParts() == null) return "";
        return message.getParts().stream()
                .filter(p -> p instanceof TextPart)
                .map(p -> ((TextPart) p).getText())
                .collect(Collectors.joining("\n"));
    }

}