package com.example.documentforge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.documentforge.model.DocumentType;
import com.example.documentforge.model.GenerationRequest;
import com.example.documentforge.model.GenerationResult;
import com.example.documentforge.service.DocumentGenerationService;

/**
 * Controller for document generation.
 */
@Controller
@RequestMapping("/generate")
public class GenerateController {

	private static final Logger logger = LoggerFactory.getLogger(GenerateController.class);

	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	private final DocumentGenerationService generationService;

	public GenerateController(DocumentGenerationService generationService) {
		this.generationService = generationService;
	}

	@PostMapping
	public String generate(@RequestParam DocumentType documentType, @RequestParam String prompt,
			@RequestParam(required = false) MultipartFile sourceFile,
			@RequestParam(required = false, defaultValue = "false") boolean useWatermark, Model model) {

		logger.info("Generate request: type={}, prompt={}, hasFile={}, useWatermark={}", documentType,
				prompt.substring(0, Math.min(50, prompt.length())), sourceFile != null && !sourceFile.isEmpty(),
				useWatermark);

		// Validate inputs
		if (documentType == null || prompt == null || prompt.isBlank()) {
			model.addAttribute("error", "Please provide both document type and prompt");
			model.addAttribute("documentTypes", DocumentType.values());
			return "fragments/response :: error";
		}

		// Validate file if provided
		if (sourceFile != null && !sourceFile.isEmpty()) {
			if (sourceFile.getSize() > MAX_FILE_SIZE) {
				model.addAttribute("error", "File too large. Maximum size is 10MB.");
				return "fragments/response :: error";
			}

			String filename = sourceFile.getOriginalFilename();
			if (filename != null) {
				String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
				if (!isAllowedFileType(ext)) {
					model.addAttribute("error",
							"Invalid file type. Allowed: PDF, TXT, CSV, JSON, XML, MD");
					return "fragments/response :: error";
				}
			}
		}

		try {
			GenerationRequest request = new GenerationRequest(documentType, prompt, useWatermark);
			GenerationResult result;

			// Use async for PPTX (takes too long for synchronous)
			boolean useAsync = generationService.shouldUseAsync(documentType);

			if (sourceFile != null && !sourceFile.isEmpty()) {
				if (useAsync) {
					// Read file content for async processing
					byte[] fileContent = sourceFile.getBytes();
					String fileName = sourceFile.getOriginalFilename();
					String contentType = sourceFile.getContentType();
					result = generationService.generateWithFileAsync(request, fileContent, fileName, contentType);
				}
				else {
					result = generationService.generateWithFile(request, sourceFile);
				}
			}
			else {
				if (useAsync) {
					result = generationService.generateAsync(request);
				}
				else {
					result = generationService.generate(request);
				}
			}

			model.addAttribute("result", result);
			model.addAttribute("documentTypes", DocumentType.values());

			if (result.pending()) {
				return "fragments/response :: pending";
			}
			else if (result.success()) {
				return "fragments/response :: success";
			}
			else {
				model.addAttribute("error", result.errorMessage());
				return "fragments/response :: error";
			}
		}
		catch (Exception e) {
			logger.error("Generation failed", e);
			model.addAttribute("error", "Failed to generate document: " + e.getMessage());
			return "fragments/response :: error";
		}
	}

	private boolean isAllowedFileType(String extension) {
		return switch (extension) {
			case "pdf", "txt", "csv", "json", "xml", "md" -> true;
			default -> false;
		};
	}

}
