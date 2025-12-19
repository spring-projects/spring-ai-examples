package com.example.documentforge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.documentforge.model.DocumentType;
import com.example.documentforge.service.DocumentGenerationService;

/**
 * Controller for the home page.
 */
@Controller
public class HomeController {

	private final DocumentGenerationService generationService;

	public HomeController(DocumentGenerationService generationService) {
		this.generationService = generationService;
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("documentTypes", DocumentType.values());
		model.addAttribute("history", generationService.getHistory());
		model.addAttribute("hasCustomSkill", generationService.hasCustomSkill());
		return "index";
	}

}
