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
 * Purchase Strategy Skill
 *
 * <p>Provides intelligent restocking recommendations and purchase optimization strategies
 *
 * @author Semir
 */
@Skill(
        name = "purchase",
        description = "Smart purchasing strategy system that provides optimized restocking recommendations",
        source = "example",
        extensions = {"version=1.0.0", "category=retail", "ai-powered=true"})
public class PurchaseStrategySkill {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseStrategySkill.class);

    private PurchaseStrategySkill() {}

    @SkillInit
    public static PurchaseStrategySkill create() {
        return new PurchaseStrategySkill();
    }

    @SkillContent
    public String content() {
        return """
            # Purchase Strategy Skill

            Generate intelligent purchasing recommendations based on inventory, sales trends, and market conditions.

            ## Features

            - Smart restocking recommendations
            - Budget optimization
            - ROI analysis
            - Seasonal planning
            - Risk assessment

            ## Available Tools

            - `generatePurchaseStrategy` - Generate comprehensive purchasing strategy
            - `optimizePurchaseOrder` - Optimize a specific purchase order for maximum ROI

            ## Usage

            Ask questions like:
            - "Generate a purchase strategy for next week"
            - "What should I order with a $10,000 budget?"
            - "Optimize my purchase order for maximum profit"
            """;
    }

    @SkillTools
    public List<ToolCallback> tools() {
        return List.of(ToolCallbacks.from(this));
    }

    @Tool(
            description =
                    "Generate comprehensive purchase strategy based on current conditions. Parameters: budget (available budget in USD, e.g., 10000), priority (focus area: 'profit', 'volume', 'balanced', 'risk-averse'). Returns prioritized purchase recommendations with ROI projections.")
    public String generatePurchaseStrategy(double budget, String priority) {
        logger.info("generatePurchaseStrategy called with budget={}, priority={}", budget, priority);
        System.out.println(String.format(
                "[TOOL] generatePurchaseStrategy called with budget=%.2f, priority=%s", budget, priority));

        StringBuilder result = new StringBuilder();
        result.append("# Purchase Strategy Report\n\n");
        result.append(String.format("**Available Budget**: $%.2f\n", budget));
        result.append(String.format("**Strategy Priority**: %s\n", priority));
        result.append(String.format("**Report Date**: %s\n\n", java.time.LocalDate.now()));

        result.append("## Current Situation Analysis\n\n");
        result.append("### Critical Findings\n");
        result.append("- 🔴 **Critical Stock-out Risk**: Jeans (8 units, ~1 day left)\n");
        result.append("- ⚠️ **Low Stock Alert**: Winter Coats (15 units, ~2 days left)\n");
        result.append("- ⚠️ **Low Stock Alert**: Shirts (12 units, ~3 days left)\n");
        result.append("- ✅ **Adequate Stock**: Sweaters (45 units, ~4 days left)\n");
        result.append("- ✅ **Good Stock**: Dresses (25 units, ~2 weeks left)\n\n");

        result.append("### Sales Trends\n");
        result.append("- **Hot Sellers**: Winter Coats (+45%), Sweaters (+35%)\n");
        result.append("- **Stable**: Jeans, Shirts\n");
        result.append("- **Declining**: Dresses (-15%)\n\n");

        result.append("## Recommended Purchase Orders\n\n");

        double totalCost = 0;
        double projectedRevenue = 0;

        result.append("### Priority 1: URGENT - Jeans\n");
        result.append("- **Supplier SKU**: SUP-JEAN-C01\n");
        result.append("- **Recommended Quantity**: 70 units\n");
        result.append("- **Unit Cost**: $45.00 (with 7% bulk discount: $41.85)\n");
        result.append("- **Total Cost**: $2,929.50\n");
        result.append("- **Expected Revenue**: $5,600 (70 units × $80)\n");
        result.append("- **Projected Profit**: $2,670.50 (91% ROI)\n");
        result.append("- **Justification**: Critical stock-out prevention, consistent demand\n");
        result.append("- **Delivery**: 3-5 business days\n\n");
        totalCost += 2929.50;
        projectedRevenue += 5600;

        result.append("### Priority 2: HIGH - Winter Coats\n");
        result.append("- **Supplier SKU**: SUP-COAT-W01\n");
        result.append("- **Recommended Quantity**: 50 units\n");
        result.append("- **Unit Cost**: $75.00 (with 5% bulk discount: $71.25)\n");
        result.append("- **Total Cost**: $3,562.50\n");
        result.append("- **Expected Revenue**: $6,000 (50 units × $120)\n");
        result.append("- **Projected Profit**: $2,437.50 (68% ROI)\n");
        result.append("- **Justification**: High demand (+45%), peak season, low stock\n");
        result.append("- **Delivery**: 3-5 business days\n\n");
        totalCost += 3562.50;
        projectedRevenue += 6000;

        if (budget >= 8000) {
            result.append("### Priority 3: MEDIUM - Sweaters\n");
            result.append("- **Supplier SKU**: SUP-SWTR-W01\n");
            result.append("- **Recommended Quantity**: 100 units\n");
            result.append("- **Unit Cost**: $38.00 (with 8% bulk discount: $34.96)\n");
            result.append("- **Total Cost**: $3,496.00\n");
            result.append("- **Expected Revenue**: $6,500 (100 units × $65)\n");
            result.append("- **Projected Profit**: $3,004.00 (86% ROI)\n");
            result.append("- **Justification**: Top seller (+35%), stock sufficient for 4 days only\n");
            result.append("- **Delivery**: 2-4 business days\n\n");
            totalCost += 3496.00;
            projectedRevenue += 6500;
        }

        if (budget >= 11000) {
            result.append("### Priority 4: MEDIUM - Shirts\n");
            result.append("- **Supplier SKU**: SUP-SHRT-C01\n");
            result.append("- **Recommended Quantity**: 40 units\n");
            result.append("- **Unit Cost**: $25.00\n");
            result.append("- **Total Cost**: $1,000.00\n");
            result.append("- **Expected Revenue**: $1,800 (40 units × $45)\n");
            result.append("- **Projected Profit**: $800.00 (80% ROI)\n");
            result.append("- **Justification**: Low stock, consistent basic item demand\n");
            result.append("- **Delivery**: 2-3 business days\n\n");
            totalCost += 1000.00;
            projectedRevenue += 1800;
        }

        result.append("## Financial Summary\n\n");
        result.append(String.format("- **Total Investment**: $%.2f\n", totalCost));
        result.append(String.format("- **Budget Remaining**: $%.2f\n", budget - totalCost));
        result.append(String.format("- **Budget Utilization**: %.1f%%\n", (totalCost / budget) * 100));
        result.append(String.format("- **Projected Revenue**: $%.2f\n", projectedRevenue));
        result.append(String.format("- **Projected Profit**: $%.2f\n", projectedRevenue - totalCost));
        result.append(
                String.format("- **Expected ROI**: %.1f%%\n\n", ((projectedRevenue - totalCost) / totalCost) * 100));

        result.append("## Risk Assessment\n\n");
        result.append("- **Stock-out Risk**: 🔴 High (Jeans critical)\n");
        result.append("- **Over-stock Risk**: ✅ Low (all orders based on demand)\n");
        result.append("- **Weather Risk**: ⚠️ Medium (winter items dependent on cold weather)\n");
        result.append("- **Cash Flow Risk**: ✅ Low (Net 30 payment terms)\n\n");

        result.append("## Implementation Plan\n\n");
        result.append("**Immediate Actions (Today)**:\n");
        result.append("1. Place order for Jeans (70 units) - URGENT\n");
        result.append("2. Place order for Winter Coats (50 units) - HIGH PRIORITY\n\n");

        result.append("**This Week**:\n");
        result.append("3. Monitor Sweater sales velocity\n");
        result.append("4. Place order for Sweaters (100 units) if budget allows\n\n");

        result.append("**Next Week**:\n");
        result.append("5. Review shirt inventory after first orders arrive\n");
        result.append("6. Adjust strategy based on actual sales data\n\n");

        result.append("## Additional Recommendations\n");
        result.append("- 💡 **Promotional Opportunity**: Consider 10-15% discount on Dresses to clear inventory\n");
        result.append("- 📊 **Inventory Target**: Maintain 7-10 days of stock for fast-moving items\n");
        result.append("- 🎯 **Focus**: Winter items are in peak demand - maximize this opportunity\n");
        result.append("- ⚙️ **Process**: Implement daily inventory checks during peak season\n");

        return result.toString();
    }

    @Tool(
            description =
                    "Optimize a specific purchase order for maximum ROI. Parameters: items (comma-separated list of supplier SKUs, e.g., 'SUP-COAT-W01,SUP-JEAN-C01'), quantities (comma-separated quantities matching items, e.g., '50,70'). Returns optimization suggestions including cost efficiency and profit maximization tips.")
    public String optimizePurchaseOrder(String items, String quantities) {
        logger.info("optimizePurchaseOrder called with items={}, quantities={}", items, quantities);
        System.out.println(
                String.format("[TOOL] optimizePurchaseOrder called with items=%s, quantities=%s", items, quantities));

        StringBuilder result = new StringBuilder();
        result.append("# Purchase Order Optimization\n\n");
        result.append(String.format("**Order Date**: %s\n", java.time.LocalDate.now()));
        result.append(String.format("**Order ID**: PO-%05d\n\n", 12345));

        String[] itemArray = items.split(",");
        String[] qtyArray = quantities.split(",");

        if (itemArray.length != qtyArray.length) {
            return "Error: Number of items and quantities must match.";
        }

        result.append("## Order Analysis\n\n");

        double totalCost = 0;
        double optimizedCost = 0;

        for (int i = 0; i < itemArray.length; i++) {
            String sku = itemArray[i].trim();
            int qty = Integer.parseInt(qtyArray[i].trim());

            result.append(String.format("### Item %d: %s\n", i + 1, sku));
            result.append(String.format("- **Requested Quantity**: %d units\n", qty));

            double unitCost;
            int bulkThreshold;
            double bulkDiscount;
            int minOrder;

            switch (sku.toUpperCase()) {
                case "SUP-COAT-W01":
                    unitCost = 75.0;
                    bulkThreshold = 50;
                    bulkDiscount = 0.05;
                    minOrder = 10;
                    break;
                case "SUP-SWTR-W01":
                    unitCost = 38.0;
                    bulkThreshold = 100;
                    bulkDiscount = 0.08;
                    minOrder = 20;
                    break;
                case "SUP-JEAN-C01":
                    unitCost = 45.0;
                    bulkThreshold = 50;
                    bulkDiscount = 0.07;
                    minOrder = 15;
                    break;
                case "SUP-SHRT-C01":
                    unitCost = 25.0;
                    bulkThreshold = 100;
                    bulkDiscount = 0.10;
                    minOrder = 20;
                    break;
                case "SUP-DRSS-F01":
                    unitCost = 52.0;
                    bulkThreshold = 40;
                    bulkDiscount = 0.06;
                    minOrder = 12;
                    break;
                default:
                    continue;
            }

            double itemCost = unitCost * qty;
            totalCost += itemCost;

            result.append(String.format("- **Unit Cost**: $%.2f\n", unitCost));
            result.append(String.format("- **Current Total**: $%.2f\n", itemCost));

            if (qty >= bulkThreshold) {
                double discountedCost = unitCost * (1 - bulkDiscount) * qty;
                optimizedCost += discountedCost;
                result.append(String.format("- ✅ **Bulk Discount Applied**: %.0f%% off\n", bulkDiscount * 100));
                result.append(String.format("- **Optimized Total**: $%.2f\n", discountedCost));
                result.append(String.format("- **Savings**: $%.2f\n\n", itemCost - discountedCost));
            } else {
                optimizedCost += itemCost;
                int neededForDiscount = bulkThreshold - qty;
                double potentialSavings = unitCost * bulkDiscount * bulkThreshold;
                result.append(String.format(
                        "- ⚠️ **Optimization Opportunity**: Order %d more units to get %.0f%% discount\n",
                        neededForDiscount, bulkDiscount * 100));
                result.append(String.format("- **Potential Savings**: $%.2f\n\n", potentialSavings));
            }
        }

        result.append("## Optimization Summary\n\n");
        result.append(String.format("- **Original Cost**: $%.2f\n", totalCost));
        result.append(String.format("- **Optimized Cost**: $%.2f\n", optimizedCost));
        result.append(String.format("- **Total Savings**: $%.2f\n", totalCost - optimizedCost));
        result.append(
                String.format("- **Cost Reduction**: %.1f%%\n\n", ((totalCost - optimizedCost) / totalCost) * 100));

        result.append("## Recommendations\n\n");
        result.append("1. **Consolidate Orders**: Combine orders to reach bulk discount thresholds\n");
        result.append("2. **Timing**: Place orders early in the week for faster delivery\n");
        result.append("3. **Payment**: Use Net 30 terms to preserve cash flow\n");
        result.append("4. **Shipping**: Orders over $2,000 get free shipping - optimize accordingly\n");
        result.append("5. **Relationships**: Regular orders may qualify for additional supplier discounts\n\n");

        result.append("## Next Steps\n");
        result.append("- Review and approve optimized order\n");
        result.append("- Contact supplier to confirm availability\n");
        result.append("- Schedule delivery to match expected demand\n");

        return result.toString();
    }
}
