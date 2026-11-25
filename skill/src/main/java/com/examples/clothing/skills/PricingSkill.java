/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.examples.clothing.skills;


import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.skill.annotation.Skill;
import org.springframework.ai.skill.annotation.SkillContent;
import org.springframework.ai.skill.annotation.SkillInit;
import org.springframework.ai.skill.annotation.SkillTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;

/**
 * Store Pricing Management Skill
 *
 * <p>Provides store product pricing and profit margin analysis
 *
 * @author Semir
 */
@Skill(
        name = "pricing",
        description = "Store pricing system for checking retail prices and profit margins",
        source = "example",
        extensions = {"version=1.0.0", "category=retail"})
public class PricingSkill {

    private static final Logger logger = LoggerFactory.getLogger(PricingSkill.class);

    private PricingSkill() {}

    @SkillInit
    public static PricingSkill create() {
        return new PricingSkill();
    }

    @SkillContent
    public String content() {
        return """
            # Pricing Management Skill

            Analyze store pricing strategy and profit margins for clothing items.

            ## Features

            - Check retail prices by SKU
            - Calculate profit margins
            - Compare pricing across categories
            - Analyze pricing competitiveness

            ## Available Tools

            - `getPricing` - Get detailed pricing information for specific items or categories

            ## Usage

            Ask questions like:
            - "What's the pricing for winter coats?"
            - "Show me profit margins for all items"
            - "Get pricing for SKU COAT-001"
            """;
    }

    @SkillTools
    public List<ToolCallback> tools() {
        return List.of(ToolCallbacks.from(this));
    }

    @Tool(
            description =
                    "Get pricing information including retail price, cost, and profit margin. Use sku parameter for specific item (e.g., 'COAT-001') or category name for all items in that category (e.g., 'coat', 'sweater', 'all').")
    public String getPricing(String skuOrCategory) {
        logger.info("getPricing called with skuOrCategory={}", skuOrCategory);
        System.out.println(String.format("[TOOL] getPricing called with skuOrCategory=%s", skuOrCategory));

        StringBuilder result = new StringBuilder();
        result.append("# Pricing Analysis Report\n\n");
        result.append(String.format("**Query**: %s\n", skuOrCategory));
        result.append(String.format("**Report Date**: %s\n\n", java.time.LocalDate.now()));

        if (skuOrCategory.equalsIgnoreCase("all")
                || skuOrCategory.equalsIgnoreCase("coat")
                || skuOrCategory.equalsIgnoreCase("COAT-001")) {
            result.append("## Winter Coats (COAT-001)\n");
            result.append("- **Retail Price**: $120/unit\n");
            result.append("- **Cost Price**: $75/unit\n");
            result.append("- **Profit Margin**: $45 (37.5%)\n");
            result.append("- **Recommended Price Range**: $110-$135\n");
            result.append("- **Competitiveness**: ✅ Competitive\n\n");
        }

        if (skuOrCategory.equalsIgnoreCase("all")
                || skuOrCategory.equalsIgnoreCase("sweater")
                || skuOrCategory.equalsIgnoreCase("SWTR-001")) {
            result.append("## Sweaters (SWTR-001)\n");
            result.append("- **Retail Price**: $65/unit\n");
            result.append("- **Cost Price**: $38/unit\n");
            result.append("- **Profit Margin**: $27 (41.5%)\n");
            result.append("- **Recommended Price Range**: $60-$75\n");
            result.append("- **Competitiveness**: ✅ Competitive\n\n");
        }

        if (skuOrCategory.equalsIgnoreCase("all")
                || skuOrCategory.equalsIgnoreCase("jeans")
                || skuOrCategory.equalsIgnoreCase("JEAN-001")) {
            result.append("## Jeans (JEAN-001)\n");
            result.append("- **Retail Price**: $80/unit\n");
            result.append("- **Cost Price**: $45/unit\n");
            result.append("- **Profit Margin**: $35 (43.75%)\n");
            result.append("- **Recommended Price Range**: $75-$90\n");
            result.append("- **Competitiveness**: ✅ Competitive\n\n");
        }

        if (skuOrCategory.equalsIgnoreCase("all")
                || skuOrCategory.equalsIgnoreCase("dress")
                || skuOrCategory.equalsIgnoreCase("DRSS-001")) {
            result.append("## Dresses (DRSS-001)\n");
            result.append("- **Retail Price**: $95/unit\n");
            result.append("- **Cost Price**: $52/unit\n");
            result.append("- **Profit Margin**: $43 (45.3%)\n");
            result.append("- **Recommended Price Range**: $90-$110\n");
            result.append("- **Competitiveness**: ✅ Competitive\n\n");
        }

        if (skuOrCategory.equalsIgnoreCase("all")
                || skuOrCategory.equalsIgnoreCase("shirt")
                || skuOrCategory.equalsIgnoreCase("SHRT-001")) {
            result.append("## Shirts (SHRT-001)\n");
            result.append("- **Retail Price**: $45/unit\n");
            result.append("- **Cost Price**: $25/unit\n");
            result.append("- **Profit Margin**: $20 (44.4%)\n");
            result.append("- **Recommended Price Range**: $40-$50\n");
            result.append("- **Competitiveness**: ✅ Competitive\n\n");
        }

        result.append("## Overall Pricing Analysis\n");
        result.append("- **Average Profit Margin**: 42.5%\n");
        result.append("- **Highest Margin**: Dresses (45.3%)\n");
        result.append("- **Lowest Margin**: Winter Coats (37.5%)\n");
        result.append("- **Pricing Strategy**: Premium positioning with healthy margins\n");

        return result.toString();
    }
}
