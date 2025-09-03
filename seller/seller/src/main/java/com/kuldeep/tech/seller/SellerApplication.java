package com.kuldeep.tech.seller;

import com.kuldeep.tech.seller.tools.SellerAgentTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SellerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SellerApplication.class, args);
	}


	@Bean
	public ToolCallbackProvider tools(SellerAgentTools sellerAgentTools) {
		return MethodToolCallbackProvider.builder().toolObjects(sellerAgentTools).build();
	}
	@Bean public ChatClient orderAgent(ChatModel chatModel, ToolCallbackProvider toolProvider) {
		return ChatClient.builder(chatModel)
				.defaultToolCallbacks(toolProvider.getToolCallbacks())
				.build();
	}
}
