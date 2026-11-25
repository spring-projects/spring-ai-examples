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

import org.springframework.ai.skill.annotation.Skill;
import org.springframework.ai.skill.annotation.SkillContent;
import org.springframework.ai.skill.annotation.SkillInit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.skill.annotation.SkillTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;

/**
 * Inventory Management Skill
 *
 * <p>Provides store inventory querying functionality
 *
 * @author Semir
 */
@Skill(
        name = "inventory",
        description = "Inventory management system for checking current stock levels of clothing items",
        source = "example",
        extensions = {"version=1.0.0", "category=retail"})
public class InventorySkill {

    private static final Logger logger = LoggerFactory.getLogger(InventorySkill.class);

    private InventorySkill() {}

    @SkillInit
    public static InventorySkill create() {
        return new InventorySkill();
    }

    @SkillContent
    public String content() {
        return """
            # Inventory Management Skill

            Check current stock levels for all clothing categories in the store.

            ## Features

            - View current inventory by category
            - Check stock levels and sizes
            - Identify low-stock items
            - Get inventory summary

            ## Available Tools

            - `checkInventory` - Check current inventory for a specific category or all categories

            ## Usage

            Ask questions like:
            - "What's the current inventory for winter coats?"
            - "Show me all inventory"
            - "Which items are low in stock?"
            """;
    }

    @SkillTools
    public List<ToolCallback> tools() {
        return List.of(ToolCallbacks.from(this));
    }

    @Tool(
            description =
                    "Check current inventory levels. Use category parameter to filter by clothing type (e.g., 'coat', 'sweater', 'jeans', 'dress', 'all'). Returns current stock count, sizes available, and stock status.")
    public String checkInventory(String category) {
        logger.info("checkInventory called with category={}", category);
        System.out.println(String.format("[TOOL] checkInventory called with category=%s", category));

        StringBuilder result = new StringBuilder();
        result.append("# Current Inventory Report\n\n");
        result.append(String.format("**Category**: %s\n", category));
        result.append(String.format("**Report Date**: %s\n\n", java.time.LocalDate.now()));

        if (category.equalsIgnoreCase("all") || category.equalsIgnoreCase("coat")) {
            result.append("## Winter Coats\n");
            result.append("- **SKU**: COAT-001\n");
            result.append("- **Current Stock**: 15 units\n");
            result.append("- **Sizes**: S(3), M(5), L(4), XL(3)\n");
            result.append("- **Status**: ⚠️ Low Stock\n");
            result.append("- **Retail Price**: $120/unit\n\n");
        }

        if (category.equalsIgnoreCase("all") || category.equalsIgnoreCase("sweater")) {
            result.append("## Sweaters\n");
            result.append("- **SKU**: SWTR-001\n");
            result.append("- **Current Stock**: 45 units\n");
            result.append("- **Sizes**: S(10), M(15), L(12), XL(8)\n");
            result.append("- **Status**: ✅ Good Stock\n");
            result.append("- **Retail Price**: $65/unit\n\n");
        }

        if (category.equalsIgnoreCase("all") || category.equalsIgnoreCase("jeans")) {
            result.append("## Jeans\n");
            result.append("- **SKU**: JEAN-001\n");
            result.append("- **Current Stock**: 8 units\n");
            result.append("- **Sizes**: 28(1), 30(2), 32(3), 34(2)\n");
            result.append("- **Status**: 🔴 Critical - Restock Needed\n");
            result.append("- **Retail Price**: $80/unit\n\n");
        }

        if (category.equalsIgnoreCase("all") || category.equalsIgnoreCase("dress")) {
            result.append("## Dresses\n");
            result.append("- **SKU**: DRSS-001\n");
            result.append("- **Current Stock**: 25 units\n");
            result.append("- **Sizes**: S(8), M(10), L(5), XL(2)\n");
            result.append("- **Status**: ✅ Good Stock\n");
            result.append("- **Retail Price**: $95/unit\n\n");
        }

        if (category.equalsIgnoreCase("all") || category.equalsIgnoreCase("shirt")) {
            result.append("## Shirts\n");
            result.append("- **SKU**: SHRT-001\n");
            result.append("- **Current Stock**: 12 units\n");
            result.append("- **Sizes**: S(2), M(4), L(4), XL(2)\n");
            result.append("- **Status**: ⚠️ Low Stock\n");
            result.append("- **Retail Price**: $45/unit\n\n");
        }

        result.append("## Summary\n");
        result.append("- **Total Items**: 105 units\n");
        result.append("- **Low Stock Items**: 2 (Coats, Shirts)\n");
        result.append("- **Critical Items**: 1 (Jeans)\n");
        result.append("- **Action Required**: Consider restocking Jeans immediately\n");

        return result.toString();
    }
}
