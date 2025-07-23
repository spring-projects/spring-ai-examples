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

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * @author Christian Tzolov
 */
@Service
public class McpToolProvider {

		private static final Logger logger = org.slf4j.LoggerFactory.getLogger(McpToolProvider.class);

	private final RestClient restClient;

	public McpToolProvider() {
		this.restClient = RestClient.create();
	}

	public record WeatherResponse(Current current) {
		public record Current(LocalDateTime time, int interval, double temperature_2m) {
		}
	}

	@McpTool(description = "Get the temperature (in celsius) for a specific location")
	public WeatherResponse getTemperature(@McpToolParam(description = "The location latitude") double latitude,
			@McpToolParam(description = "The location longitude") double longitude,
			@McpToolParam(description = "The city name") String city) {

		WeatherResponse response = restClient
				.get()
				.uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
						latitude, longitude)
				.retrieve()
				.body(WeatherResponse.class);

		logger.info("Check temparature for {}. Lat: {}, Lon: {}. Temp: {}", city, latitude, longitude,
				response.current);

		return response;
	}
	
}
