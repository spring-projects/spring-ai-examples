package com.example.mcpappsserver;

import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
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

  private static final String DICE_APP_URI = "ui://dice/dice-app.html";

  private static final String DICE_APP_MIME_TYPE = "text/html;profile=mcp-app";

  @McpResource(name = "Dice App Resource",
      uri = DICE_APP_URI,
      mimeType = DICE_APP_MIME_TYPE)
  public ReadResourceResult getDiceAppResource() throws IOException {
    String html = diceAppResource.getContentAsString(Charset.defaultCharset());
    return ReadResourceResult.builder(List.of(
        TextResourceContents.builder(DICE_APP_URI, html)
            .mimeType(DICE_APP_MIME_TYPE)
            .meta(Map.of("ui",
                Map.of("csp",
                    Map.of("resourceDomains",
                        List.of("https://unpkg.com")))))
            .build()))
        .build();
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
              "resourceUri", DICE_APP_URI));
    }
  }

}
