package com.example.documentforge.controller;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.documentforge.model.GenerationResult;
import com.example.documentforge.service.DocumentGenerationService;

/**
 * Controller for generation history management.
 */
@Controller
@RequestMapping("/history")
public class HistoryController {

	private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);

	private final DocumentGenerationService generationService;

	public HistoryController(DocumentGenerationService generationService) {
		this.generationService = generationService;
	}

	/**
	 * Get history list as HTMX fragment.
	 */
	@GetMapping
	public String getHistory(Model model) {
		List<GenerationResult> history = generationService.getHistory()
				.stream()
				.sorted(Comparator.comparing(GenerationResult::createdAt).reversed())
				.toList();

		model.addAttribute("history", history);
		return "fragments/history :: list";
	}

	/**
	 * Clear all history.
	 */
	@DeleteMapping
	public String clearHistory(Model model) {
		logger.info("Clearing all history");
		generationService.clearHistory();
		model.addAttribute("history", List.of());
		return "fragments/history :: cleared";
	}

	/**
	 * Delete a specific history item.
	 */
	@DeleteMapping("/{id}")
	public String deleteHistoryItem(@PathVariable String id, Model model) {
		logger.info("Deleting history item: {}", id);
		generationService.removeFromHistory(id);

		List<GenerationResult> history = generationService.getHistory()
				.stream()
				.sorted(Comparator.comparing(GenerationResult::createdAt).reversed())
				.toList();

		model.addAttribute("history", history);
		return "fragments/history :: list";
	}

	/**
	 * View a specific result (for async job results).
	 */
	@GetMapping("/{id}")
	public String viewResult(@PathVariable String id, Model model) {
		logger.info("Viewing result: {}", id);
		GenerationResult result = generationService.getResult(id);

		if (result == null) {
			model.addAttribute("error", "Result not found");
			return "fragments/response :: error";
		}

		model.addAttribute("result", result);

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

}
