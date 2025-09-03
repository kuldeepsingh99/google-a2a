package com.kuldeep.tech.order;

import com.kuldeep.tech.order.tools.OrderAgentTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider tools(OrderAgentTools orderAgentTools) {
		return MethodToolCallbackProvider.builder().toolObjects(orderAgentTools).build();
	}
	@Bean
	public ChatClient orderAgent(ChatModel chatModel, ToolCallbackProvider toolProvider) {
		return ChatClient.builder(chatModel).defaultToolCallbacks(toolProvider.getToolCallbacks()).build();
	}
}
