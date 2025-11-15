/*
* Copyright 2024 - 2024 the original author or authors.
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
package com.example.agentic;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Record representing the response from the routing classification process.
 * 
 * <p>
 * This record is used by the {@link RoutingWorkflow} to
 * capture and communicate routing decisions made by the LLM classifier.
 * 
 * @param reasoning A detailed explanation of why a particular route was chosen,
 *                  considering factors like key terms, user intent, and urgency
 *                  level
 * @param selection The name of the selected route that will handle the input
 * 
 * 
 * @author Christian Tzolov
 * @see RoutingWorkflow
 */
public record RoutingResponse(

		@JsonPropertyDescription(
				"The reasoning behind the route selection, explaining why this particular " +
				"route was chosen based on the input analysis. Consider factors like key terms, " +
				"intent, and urgency level."
		)
		String reasoning,

		@JsonPropertyDescription("The selected route key")
		String selection) {
}
