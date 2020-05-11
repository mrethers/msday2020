package com.perficient.msday2020.bridge.records;

import java.util.Date;

public class TemperatureRecord {
	
	private String deviceId;
	
	private Date recordedDate = new Date();
	
	private int temperature;
	
	private double efficiency;
	
	public TemperatureRecord() {
	}
	
	public TemperatureRecord(String deviceId, int temperature) {
		this.deviceId = deviceId;
		this.temperature = temperature;
	}
	
	public int getTemperature() {
		return temperature;
	}
	
	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}
	
	public void setEfficiency(double efficiency) {
		this.efficiency = efficiency;
	}
	
	public double getEfficiency() {
		return efficiency;
	}
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	
	public Date getRecordedDate() {
		return recordedDate;
	}
}
