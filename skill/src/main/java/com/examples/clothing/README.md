# Clothing Store Purchase Assistant Example

A complete clothing store purchase decision scenario demonstrating how to build **real-world AI intelligent assistants** using the Spring AI Skill Framework.

## Scenario Overview

This example implements an **Intelligent Clothing Store Purchase Assistant**. Store owners engage in **natural language conversations**, and the AI automatically invokes appropriate tools to analyze inventory, trends, costs, and provides optimized purchase recommendations.

### Why This Scenario?

Compared to generic scenarios like travel planning, clothing store purchase decisions offer several advantages:

1. **Strong Data Specificity** - Uses fixed simulated data (inventory, prices, suppliers), less likely to trigger the LLM's general knowledge
2. **Clear Business Logic** - Purchase decisions require synthesizing multiple data sources, demonstrating Skills collaboration
3. **High Practicality** - Real business scenario, easy to understand and extend
4. **Good Verifiability** - Fixed data facilitates verification of tool execution

### Core Features

✅ **Real AI Conversations** - Integrated with ChatClient, supports streaming responses
✅ **Intelligent Tool Invocation** - AI automatically selects and calls appropriate Skills
✅ **Multi-dimensional Analysis** - Synthesizes inventory, trends, costs, weather and other factors
✅ **Fixed Simulated Data** - Facilitates verification and debugging
✅ **Professional Recommendations** - Provides ROI analysis and priority ranking

### Business Workflow

```
Store owner needs to restock
    ↓
1. InventorySkill - Check current inventory status
    ↓
2. TrendSkill - Analyze sales trends and popular items
    ↓
3. WeatherSkill - Check weather (affects seasonal items)
    ↓
4. SupplierSkill - Query supplier products and costs
    ↓
5. PricingSkill - Analyze profit margins and pricing
    ↓
6. PurchaseStrategySkill - Generate intelligent purchase recommendations
```

## Skills Overview

### 1. InventorySkill (Inventory Management)

**Registration Method**: `registerFromInstance`

**Features**:
- View current inventory status
- Query inventory by category
- Identify low stock and out-of-stock risks
- Provide inventory summary reports

**Tools**:
- `checkInventory(category)` - Query specified category or all inventory

**Fixed Data**:
- Winter Coats: 15 units (⚠️ Low Stock)
- Sweaters: 45 units (✅ Good Stock)
- Jeans: 8 units (🔴 Critical)
- Dresses: 25 units (✅ Good Stock)
- Shirts: 12 units (⚠️ Low Stock)

### 2. PricingSkill (Price Management)

**Registration Method**: `registerFromInstance`

**Features**:
- Query product retail prices
- Calculate costs and profit margins
- Analyze pricing competitiveness
- Provide pricing recommendations

**Tools**:
- `getPricing(skuOrCategory)` - Get pricing information and profit analysis

**Fixed Data**:
- Winter Coats: $120 retail, $75 cost, 37.5% margin
- Sweaters: $65 retail, $38 cost, 41.5% margin
- Jeans: $80 retail, $45 cost, 43.75% margin
- Dresses: $95 retail, $52 cost, 45.3% margin
- Shirts: $45 retail, $25 cost, 44.4% margin

### 3. SupplierSkill (Supplier Management)

**Registration Method**: `registerFromClass` (lazy loading)

**Features**:
- Browse supplier product catalog
- Query wholesale costs
- Understand minimum order quantities
- Calculate bulk discounts

**Tools**:
- `getSupplierCatalog(category)` - Get supplier catalog
- `getSupplierQuote(supplierSku, quantity)` - Get quotation

**Fixed Data**:
- SUP-COAT-W01: $75/unit, min 10, bulk 5% off at 50+
- SUP-SWTR-W01: $38/unit, min 20, bulk 8% off at 100+
- SUP-JEAN-C01: $45/unit, min 15, bulk 7% off at 50+
- SUP-SHRT-C01: $25/unit, min 20, bulk 10% off at 100+
- SUP-DRSS-F01: $52/unit, min 12, bulk 6% off at 40+

### 4. TrendSkill (Sales Trends)

**Registration Method**: `registerFromInstance`

**Features**:
- Analyze hot-selling items
- Track sales velocity
- Forecast future demand
- Identify market trends

**Tools**:
- `getSalesTrends(period)` - Get sales trend reports
- `getPredictedDemand(category)` - Get demand forecasts

**Fixed Data**:
- Sweaters: 85 units sold, +35% trend (Hot!)
- Winter Coats: 62 units sold, +45% trend (Hot!)
- Jeans: 48 units sold, stable
- Dresses: 35 units sold, -15% trend
- Shirts: 28 units sold, stable

### 5. WeatherSkill (Weather Query)

**Registration Method**: `registerFromClass` (reuses existing)

**Features**:
- Query city weather
- Influence seasonal product recommendations

**Tools**:
- `getWeather(city)` - Get weather information

### 6. PurchaseStrategySkill (Purchase Strategy)

**Registration Method**: `registerFromClass` (lazy loading)

**Features**:
- Generate intelligent purchase recommendations
- Optimize budget allocation
- ROI analysis and forecasting
- Risk assessment
- Priority ranking

**Tools**:
- `generatePurchaseStrategy(budget, priority)` - Generate purchase strategy
- `optimizePurchaseOrder(items, quantities)` - Optimize orders

## Running the Example

```bash
mvn exec:java -pl springai-skill-framework-examples \
  -Dexec.mainClass="clothing.examples.com.semir.spring.ai.skill.ClothingStoreExample"
```

## Usage Examples

After starting, you can converse with the AI like this:

### Example Conversation 1: Check Inventory

```
🧑 You: Show me current inventory status

🤖 Assistant: [AI automatically calls checkInventory tool]
Based on the current inventory report, here's the status:

**Critical Items** 🔴:
- Jeans (JEAN-001): Only 8 units left - URGENT restock needed!

**Low Stock Items** ⚠️:
- Winter Coats (COAT-001): 15 units - needs restock soon
- Shirts (SHRT-001): 12 units - monitor closely

**Good Stock** ✅:
- Sweaters (SWTR-001): 45 units
- Dresses (DRSS-001): 25 units

I recommend prioritizing Jeans for immediate restocking.
```

### Example Conversation 2: Analyze Sales Trends

```
🧑 You: What items are trending this week?

🤖 Assistant: [AI calls getSalesTrends tool]
Here are the hot-selling items this week:

🔥 **Top Performers**:
1. Wool Sweaters - 85 units sold (+35% growth!)
2. Winter Coats - 62 units sold (+45% growth!)

These winter items are in peak demand. With the cold weather,
customers are buying heavily. Combined with your low stock on coats,
I strongly recommend restocking both items immediately.
```

### Example Conversation 3: Generate Purchase Strategy

```
🧑 You: I have a $10,000 budget. What should I buy?

🤖 Assistant: [AI comprehensively calls multiple tools: checkInventory, getSalesTrends, getSupplierCatalog, generatePurchaseStrategy]
Based on comprehensive analysis, here's your optimized purchase strategy:

**Priority 1 (URGENT)** 🔴:
- Jeans: 70 units at $41.85/unit = $2,929.50
- Expected ROI: 91%
- Reason: Critical stock-out within 1 day

**Priority 2 (HIGH)** ⚠️:
- Winter Coats: 50 units at $71.25/unit = $3,562.50
- Expected ROI: 68%
- Reason: Hot seller (+45%), low stock

**Priority 3 (MEDIUM)**:
- Sweaters: 100 units at $34.96/unit = $3,496.00
- Expected ROI: 86%
- Reason: Top seller (+35%), qualify for 8% bulk discount

**Total Investment**: $9,988.00
**Budget Remaining**: $12.00
**Projected Revenue**: $18,100
**Projected Profit**: $8,112 (81% ROI)

This strategy maximizes your ROI while addressing critical stock issues.
```

### Example Conversation 4: Consider Weather Factors

```
🧑 You: Check the weather in New York and suggest what to stock

🤖 Assistant: [AI calls getWeather and getPredictedDemand]
New York Weather: ❄️ 5°C, Snowy, Humidity 78%

With this cold, snowy weather, I recommend:
- **Priority**: Winter Coats and Sweaters (high demand expected)
- **Avoid**: Dresses and light items (off-season)
- **Moderate**: Basic items like Jeans (consistent demand)

Cold weather creates urgency for warm clothing. Stock up on
winter items now to capture this demand spike.
```

## Special Commands

- `exit` or `quit` - Exit the program
- `/stats` - View framework statistics

## Architecture Design Highlights

### 1. Diverse Registration Methods

```java
// Instance-based registration (eager loading, frequently accessed)
skillKit.registerFromInstance(InventorySkill.create());
skillKit.registerFromInstance(PricingSkill.create());
skillKit.registerFromInstance(TrendSkill.create());

// Class-based registration (lazy loading, on-demand)
skillKit.registerFromClass(SupplierSkill.class);
skillKit.registerFromClass(PurchaseStrategySkill.class);
skillKit.registerFromClass(WeatherSkillPojo.class);
```

### 2. Fixed Data Design

All Skills use fixed simulated data, facilitating:
- ✅ Verification of correct tool execution
- ✅ Debugging and testing
- ✅ Avoiding LLM's use of general knowledge
- ✅ Ensuring reproducible results

### 3. Real Business Scenario

Complete purchase decision workflow:
- **Data Collection** - Inventory, trends, prices
- **Analysis & Decision** - ROI, priority, risk
- **Execution Recommendations** - Specific orders, quantities, costs

### 4. Intelligent Tool Collaboration

AI automatically selects appropriate tool combinations based on conversation:
- Ask about inventory → InventorySkill
- Ask about trends → TrendSkill
- Need purchase recommendations → Calls multiple Skills (Inventory + Trend + Supplier + Purchase)
- Consider weather → WeatherSkill + PredictedDemand

## Extension Suggestions

### Add More Skills

1. **CustomerFeedbackSkill** - Analyze customer reviews and feedback
2. **CompetitorSkill** - Competitor price and product analysis
3. **SeasonalPlanningSkill** - Seasonal product planning
4. **PromotionSkill** - Promotional activity recommendations
5. **ReturnAnalysisSkill** - Return rate analysis

### Integrate Real Data

Can replace fixed data with:
- Database connections (real inventory)
- ERP system integration
- Real-time sales data
- Real supplier APIs

## Related Examples

- **TravelPlanningExample** - Travel planning scenario (AI conversation)
- **SkillKitExample** - Data analysis scenario (AI conversation)
- **BasicUsageExample** - Basic framework usage

## Technical Summary

This example demonstrates:
✓ Professional scenario Skill design approach
✓ Fixed data usage and verification
✓ Multi-Skill intelligent collaboration patterns
✓ Real business decision workflows
✓ AI-driven tool selection and invocation
✓ Complete error handling and user experience
