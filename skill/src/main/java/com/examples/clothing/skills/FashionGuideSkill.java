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
import org.springframework.ai.skill.capability.SkillReferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Fashion Guide Skill
 *
 * <p>Provides fashion trends, industry guides, and style recommendations for clothing stores.
 * Includes extensive reference material links to help store owners stay informed about latest fashion dynamics.
 *
 * <p><b>Key Features</b>:
 * <ul>
 *   <li>Provides current season fashion trend overview</li>
 *   <li>Provides detailed reference material links (via @SkillReferences)</li>
 *   <li>Helps stores understand market dynamics</li>
 * </ul>
 *
 * <p><b>Reference Materials</b>:
 * <ul>
 *   <li>spring-trends - Spring fashion trend report</li>
 *   <li>summer-trends - Summer fashion trend report</li>
 *   <li>buying-guide - Clothing purchase guide</li>
 *   <li>color-trends - Seasonal color trend guide</li>
 *   <li>style-guide - Style and outfit recommendations</li>
 * </ul>
 *
 * @author Semir
 */
@Skill(
        name = "fashion-guide",
        description = "Provides fashion trends, industry guides, and style recommendations for clothing stores",
        source = "example")
public class FashionGuideSkill {

    @SkillContent
    public String content() {
        return """
                # Fashion Guide Skill

                I provide comprehensive industry guidance for clothing store owners.

                ## What I Can Help With

                1. **Reference Materials** - Access detailed reports and guides
                2. **Market Insights** - Stay updated with industry dynamics

                ## How to Use

                ### Access Detailed References
                I provide extensive reference materials that you can access using the `loadSkillReference` tool:

                - **spring-trends**: Detailed Spring/Summer fashion trend report
                - **summer-trends**: Summer seasonal trend analysis
                - **buying-guide**: Comprehensive clothing buying guide
                - **color-trends**: Current season's popular color palette
                - **style-guide**: Style and outfit recommendations

                ### Example Questions
                - "Show me the spring trends report" (triggers reference loading)
                - "Where can I find the buying guide?" (triggers reference loading)
                - "Give me the color trends document" (triggers reference loading)

                ## Tips
                - Check trend overview first for a quick understanding
                - Use reference materials for detailed analysis
                - Combine with inventory and sales data for better decisions
                """;
    }

    /**
     * Provides fashion reference material links
     *
     * <p>These links point to detailed fashion trend reports, buying guides, and style recommendation documents.
     * LLM can load these materials through the loadSkillReference tool.
     */
    @SkillReferences
    public Map<String, String> references() {
        Map<String, String> refs = new HashMap<>();

        // Seasonal trend reports
        refs.put("spring-trends", "https://fashion-industry.com/reports/2025-spring-summer-trends.pdf");
        refs.put("summer-trends", "https://fashion-industry.com/reports/2025-summer-seasonal-analysis.pdf");

        // Buying guide
        refs.put("buying-guide", "https://fashion-industry.com/guides/clothing-buyer-handbook.pdf");

        // Color trends
        refs.put("color-trends", "https://fashion-industry.com/trends/2025-color-palette.pdf");

        // Style guide
        refs.put("style-guide", "https://fashion-industry.com/guides/style-outfit-recommendations.pdf");

        // Industry report
        refs.put("market-report", "https://fashion-industry.com/reports/q1-2025-market-analysis.pdf");

        // Sustainable fashion guide
        refs.put("sustainability-guide", "https://fashion-industry.com/guides/sustainable-fashion-practices.pdf");

        return refs;
    }

    @SkillInit
    public static FashionGuideSkill create() {
        return new FashionGuideSkill();
    }
}
