# Fashion Guide Skill - References Feature Demo

## Overview

In the Clothing Store Example, we added the **Fashion Guide Skill**, which demonstrates how to use the `@SkillReferences` annotation to provide reference materials for Skills.

## Features

### Fashion Guide Skill Provides

1. **Quick Trend Overview** - Via `getTrendOverview` tool
2. **Detailed Reference Materials** - Document links provided through `@SkillReferences` annotation

### Reference Materials List

- `spring-trends` - Spring fashion trends report
- `summer-trends` - Summer fashion trends report
- `buying-guide` - Clothing purchasing guide
- `color-trends` - Seasonal color trends guide
- `style-guide` - Style and outfit recommendations
- `market-report` - Market analysis report
- `sustainability-guide` - Sustainable fashion guide

## Example Questions to Trigger Reference Loading

### 1. Direct Reference Inquiry

```
🧑 You: Show me the spring trends report

🤖 Assistant:
[AI first calls getTrendOverview for overview]
[Then calls loadSkillReference("fashion-guide", "spring-trends")]
[Returns link: https://fashion-industry.com/reports/2025-spring-summer-trends.pdf]
```

### 2. Buying Guide Inquiry

```
🧑 You: Give me the buying guide link

🤖 Assistant:
[AI calls loadSkillReference("fashion-guide", "buying-guide")]
[Returns link: https://fashion-industry.com/guides/clothing-buyer-handbook.pdf]
```

### 3. Color Trends Inquiry

```
🧑 You: Where can I find the color trends document?

🤖 Assistant:
[AI calls loadSkillReference("fashion-guide", "color-trends")]
[Returns link: https://fashion-industry.com/trends/2025-color-palette.pdf]
```

### 4. Comprehensive Query

```
🧑 You: What are the current fashion trends? Also give me the detailed report.

🤖 Assistant:
[AI first calls getTrendOverview to show quick overview]
[Then calls loadSkillReference("fashion-guide", "spring-trends") to provide detailed report link]
```

## Running the Example

```bash
cd springai-skill-framework-examples
mvn compile exec:java -Dexec.mainClass="clothing.examples.com.semir.spring.ai.skill.ClothingStoreExample"
```

## How It Works

### 1. Skill Definition

```java
@Skill(name = "fashion-guide", description = "...", source = "example")
public class FashionGuideSkill {

    @SkillReferences
    public Map<String, String> references() {
        Map<String, String> refs = new HashMap<>();
        refs.put("spring-trends", "https://fashion-industry.com/reports/2025-spring-summer-trends.pdf");
        refs.put("buying-guide", "https://fashion-industry.com/guides/clothing-buyer-handbook.pdf");
        // ... more references
        return refs;
    }
}
```

### 2. LLM Invocation Flow

1. **User Question**: "Show me the spring trends report"
2. **AI Intent Recognition**: Need to load fashion-guide's spring-trends reference
3. **Tool Invocation**: `loadSkillReference("fashion-guide", "spring-trends")`
4. **Framework Processing**:
   - Check if fashion-guide skill exists
   - Check if it supports ReferencesLoader
   - Call `skill.as(ReferencesLoader.class).getReferences()`
   - Return the link for `spring-trends`
5. **Return Result**: "https://fashion-industry.com/reports/2025-spring-summer-trends.pdf"

### 3. System Prompt Guidance

The system prompt explicitly tells the AI how to use references:

```
**IMPORTANT: Fashion Guide Skill has Reference Materials**
When users ask about:
- "spring trends report" or "summer trends"
- "buying guide" or "color trends"
- "style guide" or any fashion reference materials

You should:
1. First use getTrendOverview tool for a quick summary
2. Then use loadSkillReference tool to get the actual document link
   Example: loadSkillReference("fashion-guide", "spring-trends")
3. Provide the reference link to the user
```

## Technical Details

### Automatic References Support

Because `FashionGuideSkill` uses the `@SkillReferences` annotation, the framework automatically:

1. **Annotation Scanning**: `AnnotationDrivenSkill` scans `@SkillReferences` methods
2. **Dynamic Proxy**: Creates dynamic proxy for `ReferencesLoader` interface
3. **Support Check**: `skill.supports(ReferencesLoader.class)` returns `true`
4. **Capability Conversion**: `skill.as(ReferencesLoader.class)` returns proxy instance

### SimpleSkillLoaderTool

Provides two tools for LLM:

1. `loadSkillContent(skillName)` - Load Skill content
2. `loadSkillReference(skillName, referenceKey)` - Load specific reference material

## Comparison with Other Skills

| Skill | Has References | Description |
|-------|----------------|-------------|
| Inventory Management | ❌ | Provides tools, no references |
| Pricing Analysis | ❌ | Provides tools, no references |
| Supplier Catalog | ❌ | Provides tools, no references |
| Sales Trends | ❌ | Provides tools, no references |
| Weather | ❌ | Provides tools, no references |
| **Fashion Guide** | ✅ | **Provides tools + references** |
| Purchase Strategy | ❌ | Provides tools, no references |

## Best Practices

### 1. Clear Reference Key Naming

```java
// ✅ Good naming
refs.put("spring-trends", "...");
refs.put("buying-guide", "...");
refs.put("color-trends", "...");

// ❌ Poor naming
refs.put("ref1", "...");
refs.put("doc", "...");
```

### 2. Document in Skill Content

```java
@SkillContent
public String content() {
    return """
            ## Reference Materials Available
            - spring-trends: Spring/Summer fashion trend report
            - buying-guide: Clothing buying guide
            ...
            """;
}
```

### 3. System Prompt Guidance

Explicitly tell the AI when and how to use `loadSkillReference`.

## Summary

By adding the Fashion Guide Skill to the Clothing Store Example, we demonstrate:

1. ✅ How to use the `@SkillReferences` annotation
2. ✅ How to load reference materials via LLM tools
3. ✅ How to design questions to trigger reference loading
4. ✅ How to integrate references functionality in real scenarios

This design fully aligns with the framework's extensibility architecture, providing Skills with rich reference material support capabilities.
