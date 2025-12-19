package com.example.documentforge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.documentforge.service.DocumentGenerationService;

import org.springframework.ai.anthropic.api.AnthropicApi;

/**
 * Controller for file downloads.
 */
@Controller
@RequestMapping("/download")
public class DownloadController {

	private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

	private final DocumentGenerationService generationService;

	public DownloadController(DocumentGenerationService generationService) {
		this.generationService = generationService;
	}

	@GetMapping("/{fileId}")
	public ResponseEntity<byte[]> download(@PathVariable String fileId) {
		logger.info("Download request for file: {}", fileId);

		try {
			// Get file metadata
			AnthropicApi.FileMetadata metadata = generationService.getFileMetadata(fileId);

			// Download file content
			byte[] content = generationService.downloadFile(fileId);

			// Build response with appropriate headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(metadata.mimeType()));
			headers.setContentDisposition(ContentDisposition.attachment()
					.filename(metadata.filename())
					.build());
			headers.setContentLength(content.length);

			logger.info("Serving file: {} ({} bytes)", metadata.filename(), content.length);

			return ResponseEntity.ok()
					.headers(headers)
					.body(content);
		}
		catch (Exception e) {
			logger.error("Download failed for file {}: {}", fileId, e.getMessage());
			return ResponseEntity.notFound().build();
		}
	}

}
