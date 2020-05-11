package com.perficient.msday2020.simulator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("simulator")
public class SimulatorConfig {

	private String id;
	private long rate;
	private String url;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public long getRate() {
		return rate;
	}
	
	public void setRate(long rate) {
		this.rate = rate;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
}
