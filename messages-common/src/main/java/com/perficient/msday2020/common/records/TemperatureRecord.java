package com.perficient.msday2020.common.records;

public class TemperatureRecord extends ThermostatRecord {
	
	int temperature;
	
	double efficiency;
	
	public TemperatureRecord() {
	}
	
	public TemperatureRecord(String deviceId, int temperature) {
		super(deviceId);
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
}
