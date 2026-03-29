package com.example.java_ai_function_callback;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("Weather API request")
public class WeatherRequest {

	@JsonPropertyDescription("The city and state e.g. San Francisco, CA")
	private String location = "";

	@JsonPropertyDescription("The city latitude")
	private double lat = 0.0;

	@JsonPropertyDescription("The city longitude")
	private double lon = 0.0;

	@JsonPropertyDescription("Temperature unit")
	private SpringAiJavaFunctionCallbackApplication.Unit unit = SpringAiJavaFunctionCallbackApplication.Unit.C;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public SpringAiJavaFunctionCallbackApplication.Unit getUnit() {
		return unit;
	}

	public void setUnit(SpringAiJavaFunctionCallbackApplication.Unit unit) {
		this.unit = unit;
	}

}

