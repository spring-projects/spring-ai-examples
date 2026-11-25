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
 * Sales Trend Analysis Skill
 *
 * <p>Provides hot-selling item trends and sales data analysis
 *
 * @author Semir
 */
@Skill(
        name = "trend",
        description = "Sales trend analysis system for tracking popular items and market demand",
        source = "example",
        extensions = {"version=1.0.0", "category=retail"})
public class TrendSkill {

    private static final Logger logger = LoggerFactory.getLogger(TrendSkill.class);

    private TrendSkill() {}

    @SkillInit
    public static TrendSkill create() {
        return new TrendSkill();
    }

    @SkillContent
    public String content() {
        return """
            # Sales Trend Analysis Skill

            Analyze sales trends and market demand for clothing items.

            ## Features

            - View hot-selling items
            - Track sales velocity
            - Analyze seasonal trends
            - Predict demand patterns

            ## Available Tools

            - `getSalesTrends` - Get current sales trends and popular items
            - `getPredictedDemand` - Get demand predictions for upcoming period

            ## Usage

            Ask questions like:
            - "What items are trending this week?"
            - "Show me sales trends for winter items"
            - "What's the predicted demand for next month?"
            """;
    }

    @SkillTools
    public List<ToolCallback> tools() {
        return List.of(ToolCallbacks.from(this));
    }

    @Tool(
            description =
                    "Get sales trends and popular items. Use period parameter to specify timeframe ('week', 'month', 'season'). Returns trending items, sales velocity, and market demand indicators.")
    public String getSalesTrends(String period) {
        logger.info("getSalesTrends called with period={}", period);
        System.out.println(String.format("[TOOL] getSalesTrends called with period=%s", period));

        StringBuilder result = new StringBuilder();
        result.append("# Sales Trends Report\n\n");
        result.append(String.format("**Period**: Past %s\n", period));
        result.append(String.format("**Report Date**: %s\n\n", java.time.LocalDate.now()));

        result.append("## Top Selling Items\n\n");

        result.append("### 1. 🔥 Wool Sweaters (SWTR-001)\n");
        result.append("- **Units Sold**: 85 units\n");
        result.append("- **Sales Velocity**: 12 units/day\n");
        result.append("- **Revenue**: $5,525\n");
        result.append("- **Trend**: ⬆️ +35% vs last period\n");
        result.append("- **Reason**: Cold weather surge\n\n");

        result.append("### 2. 🔥 Winter Coats (COAT-001)\n");
        result.append("- **Units Sold**: 62 units\n");
        result.append("- **Sales Velocity**: 9 units/day\n");
        result.append("- **Revenue**: $7,440\n");
        result.append("- **Trend**: ⬆️ +45% vs last period\n");
        result.append("- **Reason**: Winter season peak\n\n");

        result.append("### 3. Jeans (JEAN-001)\n");
        result.append("- **Units Sold**: 48 units\n");
        result.append("- **Sales Velocity**: 7 units/day\n");
        result.append("- **Revenue**: $3,840\n");
        result.append("- **Trend**: ➡️ Stable\n");
        result.append("- **Reason**: Consistent demand\n\n");

        result.append("### 4. Dresses (DRSS-001)\n");
        result.append("- **Units Sold**: 35 units\n");
        result.append("- **Sales Velocity**: 5 units/day\n");
        result.append("- **Revenue**: $3,325\n");
        result.append("- **Trend**: ⬇️ -15% vs last period\n");
        result.append("- **Reason**: Off-season\n\n");

        result.append("### 5. Shirts (SHRT-001)\n");
        result.append("- **Units Sold**: 28 units\n");
        result.append("- **Sales Velocity**: 4 units/day\n");
        result.append("- **Revenue**: $1,260\n");
        result.append("- **Trend**: ➡️ Stable\n");
        result.append("- **Reason**: Basic item\n\n");

        result.append("## Market Insights\n");
        result.append("- **Hot Categories**: Winter wear (coats, sweaters)\n");
        result.append("- **Peak Shopping Days**: Weekends and Fridays\n");
        result.append("- **Average Transaction**: $185\n");
        result.append("- **Customer Preference**: Quality over quantity\n\n");

        result.append("## Recommendations\n");
        result.append("- ⚠️ **Action Required**: Restock winter coats immediately (high demand)\n");
        result.append("- ✅ **Sweater stock is adequate** but monitor closely\n");
        result.append("- 📊 **Jeans need urgent restocking** (critical low inventory)\n");
        result.append("- 💡 **Consider promotion** on dresses to boost off-season sales\n");

        return result.toString();
    }

    @Tool(
            description =
                    "Get demand predictions for the next period. Use category parameter to filter predictions by clothing type (e.g., 'winter', 'casual', 'formal', 'all'). Returns forecasted demand, recommended stock levels, and confidence scores.")
    public String getPredictedDemand(String category) {
        logger.info("getPredictedDemand called with category={}", category);
        System.out.println(String.format("[TOOL] getPredictedDemand called with category=%s", category));

        StringBuilder result = new StringBuilder();
        result.append("# Demand Prediction Report\n\n");
        result.append(String.format("**Category**: %s\n", category));
        result.append("**Forecast Period**: Next 30 days\n");
        result.append(String.format("**Report Date**: %s\n\n", java.time.LocalDate.now()));

        if (category.equalsIgnoreCase("all")
                || category.toLowerCase().contains("winter")
                || category.toLowerCase().contains("coat")) {
            result.append("## Winter Coats (COAT-001)\n");
            result.append("- **Predicted Demand**: 90-110 units\n");
            result.append("- **Current Stock**: 15 units\n");
            result.append("- **Recommended Order**: 80-100 units\n");
            result.append("- **Confidence**: 92% (High)\n");
            result.append("- **Factors**: Peak winter season, historical data\n");
            result.append("- **Risk**: ⚠️ High - Stock-out likely within 2 days\n\n");
        }

        if (category.equalsIgnoreCase("all")
                || category.toLowerCase().contains("winter")
                || category.toLowerCase().contains("sweater")) {
            result.append("## Sweaters (SWTR-001)\n");
            result.append("- **Predicted Demand**: 120-140 units\n");
            result.append("- **Current Stock**: 45 units\n");
            result.append("- **Recommended Order**: 80-100 units\n");
            result.append("- **Confidence**: 88% (High)\n");
            result.append("- **Factors**: Cold weather continuation\n");
            result.append("- **Risk**: ⚠️ Medium - Stock sufficient for 4 days\n\n");
        }

        if (category.equalsIgnoreCase("all")
                || category.toLowerCase().contains("casual")
                || category.toLowerCase().contains("jeans")) {
            result.append("## Jeans (JEAN-001)\n");
            result.append("- **Predicted Demand**: 75-85 units\n");
            result.append("- **Current Stock**: 8 units\n");
            result.append("- **Recommended Order**: 70-80 units\n");
            result.append("- **Confidence**: 85% (High)\n");
            result.append("- **Factors**: Consistent year-round demand\n");
            result.append("- **Risk**: 🔴 Critical - Stock-out imminent (1 day)\n\n");
        }

        if (category.equalsIgnoreCase("all")
                || category.toLowerCase().contains("formal")
                || category.toLowerCase().contains("dress")) {
            result.append("## Dresses (DRSS-001)\n");
            result.append("- **Predicted Demand**: 40-50 units\n");
            result.append("- **Current Stock**: 25 units\n");
            result.append("- **Recommended Order**: 20-30 units\n");
            result.append("- **Confidence**: 75% (Medium)\n");
            result.append("- **Factors**: Off-season, special events\n");
            result.append("- **Risk**: ✅ Low - Stock sufficient for 2 weeks\n\n");
        }

        if (category.equalsIgnoreCase("all")
                || category.toLowerCase().contains("casual")
                || category.toLowerCase().contains("shirt")) {
            result.append("## Shirts (SHRT-001)\n");
            result.append("- **Predicted Demand**: 50-60 units\n");
            result.append("- **Current Stock**: 12 units\n");
            result.append("- **Recommended Order**: 40-50 units\n");
            result.append("- **Confidence**: 82% (High)\n");
            result.append("- **Factors**: Basic wardrobe item\n");
            result.append("- **Risk**: ⚠️ Medium - Stock sufficient for 3 days\n\n");
        }

        result.append("## Overall Forecast Summary\n");
        result.append("- **Total Predicted Sales**: $32,000-$38,000\n");
        result.append("- **Recommended Investment**: $18,000-$22,000\n");
        result.append("- **Expected ROI**: 75-85%\n\n");

        result.append("## Strategic Recommendations\n");
        result.append("1. **Priority 1 (Urgent)**: Order jeans immediately to avoid stock-out\n");
        result.append("2. **Priority 2 (High)**: Restock winter coats within 48 hours\n");
        result.append("3. **Priority 3 (Medium)**: Order sweaters for next week\n");
        result.append("4. **Priority 4 (Low)**: Monitor shirt sales, order if needed\n");
        result.append("5. **Opportunity**: Dresses have low demand - consider promotional strategy\n");

        return result.toString();
    }
}
