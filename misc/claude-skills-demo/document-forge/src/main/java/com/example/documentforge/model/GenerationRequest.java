package com.example.documentforge.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request to generate a document.
 */
public record GenerationRequest(
		@NotNull(message = "Document type is required")
		DocumentType documentType,

		@NotBlank(message = "Prompt is required")
		String prompt,

		boolean useWatermark
) {
	/**
	 * Constructor without watermark option (defaults to false).
	 */
	public GenerationRequest(DocumentType documentType, String prompt) {
		this(documentType, prompt, false);
	}
}
