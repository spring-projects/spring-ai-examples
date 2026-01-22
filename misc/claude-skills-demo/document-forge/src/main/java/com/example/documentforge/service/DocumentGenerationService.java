package com.example.documentforge.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.AnthropicSkillsResponseHelper;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import com.example.documentforge.model.DocumentType;
import com.example.documentforge.model.GenerationRequest;
import com.example.documentforge.model.GenerationResult;
import com.example.documentforge.model.GenerationResult.GeneratedFile;

/**
 * Service for generating documents using Claude Skills.
 */
@Service
public class DocumentGenerationService {

	private static final Logger logger = LoggerFactory.getLogger(DocumentGenerationService.class);

	private final AnthropicChatModel chatModel;
	private final AnthropicApi anthropicApi;
	private final String customSkillId;

	// In-memory storage for generation history (session-based in real app)
	private final Map<String, GenerationResult> history = new ConcurrentHashMap<>();

	private final Executor asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();

	public DocumentGenerationService(
			AnthropicChatModel chatModel,
			AnthropicApi anthropicApi,
			@org.springframework.beans.factory.annotation.Value("${document-forge.custom-skill-id:}") String customSkillId) {
		this.chatModel = chatModel;
		this.anthropicApi = anthropicApi;
		this.customSkillId = customSkillId;
	}

	/**
	 * Check if a document type should use async generation by default.
	 */
	public boolean shouldUseAsync(DocumentType type) {
		return type == DocumentType.PPTX;
	}

	/**
	 * Generate a document using Claude Skills.
	 */
	public GenerationResult generate(GenerationRequest request) {
		String id = UUID.randomUUID().toString();
		logger.info("Generating {} document with id: {}", request.documentType(), id);

		try {
			// Build the prompt with skill-specific instructions
			String enhancedPrompt = buildEnhancedPrompt(request);

			// Configure options with the appropriate skill(s)
			AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder()
					.skill(request.documentType().getSkill())
					.maxTokens(8192);

			// Add custom skill if requested and configured
			if (request.useWatermark() && hasCustomSkill()) {
				logger.info("Adding custom skill: {}", customSkillId);
				optionsBuilder.skill(customSkillId);
			}

			AnthropicChatOptions options = optionsBuilder.build();

			Prompt prompt = new Prompt(List.of(new UserMessage(enhancedPrompt)), options);

			// Call Claude with Skills
			ChatResponse response = chatModel.call(prompt);

			String responseText = response.getResult().getOutput().getText();
			logger.debug("Response text: {}", responseText);

			// Extract file IDs from the response
			List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);
			logger.info("Extracted {} file(s) from response", fileIds.size());

			// Get file metadata for each file
			List<GeneratedFile> files = new ArrayList<>();
			for (String fileId : fileIds) {
				try {
					AnthropicApi.FileMetadata metadata = anthropicApi.getFileMetadata(fileId);
					// Handle null size gracefully
					long fileSize = metadata.size() != null ? metadata.size() : 0L;
					String filename = metadata.filename() != null ? metadata.filename() : "document." + request.documentType().getExtension();
					String mimeType = metadata.mimeType() != null ? metadata.mimeType() : "application/octet-stream";

					files.add(new GeneratedFile(fileId, filename, fileSize, mimeType));
					logger.info("File: {} ({} bytes)", filename, fileSize);
				}
				catch (Exception e) {
					logger.warn("Failed to get metadata for file {}: {}", fileId, e.getMessage());
					// Still add the file with minimal info so user can download it
					files.add(new GeneratedFile(
							fileId,
							"document." + request.documentType().getExtension(),
							0L,
							"application/octet-stream"
					));
				}
			}

			GenerationResult result = GenerationResult.success(id, request.documentType(),
					request.prompt(), responseText, files);

			// Store in history
			history.put(id, result);

			return result;
		}
		catch (Exception e) {
			logger.error("Failed to generate document: {}", e.getMessage(), e);
			GenerationResult result = GenerationResult.failure(id, request.documentType(),
					request.prompt(), e.getMessage());
			history.put(id, result);
			return result;
		}
	}

	/**
	 * Generate a document using Claude Skills with an attached source file.
	 */
	public GenerationResult generateWithFile(GenerationRequest request, MultipartFile sourceFile) {
		String id = UUID.randomUUID().toString();
		logger.info("Generating {} document with id: {} from source file: {}", request.documentType(), id,
				sourceFile.getOriginalFilename());

		try {
			// Build the prompt with file context
			String enhancedPrompt = buildEnhancedPromptWithFile(request, sourceFile);

			// Configure options with the appropriate skill(s)
			AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder()
					.skill(request.documentType().getSkill())
					.maxTokens(8192);

			// Add custom skill if requested and configured
			if (request.useWatermark() && hasCustomSkill()) {
				logger.info("Adding custom skill: {}", customSkillId);
				optionsBuilder.skill(customSkillId);
			}

			AnthropicChatOptions options = optionsBuilder.build();

			// Create the message - for text files, include content in prompt
			// For PDFs, attach as media
			UserMessage userMessage;
			String contentType = sourceFile.getContentType();
			String filename = sourceFile.getOriginalFilename();

			if (isPdfFile(filename, contentType)) {
				// Attach PDF as media
				MimeType mimeType = MimeType.valueOf("application/pdf");
				ByteArrayResource resource = new ByteArrayResource(sourceFile.getBytes());
				Media media = new Media(mimeType, resource);
				userMessage = UserMessage.builder()
						.text(enhancedPrompt)
						.media(media)
						.build();
				logger.debug("Attached PDF file as media");
			}
			else {
				// For text-based files, include content in the prompt
				userMessage = new UserMessage(enhancedPrompt);
			}

			Prompt prompt = new Prompt(List.of(userMessage), options);

			// Call Claude with Skills
			ChatResponse response = chatModel.call(prompt);

			String responseText = response.getResult().getOutput().getText();
			logger.debug("Response text: {}", responseText);

			// Extract file IDs from the response
			List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);
			logger.info("Extracted {} file(s) from response", fileIds.size());

			// Get file metadata for each file
			List<GeneratedFile> files = new ArrayList<>();
			for (String fileId : fileIds) {
				try {
					AnthropicApi.FileMetadata metadata = anthropicApi.getFileMetadata(fileId);
					long fileSize = metadata.size() != null ? metadata.size() : 0L;
					String outputFilename = metadata.filename() != null ? metadata.filename()
							: "document." + request.documentType().getExtension();
					String mimeTypeStr = metadata.mimeType() != null ? metadata.mimeType()
							: "application/octet-stream";

					files.add(new GeneratedFile(fileId, outputFilename, fileSize, mimeTypeStr));
					logger.info("File: {} ({} bytes)", outputFilename, fileSize);
				}
				catch (Exception e) {
					logger.warn("Failed to get metadata for file {}: {}", fileId, e.getMessage());
					files.add(new GeneratedFile(fileId, "document." + request.documentType().getExtension(), 0L,
							"application/octet-stream"));
				}
			}

			GenerationResult result = GenerationResult.success(id, request.documentType(), request.prompt(),
					responseText, files);

			history.put(id, result);
			return result;
		}
		catch (Exception e) {
			logger.error("Failed to generate document: {}", e.getMessage(), e);
			GenerationResult result = GenerationResult.failure(id, request.documentType(), request.prompt(),
					e.getMessage());
			history.put(id, result);
			return result;
		}
	}

	/**
	 * Start async generation (returns immediately with pending status).
	 */
	public GenerationResult generateAsync(GenerationRequest request) {
		String id = UUID.randomUUID().toString();
		logger.info("Starting async generation for {} document with id: {}", request.documentType(), id);

		// Create pending result
		GenerationResult pendingResult = GenerationResult.pending(id, request.documentType(), request.prompt());
		history.put(id, pendingResult);

		// Run generation in background
		CompletableFuture.runAsync(() -> {
			try {
				GenerationResult result = doGenerate(id, request, null);
				history.put(id, result);
				logger.info("Async generation completed for id: {}", id);
			}
			catch (Exception e) {
				logger.error("Async generation failed for id: {}", id, e);
				history.put(id, GenerationResult.failure(id, request.documentType(), request.prompt(), e.getMessage()));
			}
		}, asyncExecutor);

		return pendingResult;
	}

	/**
	 * Start async generation with file (returns immediately with pending status).
	 */
	public GenerationResult generateWithFileAsync(GenerationRequest request, byte[] fileContent, String fileName,
			String contentType) {
		String id = UUID.randomUUID().toString();
		logger.info("Starting async generation for {} document with id: {} from file: {}", request.documentType(), id,
				fileName);

		// Create pending result
		GenerationResult pendingResult = GenerationResult.pending(id, request.documentType(), request.prompt());
		history.put(id, pendingResult);

		// Run generation in background
		CompletableFuture.runAsync(() -> {
			try {
				GenerationResult result = doGenerateWithFile(id, request, fileContent, fileName, contentType);
				history.put(id, result);
				logger.info("Async generation completed for id: {}", id);
			}
			catch (Exception e) {
				logger.error("Async generation failed for id: {}", id, e);
				history.put(id, GenerationResult.failure(id, request.documentType(), request.prompt(), e.getMessage()));
			}
		}, asyncExecutor);

		return pendingResult;
	}

	/**
	 * Internal method to perform generation (used by both sync and async).
	 */
	private GenerationResult doGenerate(String id, GenerationRequest request, Void unused) {
		// Delegate to the existing generate logic but with provided ID
		try {
			String enhancedPrompt = buildEnhancedPrompt(request);

			AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder()
					.skill(request.documentType().getSkill())
					.maxTokens(8192);

			// Add custom skill if requested and configured
			if (request.useWatermark() && hasCustomSkill()) {
				logger.info("Adding custom skill (async): {}", customSkillId);
				optionsBuilder.skill(customSkillId);
			}

			AnthropicChatOptions options = optionsBuilder.build();

			Prompt prompt = new Prompt(List.of(new UserMessage(enhancedPrompt)), options);
			ChatResponse response = chatModel.call(prompt);

			String responseText = response.getResult().getOutput().getText();
			List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);

			List<GeneratedFile> files = extractFiles(fileIds, request.documentType());

			return GenerationResult.success(id, request.documentType(), request.prompt(), responseText, files);
		}
		catch (Exception e) {
			logger.error("Generation failed: {}", e.getMessage(), e);
			return GenerationResult.failure(id, request.documentType(), request.prompt(), e.getMessage());
		}
	}

	/**
	 * Internal method to perform generation with file.
	 */
	private GenerationResult doGenerateWithFile(String id, GenerationRequest request, byte[] fileContent,
			String fileName, String contentType) {
		try {
			String enhancedPrompt = buildEnhancedPromptWithFileContent(request, fileContent, fileName, contentType);

			AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder()
					.skill(request.documentType().getSkill())
					.maxTokens(8192);

			// Add custom skill if requested and configured
			if (request.useWatermark() && hasCustomSkill()) {
				logger.info("Adding custom skill (async with file): {}", customSkillId);
				optionsBuilder.skill(customSkillId);
			}

			AnthropicChatOptions options = optionsBuilder.build();

			UserMessage userMessage;
			if (isPdfFile(fileName, contentType)) {
				MimeType mimeType = MimeType.valueOf("application/pdf");
				ByteArrayResource resource = new ByteArrayResource(fileContent);
				Media media = new Media(mimeType, resource);
				userMessage = UserMessage.builder().text(enhancedPrompt).media(media).build();
			}
			else {
				userMessage = new UserMessage(enhancedPrompt);
			}

			Prompt prompt = new Prompt(List.of(userMessage), options);
			ChatResponse response = chatModel.call(prompt);

			String responseText = response.getResult().getOutput().getText();
			List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);

			List<GeneratedFile> files = extractFiles(fileIds, request.documentType());

			return GenerationResult.success(id, request.documentType(), request.prompt(), responseText, files);
		}
		catch (Exception e) {
			logger.error("Generation with file failed: {}", e.getMessage(), e);
			return GenerationResult.failure(id, request.documentType(), request.prompt(), e.getMessage());
		}
	}

	/**
	 * Extract file metadata from file IDs.
	 */
	private List<GeneratedFile> extractFiles(List<String> fileIds, DocumentType documentType) {
		List<GeneratedFile> files = new ArrayList<>();
		for (String fileId : fileIds) {
			try {
				AnthropicApi.FileMetadata metadata = anthropicApi.getFileMetadata(fileId);
				long fileSize = metadata.size() != null ? metadata.size() : 0L;
				String filename = metadata.filename() != null ? metadata.filename()
						: "document." + documentType.getExtension();
				String mimeType = metadata.mimeType() != null ? metadata.mimeType() : "application/octet-stream";

				files.add(new GeneratedFile(fileId, filename, fileSize, mimeType));
			}
			catch (Exception e) {
				logger.warn("Failed to get metadata for file {}: {}", fileId, e.getMessage());
				files.add(new GeneratedFile(fileId, "document." + documentType.getExtension(), 0L,
						"application/octet-stream"));
			}
		}
		return files;
	}

	/**
	 * Build enhanced prompt with file content (for async where we have byte[]).
	 */
	private String buildEnhancedPromptWithFileContent(GenerationRequest request, byte[] fileContent, String fileName,
			String contentType) {
		StringBuilder promptBuilder = new StringBuilder();

		String typeInstruction = switch (request.documentType()) {
			case XLSX -> "Analyze the provided source document and create an Excel spreadsheet (.xlsx file). "
					+ "Extract relevant data and organize it into a well-structured spreadsheet.";
			case PPTX -> "Analyze the provided source document and create a PowerPoint presentation (.pptx file). "
					+ "Extract key points and create a professional presentation with clear slides.";
			case DOCX -> "Analyze the provided source document and create a Word document (.docx file). "
					+ "Transform the content into a well-formatted document with proper structure.";
			case PDF -> "Analyze the provided source document and create a PDF document. "
					+ "Transform the content into a professionally formatted PDF.";
		};

		promptBuilder.append(typeInstruction);
		promptBuilder.append("\n\nUser instructions: ").append(request.prompt());

		if (!isPdfFile(fileName, contentType)) {
			String textContent = new String(fileContent, StandardCharsets.UTF_8);
			promptBuilder.append("\n\n--- SOURCE DOCUMENT CONTENT (").append(fileName).append(") ---\n");
			promptBuilder.append(textContent);
			promptBuilder.append("\n--- END OF SOURCE DOCUMENT ---");
		}
		else {
			promptBuilder.append("\n\nI have attached a PDF file named '").append(fileName)
					.append("'. Please analyze its contents.");
		}

		return promptBuilder.toString();
	}

	/**
	 * Download a file by its ID.
	 */
	public byte[] downloadFile(String fileId) {
		logger.info("Downloading file: {}", fileId);
		return anthropicApi.downloadFile(fileId);
	}

	/**
	 * Get file metadata.
	 */
	public AnthropicApi.FileMetadata getFileMetadata(String fileId) {
		return anthropicApi.getFileMetadata(fileId);
	}

	/**
	 * Get generation history.
	 */
	public List<GenerationResult> getHistory() {
		return new ArrayList<>(history.values());
	}

	/**
	 * Get a specific generation result.
	 */
	public GenerationResult getResult(String id) {
		return history.get(id);
	}

	/**
	 * Clear history.
	 */
	public void clearHistory() {
		history.clear();
	}

	/**
	 * Remove a specific item from history.
	 */
	public void removeFromHistory(String id) {
		history.remove(id);
	}

	/**
	 * Build an enhanced prompt with document-type-specific instructions.
	 */
	private String buildEnhancedPrompt(GenerationRequest request) {
		String typeInstruction = switch (request.documentType()) {
			case XLSX -> "Create an Excel spreadsheet (.xlsx file) with the following requirements. " +
					"Use appropriate formatting, formulas where relevant, and organize data clearly. ";
			case PPTX -> "Create a PowerPoint presentation (.pptx file) with the following requirements. " +
					"Use a professional design, clear slide titles, and organized content. ";
			case DOCX -> "Create a Word document (.docx file) with the following requirements. " +
					"Use proper formatting, headings, and professional layout. ";
			case PDF -> "Create a PDF document with the following requirements. " +
					"Ensure professional formatting and clear organization. ";
		};

		return typeInstruction + request.prompt();
	}

	/**
	 * Build an enhanced prompt with file content included.
	 */
	private String buildEnhancedPromptWithFile(GenerationRequest request, MultipartFile sourceFile)
			throws IOException {
		StringBuilder promptBuilder = new StringBuilder();

		// Add document type instruction
		String typeInstruction = switch (request.documentType()) {
			case XLSX -> "Analyze the provided source document and create an Excel spreadsheet (.xlsx file). " +
					"Extract relevant data and organize it into a well-structured spreadsheet with appropriate " +
					"formatting and formulas where relevant. ";
			case PPTX -> "Analyze the provided source document and create a PowerPoint presentation (.pptx file). " +
					"Extract key points and create a professional presentation with clear slides, " +
					"organized content, and appropriate visual structure. ";
			case DOCX -> "Analyze the provided source document and create a Word document (.docx file). " +
					"Transform the content into a well-formatted document with proper headings, " +
					"structure, and professional layout. ";
			case PDF -> "Analyze the provided source document and create a PDF document. " +
					"Transform the content into a professionally formatted PDF with clear organization. ";
		};

		promptBuilder.append(typeInstruction);
		promptBuilder.append("\n\nUser instructions: ").append(request.prompt());

		// For text-based files, include content in the prompt
		String filename = sourceFile.getOriginalFilename();
		String contentType = sourceFile.getContentType();

		if (!isPdfFile(filename, contentType)) {
			String fileContent = new String(sourceFile.getBytes(), StandardCharsets.UTF_8);
			promptBuilder.append("\n\n--- SOURCE DOCUMENT CONTENT (").append(filename).append(") ---\n");
			promptBuilder.append(fileContent);
			promptBuilder.append("\n--- END OF SOURCE DOCUMENT ---");
		}
		else {
			promptBuilder.append("\n\nI have attached a PDF file named '").append(filename)
					.append("'. Please analyze its contents.");
		}

		return promptBuilder.toString();
	}

	/**
	 * Check if the file is a PDF based on filename or content type.
	 */
	private boolean isPdfFile(String filename, String contentType) {
		return "application/pdf".equals(contentType)
				|| (filename != null && filename.toLowerCase().endsWith(".pdf"));
	}

	/**
	 * Check if a custom skill is configured.
	 */
	public boolean hasCustomSkill() {
		return customSkillId != null && !customSkillId.isBlank();
	}

}

