package com.example.documentforge.model;

import org.springframework.ai.anthropic.api.AnthropicApi.AnthropicSkill;

/**
 * Supported document types for generation.
 */
public enum DocumentType {

	XLSX("Excel Spreadsheet", "xlsx", "#22C55E", "spreadsheet", AnthropicSkill.XLSX),
	PPTX("PowerPoint Presentation", "pptx", "#F97316", "presentation", AnthropicSkill.PPTX),
	DOCX("Word Document", "docx", "#3B82F6", "document", AnthropicSkill.DOCX),
	PDF("PDF Document", "pdf", "#EF4444", "document", AnthropicSkill.PDF);

	private final String displayName;
	private final String extension;
	private final String color;
	private final String icon;
	private final AnthropicSkill skill;

	DocumentType(String displayName, String extension, String color, String icon, AnthropicSkill skill) {
		this.displayName = displayName;
		this.extension = extension;
		this.color = color;
		this.icon = icon;
		this.skill = skill;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getExtension() {
		return extension;
	}

	public String getColor() {
		return color;
	}

	public String getIcon() {
		return icon;
	}

	public AnthropicSkill getSkill() {
		return skill;
	}

}
