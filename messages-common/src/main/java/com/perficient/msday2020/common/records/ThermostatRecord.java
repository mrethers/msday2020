package com.perficient.msday2020.common.records;

import java.util.Date;

public class ThermostatRecord {

	private String deviceId;
	
	private Date recordedDate = new Date();
	
	public ThermostatRecord() {
	}
	
	public ThermostatRecord(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public Date getRecordedDate() {
		return recordedDate;
	}
}
