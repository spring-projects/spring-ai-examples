/* 
* Copyright 2025 - 2025 the original author or authors.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* https://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springframework.ai.mcp.sample.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.logaritex.mcp.annotation.McpResource;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

import org.springframework.stereotype.Service;

/**
 * @author Christian Tzolov
 */
@Service
public class UserProfileResourceProvider {
	
	private final Map<String, Map<String, String>> userProfiles = new HashMap<>();

	public UserProfileResourceProvider() {
		// Initialize with some sample data
		Map<String, String> johnProfile = new HashMap<>();
		johnProfile.put("name", "John Smith");
		johnProfile.put("email", "john.smith@example.com");
		johnProfile.put("age", "32");
		johnProfile.put("location", "New York");

		Map<String, String> janeProfile = new HashMap<>();
		janeProfile.put("name", "Jane Doe");
		janeProfile.put("email", "jane.doe@example.com");
		janeProfile.put("age", "28");
		janeProfile.put("location", "London");

		Map<String, String> bobProfile = new HashMap<>();
		bobProfile.put("name", "Bob Johnson");
		bobProfile.put("email", "bob.johnson@example.com");
		bobProfile.put("age", "45");
		bobProfile.put("location", "Tokyo");

		Map<String, String> aliceProfile = new HashMap<>();
		aliceProfile.put("name", "Alice Brown");
		aliceProfile.put("email", "alice.brown@example.com");
		aliceProfile.put("age", "36");
		aliceProfile.put("location", "Sydney");

		userProfiles.put("john", johnProfile);
		userProfiles.put("jane", janeProfile);
		userProfiles.put("bob", bobProfile);
		userProfiles.put("alice", aliceProfile);
	}

	/**
	 * Resource method that takes a ReadResourceRequest parameter and URI variable.
	 */
	@McpResource(uri = "user-profile://{username}", name = "User Profile", description = "Provides user profile information for a specific user")
	public ReadResourceResult getUserProfile(ReadResourceRequest request, String username) {
		String profileInfo = formatProfileInfo(userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>()));

		return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", profileInfo)));
	}

	/**
	 * Resource method that takes URI variables directly as parameters. The URI
	 * template in the annotation defines the variables that will be extracted.
	 */
	@McpResource(uri = "user-profile://{username}", name = "User Details", description = "Provides user details for a specific user using URI variables")
	public ReadResourceResult getUserDetails(String username) {
		String profileInfo = formatProfileInfo(userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>()));

		return new ReadResourceResult(
				List.of(new TextResourceContents("user-profile://" + username, "text/plain", profileInfo)));
	}

	/**
	 * Resource method that takes multiple URI variables as parameters.
	 */
	@McpResource(uri = "user-attribute://{username}/{attribute}", name = "User Attribute", description = "Provides a specific attribute from a user's profile")
	public ReadResourceResult getUserAttribute(String username, String attribute) {
		Map<String, String> profile = userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>());
		String attributeValue = profile.getOrDefault(attribute, "Attribute not found");

		return new ReadResourceResult(
				List.of(new TextResourceContents("user-attribute://" + username + "/" + attribute, "text/plain",
						username + "'s " + attribute + ": " + attributeValue)));
	}

	/**
	 * Resource method that takes an exchange and URI variables.
	 */
	@McpResource(uri = "user-profile-exchange://{username}", name = "User Profile with Exchange", description = "Provides user profile information with server exchange context")
	public ReadResourceResult getProfileWithExchange(McpSyncServerExchange exchange, String username) {
		String profileInfo = formatProfileInfo(userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>()));

		return new ReadResourceResult(List.of(new TextResourceContents("user-profile-exchange://" + username,
				"text/plain", "Profile with exchange for " + username + ": " + profileInfo)));
	}

	/**
	 * Resource method that takes a String URI variable parameter.
	 */
	@McpResource(uri = "user-connections://{username}", name = "User Connections", description = "Provides a list of connections for a specific user")
	public List<String> getUserConnections(String username) {
		// Generate a simple list of connections based on username
		return List.of(username + " is connected with Alice", username + " is connected with Bob",
				username + " is connected with Charlie");
	}

	/**
	 * Resource method that takes both McpSyncServerExchange, ReadResourceRequest
	 * and
	 * URI variable parameters.
	 */
	@McpResource(uri = "user-notifications://{username}", name = "User Notifications", description = "Provides notifications for a specific user")
	public List<ResourceContents> getUserNotifications(McpSyncServerExchange exchange, ReadResourceRequest request,
			String username) {
		// Generate notifications based on username
		String notifications = generateNotifications(username);

		return List.of(new TextResourceContents(request.uri(), "text/plain", notifications));
	}

	/**
	 * Resource method that returns a single ResourceContents with TEXT content
	 * type.
	 */
	@McpResource(uri = "user-status://{username}", name = "User Status", description = "Provides the current status for a specific user")
	public ResourceContents getUserStatus(ReadResourceRequest request, String username) {
		// Generate a simple status based on username
		String status = generateUserStatus(username);

		return new TextResourceContents(request.uri(), "text/plain", status);
	}

	/**
	 * Resource method that returns a single String with TEXT content type.
	 */
	@McpResource(uri = "user-location://{username}", name = "User Location", description = "Provides the current location for a specific user")
	public String getUserLocation(String username) {
		Map<String, String> profile = userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>());

		// Extract location from profile data
		return profile.getOrDefault("location", "Location not available");
	}

	/**
	 * Resource method that returns a single String with BLOB content type. This
	 * demonstrates how a String can be treated as binary data.
	 */
	@McpResource(uri = "user-avatar://{username}", name = "User Avatar", description = "Provides a base64-encoded avatar image for a specific user", mimeType = "image/png")
	public String getUserAvatar(ReadResourceRequest request, String username) {
		// In a real implementation, this would be a base64-encoded image
		// For this example, we're just returning a placeholder string
		return "base64-encoded-avatar-image-for-" + username;
	}

	private String formatProfileInfo(Map<String, String> profile) {
		if (profile.isEmpty()) {
			return "User profile not found";
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : profile.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}
		return sb.toString().trim();
	}

	private String generateNotifications(String username) {
		// Simple logic to generate notifications
		return "You have 3 new messages\n" + "2 people viewed your profile\n" + "You have 1 new connection request";
	}

	private String generateUserStatus(String username) {
		// Simple logic to generate a status
		if (username.equals("john")) {
			return "🟢 Online";
		} else if (username.equals("jane")) {
			return "🟠 Away";
		} else if (username.equals("bob")) {
			return "⚪ Offline";
		} else if (username.equals("alice")) {
			return "🔴 Busy";
		} else {
			return "⚪ Offline";
		}
	}

}
