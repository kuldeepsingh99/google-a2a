package com.kuldeep.tech.parent.controller;

import com.kuldeep.tech.parent.config.AgentProperties;

import io.a2a.A2A;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.Message;
import io.a2a.spec.TextPart;
import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.model.ChatModel;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parent")
public class ParentController {

    private final AgentProperties agentProperties;
    private final WebClient webClient;
    private final ChatModel chatModel;




    @PostMapping("/task")
    public String handleTask(@RequestBody String userQuery) {
        // 1️⃣ Fetch all AgentCards
        List<AgentCard> agentCards = agentProperties.getAgents().stream()
                .map(cfg -> {
                    try {
                        return A2A.getAgentCard(cfg.getUrl());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to fetch AgentCard from " + cfg.getUrl(), e);
                    }
                })
                .toList();

        // 2️⃣ Ask LLM which agent(s) to use
        List<String> chosenAgentIds = chooseAgentsViaLLM(userQuery, agentCards);

        if (chosenAgentIds.isEmpty()) {
            return "I'm sorry, I can only assist with order and seller related queries.";
        }

        List<String> responses = new ArrayList<>();

        Set<String> alreadyCalledAgents = new HashSet<>();

        for (String agentId : chosenAgentIds) {
            if (alreadyCalledAgents.contains(agentId)) continue;
            alreadyCalledAgents.add(agentId);

            AgentCard card = agentCards.stream()
                    .filter(c -> c.name().equals(agentId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Agent not found: " + agentId));

            Message response = webClient.post()
                    .uri(card.url() + "/task")
                    .bodyValue(A2A.toUserMessage(userQuery, UUID.randomUUID().toString()))
                    .retrieve()
                    .bodyToMono(Message.class)
                    .block();

            if (response != null && response.getParts() != null && !response.getParts().isEmpty()) {
                var part = response.getParts().get(0);
                if (part instanceof TextPart textPart) {
                    String text = textPart.getText();
                    responses.add(text);

                    boolean sellerFlag = chosenAgentIds.stream()
                            .anyMatch(id -> id.equals("seller-agent"));

                    if (agentId.equals("order-agent") && !alreadyCalledAgents.contains("seller-agent")
                            && sellerFlag) {
                        List<String> sellerIds = extractSellerIds(text);
                        for (String sellerId : sellerIds) {
                            String sellerResponse = callSellerAgent(sellerId, agentCards, userQuery);
                            responses.add(sellerResponse);
                            alreadyCalledAgents.add("seller-agent");
                        }
                    }
                }
            }
        }


        // 4️⃣ Aggregate with LLM
        String aggregationPrompt = """
        You are the Parent Agent.
        Aggregate the following responses into a clear, concise final answer.

        User query:
        %s

        Child agent responses:
        %s

        Provide the final answer in natural language, without mentioning agents.
        """.formatted(userQuery, String.join("\n", responses));

        return chatModel.call(aggregationPrompt);
    }


    private List<String> chooseAgentsViaLLM(String userQuery, List<AgentCard> agentCards) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a task router.\n\n");
        prompt.append("User query: ").append(userQuery).append("\n\n");
        prompt.append("Available agents:\n");
        for (AgentCard card : agentCards) {
            prompt.append(", Name: ").append(card.name())
                    .append(", Skills: ").append(
                            card.skills().stream()
                                    .map(AgentSkill::name)
                                    .collect(Collectors.joining(", "))
                    ).append("\n");
        }

        prompt.append("""
    
                Routing rules:
                 1. If the query asks about orders (order details, order id, B2B, B2C, order amount, etc.), select ONLY order-agent.
                 2. If the query asks about sellers (seller information, seller id, seller rating, seller name, etc.), select ONLY seller-agent.
                 3. IMPORTANT:\s
                    - "order id" ALWAYS means the ID of an ORDER → use order-agent.
                    - "seller id" ALWAYS means the ID of a SELLER → use seller-agent.
                    - If the query only says "id" but also mentions "seller", treat it as seller id → use seller-agent.
                 4. If the query explicitly requires BOTH orders AND sellers, then return both agent IDs.
                 5. Never include an agent ID unless it is strictly required.
                 6. If the query is unrelated to orders or sellers, return an empty response.
                 7. ALWAYS return only the exact agent IDs (order-agent or seller-agent).
                 8. NEVER include explanations or extra text.
                 9. If no agent is relevant, return an empty response.
                 10. Return ONLY the agent names, separated by commas if multiple agents are found.
                 11. Do NOT return any labels or explanations.
                 12. Return order-agent if query is only related to order, in that case don't return seller
                 13. If the Query is related to both order and seller information, then returm both
                 14. If the Query is only related to seller then return seller agent, don't return order agent
            """);

        String llmResponse = chatModel.call(prompt.toString());
        if (llmResponse == null || llmResponse.isBlank()) return Collections.emptyList();

        return Arrays.stream(llmResponse.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }


    private List<String> extractSellerIds(String orderResponse) {
        List<String> sellerIds = new ArrayList<>();

        Matcher matcher = Pattern.compile("Seller ID:\\s*(\\d+)").matcher(orderResponse);
        while (matcher.find()) {
            sellerIds.add(matcher.group(1));
        }

        return sellerIds.stream().distinct().toList();
    }


    private String callSellerAgent(String sellerId, List<AgentCard> agentCards, String originalUserQuery) {
        AgentCard sellerCard = agentCards.stream()
                .filter(c -> c.name().equals("seller-agent"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Seller agent not found"));

        String sellerTask = "Returns seller information by seller ID " + sellerId;

        Message response = webClient.post()
                .uri(sellerCard.url() + "/task")
                .bodyValue(A2A.toUserMessage(sellerTask, UUID.randomUUID().toString()))
                .retrieve()
                .bodyToMono(Message.class)
                .block();

        if (response != null && response.getParts() != null && !response.getParts().isEmpty()) {
            var part = response.getParts().get(0);
            if (part instanceof TextPart textPart) {
                return textPart.getText();
            }
        }
        return "";
    }
}