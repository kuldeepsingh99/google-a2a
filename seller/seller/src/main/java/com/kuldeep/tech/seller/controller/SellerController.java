package com.kuldeep.tech.seller.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import io.a2a.A2A;
import io.a2a.spec.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SellerController {

    private final ChatClient chatClient;

    public SellerController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/.well-known/agent.json")
    public AgentCard getAgentCard() {
        return new AgentCard.Builder()
                .name("seller-agent")
                .url("http://localhost:8082")
                .version("1.0.0")
                .description("Provides Seller Information")
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
                                .id("seller-by-id")
                                .name("Seller By Id")
                                .description("Returns Seller By Id")
                                .tags(List.of("seller", "seller id", "seller info", "seller information"))
                                .examples(List.of("Give me seller by id", "Give me seller information by id",
                                        "Give me seller info by id", "Get Seller Info By Id",
                                        "Get Seller Id", "Get Seller Information by Id"))
                                .inputModes(List.of("text"))
                                .outputModes(List.of("json", "text"))
                                .build(),
                        new AgentSkill.Builder()
                                .id("seller-by-name")
                                .name("Seller By name")
                                .description("Returns Seller By name")
                                .tags(List.of("seller", "seller name", "seller info", "seller information"))
                                .examples(List.of("Give me seller by name", "Give me seller information by name",
                                        "Give me seller info by name", "Get Seller Info By name",
                                        "Get Seller name", "Get Seller Information by name"))
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
