package com.example.demo;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.util.JsonHelper;
import org.springframework.util.StringUtils;

public class MyLoggingAdvisor implements BaseAdvisor {

	private static final String ORANGE = "\033[38;5;208m";

	private static final String RESET = "\033[0m";

	private final int order;

	public final boolean showSystemMessage;

	public final boolean showAvailableTools;

	public final boolean showConversationHistory;

	public final String labelPrefix;

	private MyLoggingAdvisor(int order, boolean showSystemMessage, boolean showAvailableTools,
			boolean showConversationHistory, String labelPrefix) {
		this.order = order;
		this.showSystemMessage = showSystemMessage;
		this.showAvailableTools = showAvailableTools;
		this.showConversationHistory = showConversationHistory;
		this.labelPrefix = labelPrefix;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {

		StringBuilder sb = new StringBuilder("\n" + this.labelPrefix + "USER: ");

		if (this.showSystemMessage && chatClientRequest.prompt().getSystemMessage() != null
				&& StringUtils.hasText(chatClientRequest.prompt().getSystemMessage().getText())) {
			sb.append("\n - SYSTEM: " + first(chatClientRequest.prompt().getSystemMessage().getText(), 50));
		}

		if (this.showAvailableTools) {
			Object tools = "No Tools";

			if (chatClientRequest.prompt().getOptions() instanceof ToolCallingChatOptions toolOptions
					&& toolOptions.getToolCallbacks() != null) {
				tools = toolOptions.getToolCallbacks().stream().map(tc -> tc.getToolDefinition().name()).toList();
			}

			sb.append("\n - TOOLS: " + new JsonHelper().toJson(tools));
		}

		Message lastMessage = chatClientRequest.prompt().getLastUserOrToolResponseMessage();

		if (lastMessage.getMessageType() == MessageType.TOOL) {
			ToolResponseMessage toolResponseMessage = (ToolResponseMessage) lastMessage;
			for (var toolResponse : toolResponseMessage.getResponses()) {
				var tr = toolResponse.name() + ": " + first(toolResponse.responseData(), 300);
				sb.append("\n - TOOL-RESPONSE: " + tr);
			}
		}
		else if (lastMessage.getMessageType() == MessageType.USER) {
			if (StringUtils.hasText(lastMessage.getText())) {
				sb.append("\n - TEXT: " + first(lastMessage.getText(), 300));
			}
		}

		if (this.showConversationHistory) {
			sb.append("\n - [ALL INSTRUCTIONS]: " + chatClientRequest.prompt()
				.getInstructions()
				.stream()
				.map(m -> m.getMessageType() + ": " + messageContent(m))
				.toList());
		}

		System.out.println(ORANGE + sb.toString() + RESET);

		return chatClientRequest;
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		StringBuilder sb = new StringBuilder("\n" + this.labelPrefix + "ASSISTANT: ");

		if (chatClientResponse.chatResponse() == null || chatClientResponse.chatResponse().getResults() == null) {
			sb.append(" No chat response ");
			System.out.println(sb.toString());
			return chatClientResponse;
		}

		for (var generation : chatClientResponse.chatResponse().getResults()) {
			var message = generation.getOutput();
			if (message.getToolCalls() != null) {
				for (var toolCall : message.getToolCalls()) {
					sb.append("\n - TOOL-CALL: ")
						.append(toolCall.name())
						.append(" (")
						.append(toolCall.arguments())
						.append(")");
				}
			}

			if (message.getText() != null) {
				if (StringUtils.hasText(message.getText())) {
					sb.append("\n - TEXT: " + first(message.getText(), 200));
				}
			}
		}

		System.out.println(ORANGE + sb.toString() + RESET);

		return chatClientResponse;
	}

	private String first(String text, int n) {
		if (text.length() <= n) {
			return text;
		}
		return text.substring(0, n) + "...";
	}

	private String messageContent(Message message) {
		if (message instanceof ToolResponseMessage toolResponseMessage) {
			return toolResponseMessage.getResponses()
				.stream()
				.map(r -> first(r.name() + ": " + r.responseData(), 30))
				.reduce((a, b) -> a + ", " + b)
				.orElse("");
		}
		else if (message instanceof AssistantMessage assistantMessage) {
			if (StringUtils.hasText(assistantMessage.getText())) {
				return first(assistantMessage.getText(), 20);
			}
			return assistantMessage.getToolCalls() != null ? assistantMessage.getToolCalls()
				.stream()
				.map(tc -> first(tc.name() + ": " + tc.arguments(), 30))
				.reduce((a, b) -> a + ", " + b)
				.orElse("") : "";
		}
		else {
			if (message.getText() != null) {
				return first(message.getText(), 30);
			}
			return "";
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private int order = 0;

		private boolean showSystemMessage = true;

		private boolean showAvailableTools = false;

		private boolean showConversationHistory = false;

		private String labelPrefix = "";

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public Builder showSystemMessage(boolean showSystemMessage) {
			this.showSystemMessage = showSystemMessage;
			return this;
		}

		public Builder showAvailableTools(boolean showAvailableTools) {
			this.showAvailableTools = showAvailableTools;
			return this;
		}

		public Builder showConversationHistory(boolean showConversationHistory) {
			this.showConversationHistory = showConversationHistory;
			return this;
		}

		public Builder labelPrefix(String labelPrefix) {
			this.labelPrefix = labelPrefix;
			return this;
		}

		public MyLoggingAdvisor build() {
			MyLoggingAdvisor advisor = new MyLoggingAdvisor(this.order, this.showSystemMessage, this.showAvailableTools,
					this.showConversationHistory, this.labelPrefix);
			return advisor;
		}

	}

}
