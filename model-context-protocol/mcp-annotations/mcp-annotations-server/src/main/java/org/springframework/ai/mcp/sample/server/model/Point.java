package org.springframework.ai.mcp.sample.server.model;

import org.springaicommunity.mcp.annotation.McpToolParam;

public class Point {
	
	@McpToolParam(description = "The latitude of the location", required = true) 
	private double latitude;
	@McpToolParam(description = "The longitude of the location", required = true) 
	private double longitude;
	
	
	public Point() {}
	public Point(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
}
