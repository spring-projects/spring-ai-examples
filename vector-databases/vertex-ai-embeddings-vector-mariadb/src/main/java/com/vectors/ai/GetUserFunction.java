package com.vectors.ai;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class GetUserFunction {
    
    @Tool(name = "getUser",description = "Return the user email based on userId parameter")
    public String getUser(@ToolParam String userId) {
        // In a real application, you would fetch user data from a database or external service
        // For this example, we'll return a dummy response
        if ("123".equals(userId)) {
            return "User with ID 123 found. Name: John Doe, Email: john.doe@example.com";
        } else {
            return "User with ID " + userId + " not found.";
        }
    
 }
}
