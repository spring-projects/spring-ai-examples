package com.example.tasks;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.modern.McpSchema.CallToolResult;
import io.modelcontextprotocol.modern.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.modern.McpSchema.ElicitFormRequest;
import io.modelcontextprotocol.modern.McpSchema.InputRequiredResult;
import io.modelcontextprotocol.modern.McpSchema.TextContent;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.server.McpSyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpSyncTool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tools defined in code rather than with annotations. A {@code List<McpSyncTool>} bean is the
 * same contract the annotation scanner uses, so these tools join the annotated ones in the
 * tool repository. This is where a {@code List<McpServerFeatures.SyncToolSpecification>} bean
 * of a session-based server moves to.
 * <p>
 * Both tools are named {@code admin_*}; {@link RoleFilteringRepositoryConfiguration} shows them
 * only to callers that send {@code X-Role: admin}.
 */
@Configuration(proxyBeanMethods = false)
public class AdminToolsConfiguration {

	/**
	 * {@code admin_purge_done} asks for confirmation, so the client must support form
	 * elicitation. This is the programmatic form of
	 * {@code @McpTool(requiredCapabilities = McpClientCapability.ELICITATION_FORM)}.
	 */
	private static final ClientCapabilities FORM_ELICITATION = ClientCapabilities.builder()
		.elicitation(new ClientCapabilities.Elicitation(new ClientCapabilities.Elicitation.Form(), null))
		.build();

	private static final Map<String, Object> NO_ARGUMENTS = Map.of("type", "object", "properties", Map.of());

	private static final Map<String, Object> CONFIRM_SCHEMA = Map.of("type", "object", "properties",
			Map.of("confirmed", Map.of("type", "boolean")), "required", List.of("confirmed"));

	@Bean
	List<McpSyncTool> adminTools(TaskService tasks) {
		return List.of(stats(tasks), purgeDone(tasks));
	}

	private static McpSyncTool stats(TaskService tasks) {
		Tool tool = Tool.builder("admin_stats", NO_ARGUMENTS).description("Counts open and completed tasks").build();
		return McpSyncTool.of(tool, (ctx, request) -> {
			long done = tasks.list().stream().filter(TaskService.Task::done).count();
			return McpSyncResponse.result(text("open=" + (tasks.list().size() - done) + ", done=" + done));
		});
	}

	/**
	 * A multi round-trip tool written against the SDK directly. The required capabilities are
	 * checked by the server before the handler runs: a client that does not declare form
	 * elicitation gets JSON-RPC error {@code -32021} instead of a question it cannot answer.
	 */
	private static McpSyncTool purgeDone(TaskService tasks) {
		Tool tool = Tool.builder("admin_purge_done", NO_ARGUMENTS)
			.description("Deletes all completed tasks after asking the user to confirm")
			.build();
		return McpSyncTool.of(tool, FORM_ELICITATION, (ctx, request) -> {
			Object answer = request.inputResponses() != null ? request.inputResponses().get("confirm") : null;
			if (answer == null) {
				// First round trip: ask, and seal the ids being confirmed into the request state,
				// so that a task completed in the meantime is not deleted without confirmation.
				List<String> done = tasks.list().stream().filter(TaskService.Task::done).map(TaskService.Task::id).toList();
				if (done.isEmpty()) {
					return McpSyncResponse.result(text("Nothing to purge"));
				}
				return McpSyncResponse.result(InputRequiredResult.builder()
					.elicit("confirm", ElicitFormRequest.builder("Delete completed tasks " + done + "?", CONFIRM_SCHEMA).build())
					.requestState(String.join(",", done))
					.build());
			}
			// Second round trip: the server has verified and unsealed the request state.
			if (!confirmed(answer)) {
				return McpSyncResponse.result(text("Kept all tasks"));
			}
			List<String> deleted = Arrays.stream(request.requestState().split(",")).filter(tasks::delete).toList();
			return McpSyncResponse.result(text("Deleted " + deleted));
		});
	}

	private static boolean confirmed(Object answer) {
		return answer instanceof Map<?, ?> response && "accept".equals(response.get("action"))
				&& response.get("content") instanceof Map<?, ?> content && Boolean.TRUE.equals(content.get("confirmed"));
	}

	private static CallToolResult text(String text) {
		return CallToolResult.builder().addContent(TextContent.builder(text).build()).build();
	}

}
