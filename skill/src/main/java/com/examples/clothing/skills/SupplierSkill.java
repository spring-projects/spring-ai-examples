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
import org.springframework.ai.skill.annotation.SkillTools;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;

/**
 * Supplier Management Skill
 *
 * <p>Provides supplier catalog and wholesale cost querying
 *
 * @author Semir
 */
@Skill(
        name = "supplier",
        description = "Supplier catalog system for checking available items and wholesale costs",
        source = "example",
        extensions = {"version=1.0.0", "category=retail"})
public class SupplierSkill {

    private static final Logger logger = LoggerFactory.getLogger(SupplierSkill.class);

    private SupplierSkill() {}

    @SkillInit
    public static SupplierSkill create() {
        return new SupplierSkill();
    }

    @SkillContent
    public String content() {
        return """
            # Supplier Catalog Skill

            Access supplier inventory and wholesale pricing information.

            ## Features

            - Browse supplier catalog
            - Check wholesale prices
            - View available quantities
            - Get delivery information

            ## Available Tools

            - `getSupplierCatalog` - Get supplier catalog for specific categories or all items
            - `getSupplierQuote` - Get detailed quote including bulk discounts

            ## Usage

            Ask questions like:
            - "What items are available from the supplier?"
            - "Get supplier catalog for winter items"
            - "Get a quote for 50 units of coats"
            """;
    }

    @SkillTools
    public List<ToolCallback> tools() {
        return List.of(ToolCallbacks.from(this));
    }

    @Tool(
            description =
                    "Get supplier catalog showing available items for purchase. Use category parameter to filter (e.g., 'winter', 'summer', 'formal', 'casual', 'all'). Returns item details, wholesale cost, minimum order quantity, and availability.")
    public String getSupplierCatalog(String category) {
        logger.info("getSupplierCatalog called with category={}", category);
        System.out.println(String.format("[TOOL] getSupplierCatalog called with category=%s", category));

        StringBuilder result = new StringBuilder();
        result.append("# Supplier Catalog\n\n");
        result.append(String.format("**Category**: %s\n", category));
        result.append(String.format("**Catalog Date**: %s\n", java.time.LocalDate.now()));
        result.append("**Supplier**: Fashion Wholesale Co.\n\n");

        if (category.equalsIgnoreCase("all")
                || category.toLowerCase().contains("winter")
                || category.toLowerCase().contains("coat")) {
            result.append("## Winter Collection\n\n");

            result.append("### Premium Winter Coats\n");
            result.append("- **Supplier SKU**: SUP-COAT-W01\n");
            result.append("- **Wholesale Cost**: $75/unit\n");
            result.append("- **Min Order**: 10 units\n");
            result.append("- **Available Stock**: 500+ units\n");
            result.append("- **Sizes**: S, M, L, XL, XXL\n");
            result.append("- **Colors**: Black, Navy, Gray, Burgundy\n");
            result.append("- **Delivery**: 3-5 business days\n");
            result.append("- **Bulk Discount**: 5% off for 50+ units\n\n");

            result.append("### Wool Sweaters\n");
            result.append("- **Supplier SKU**: SUP-SWTR-W01\n");
            result.append("- **Wholesale Cost**: $38/unit\n");
            result.append("- **Min Order**: 20 units\n");
            result.append("- **Available Stock**: 800+ units\n");
            result.append("- **Sizes**: S, M, L, XL\n");
            result.append("- **Colors**: Multiple colors available\n");
            result.append("- **Delivery**: 2-4 business days\n");
            result.append("- **Bulk Discount**: 8% off for 100+ units\n\n");
        }

        if (category.equalsIgnoreCase("all")
                || category.toLowerCase().contains("casual")
                || category.toLowerCase().contains("jeans")) {
            result.append("## Casual Collection\n\n");

            result.append("### Premium Denim Jeans\n");
            result.append("- **Supplier SKU**: SUP-JEAN-C01\n");
            result.append("- **Wholesale Cost**: $45/unit\n");
            result.append("- **Min Order**: 15 units\n");
            result.append("- **Available Stock**: 600+ units\n");
            result.append("- **Sizes**: 28, 30, 32, 34, 36, 38\n");
            result.append("- **Styles**: Slim fit, Regular fit, Relaxed fit\n");
            result.append("- **Delivery**: 3-5 business days\n");
            result.append("- **Bulk Discount**: 7% off for 50+ units\n\n");

            result.append("### Cotton Shirts\n");
            result.append("- **Supplier SKU**: SUP-SHRT-C01\n");
            result.append("- **Wholesale Cost**: $25/unit\n");
            result.append("- **Min Order**: 20 units\n");
            result.append("- **Available Stock**: 1000+ units\n");
            result.append("- **Sizes**: S, M, L, XL, XXL\n");
            result.append("- **Colors**: White, Blue, Black, Gray\n");
            result.append("- **Delivery**: 2-3 business days\n");
            result.append("- **Bulk Discount**: 10% off for 100+ units\n\n");
        }

        if (category.equalsIgnoreCase("all")
                || category.toLowerCase().contains("formal")
                || category.toLowerCase().contains("dress")) {
            result.append("## Formal Collection\n\n");

            result.append("### Evening Dresses\n");
            result.append("- **Supplier SKU**: SUP-DRSS-F01\n");
            result.append("- **Wholesale Cost**: $52/unit\n");
            result.append("- **Min Order**: 12 units\n");
            result.append("- **Available Stock**: 400+ units\n");
            result.append("- **Sizes**: S, M, L, XL\n");
            result.append("- **Styles**: A-line, Bodycon, Maxi\n");
            result.append("- **Delivery**: 4-6 business days\n");
            result.append("- **Bulk Discount**: 6% off for 40+ units\n\n");
        }

        result.append("## Payment & Delivery Terms\n");
        result.append("- **Payment**: Net 30 days for established customers\n");
        result.append("- **Shipping**: Free shipping for orders over $2,000\n");
        result.append("- **Returns**: 14-day return policy on unused items\n");
        result.append("- **Express Delivery**: Available for urgent orders (+$50)\n");

        return result.toString();
    }

    @Tool(
            description =
                    "Get detailed quote for a specific supplier SKU with quantity. Parameters: supplierSku (e.g., 'SUP-COAT-W01'), quantity (number of units). Returns itemized quote with unit cost, bulk discounts, total cost, and delivery estimate.")
    public String getSupplierQuote(String supplierSku, int quantity) {
        logger.info("getSupplierQuote called with supplierSku={}, quantity={}", supplierSku, quantity);
        System.out.println(String.format(
                "[TOOL] getSupplierQuote called with supplierSku=%s, quantity=%d", supplierSku, quantity));

        StringBuilder result = new StringBuilder();
        result.append("# Supplier Quote\n\n");
        result.append(String.format("**Quote ID**: QT-%05d\n", 12345));
        result.append(String.format("**Date**: %s\n", java.time.LocalDate.now()));
        result.append("**Supplier**: Fashion Wholesale Co.\n\n");

        String itemName;
        double basePrice;
        int minOrder;
        int bulkThreshold;
        double bulkDiscount;

        switch (supplierSku.toUpperCase()) {
            case "SUP-COAT-W01":
                itemName = "Premium Winter Coats";
                basePrice = 75.0;
                minOrder = 10;
                bulkThreshold = 50;
                bulkDiscount = 0.05;
                break;
            case "SUP-SWTR-W01":
                itemName = "Wool Sweaters";
                basePrice = 38.0;
                minOrder = 20;
                bulkThreshold = 100;
                bulkDiscount = 0.08;
                break;
            case "SUP-JEAN-C01":
                itemName = "Premium Denim Jeans";
                basePrice = 45.0;
                minOrder = 15;
                bulkThreshold = 50;
                bulkDiscount = 0.07;
                break;
            case "SUP-SHRT-C01":
                itemName = "Cotton Shirts";
                basePrice = 25.0;
                minOrder = 20;
                bulkThreshold = 100;
                bulkDiscount = 0.10;
                break;
            case "SUP-DRSS-F01":
                itemName = "Evening Dresses";
                basePrice = 52.0;
                minOrder = 12;
                bulkThreshold = 40;
                bulkDiscount = 0.06;
                break;
            default:
                return String.format("Error: Supplier SKU '%s' not found in catalog.", supplierSku);
        }

        result.append(String.format("## %s (%s)\n\n", itemName, supplierSku));
        result.append(String.format("**Quantity Requested**: %d units\n", quantity));

        if (quantity < minOrder) {
            result.append(String.format("⚠️ **Warning**: Minimum order quantity is %d units\n\n", minOrder));
            return result.toString();
        }

        result.append(String.format("**Unit Price**: $%.2f\n", basePrice));

        double discount = 0;
        if (quantity >= bulkThreshold) {
            discount = bulkDiscount;
            result.append(String.format("**Bulk Discount**: %.0f%% (for %d+ units)\n", discount * 100, bulkThreshold));
        } else {
            result.append("**Bulk Discount**: Not applicable\n");
        }

        double discountedPrice = basePrice * (1 - discount);
        double subtotal = discountedPrice * quantity;
        double shipping = subtotal >= 2000 ? 0 : 80;
        double total = subtotal + shipping;

        result.append(String.format("**Discounted Price**: $%.2f/unit\n\n", discountedPrice));

        result.append("## Cost Breakdown\n");
        result.append(String.format("- Subtotal: $%.2f (%d units × $%.2f)\n", subtotal, quantity, discountedPrice));
        result.append(
                String.format("- Shipping: $%.2f %s\n", shipping, shipping == 0 ? "(Free - order over $2,000)" : ""));
        result.append(String.format("- **Total Cost**: $%.2f\n\n", total));

        result.append("## Delivery Information\n");
        result.append("- **Estimated Delivery**: 3-5 business days\n");
        result.append("- **Express Option**: Available (+$50 for next day)\n\n");

        result.append("## Additional Information\n");
        result.append("- **Payment Terms**: Net 30 days\n");
        result.append("- **Valid Until**: 7 days from quote date\n");
        result.append(String.format(
                "- **Potential Profit**: $%.2f (if sold at retail price)\n", (120 - discountedPrice) * quantity));

        return result.toString();
    }
}
