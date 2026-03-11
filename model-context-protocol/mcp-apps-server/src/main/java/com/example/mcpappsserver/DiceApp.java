package com.example.mcpappsserver;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.context.MetaProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Service
public class DiceApp {

  //
  // MCP Resource - Serves the app's UI
  //
  @Value("classpath:/app/dice-app.html")
  private Resource diceAppResource;

  @McpResource(name = "Dice App Resource",
      uri = "ui://dice/dice-app.html",
      mimeType = "text/html;profile=mcp-app",
      metaProvider = CspMetaProvider.class)
  public String getDiceAppResource() throws IOException {
    return diceAppResource.getContentAsString(Charset.defaultCharset());
  }

  public static final class CspMetaProvider implements MetaProvider {
    @Override
    public Map<String, Object> getMeta() {
      return Map.of("ui",
          Map.of("csp",
              Map.of("resourceDomains",
                  List.of("https://unpkg.com"))));
    }
  }

  //
  // MCP Tool - References the resource
  //
  @McpTool(
      title = "Roll the Dice",
      name = "roll-the-dice",
      description = "Rolls the dice",
      metaProvider = DiceMetaProvider.class)
  public String rollTheDice() {
    return "Opening dice roller app.";
  }

  public static final class DiceMetaProvider implements MetaProvider {
    @Override
    public Map<String, Object> getMeta() {
      return Map.of("ui",
          Map.of(
              "resourceUri", "ui://dice/dice-app.html"));
    }
  }

}
