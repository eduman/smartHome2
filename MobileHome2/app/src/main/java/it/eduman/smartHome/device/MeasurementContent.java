package it.eduman.smartHome.device;

import java.util.Date;

public class MeasurementContent {
	private String measurementId;
	private Date measurementDate;
	private String value;
	private String measure;
	private String deviceID;

	public MeasurementContent(Date measurementDate, String value, String measure){
		this.measurementDate = measurementDate;
		this.value = value;
		this.measure = measure;
	}

	public String getMeasurementId() {
		return measurementId;
	}

	public MeasurementContent setMeasurementId(String measurementId) {
		this.measurementId = measurementId;
		return this;
	}

	public Date getMeasurementDate() {
		return measurementDate;
	}

	public MeasurementContent setMeasurementDate(Date measurementDate) {
		this.measurementDate = measurementDate;
		return this;
	}

	public String getValue() {
		return value;
	}

	public MeasurementContent setValue(String value) {
		this.value = value;
		return this;
	}

	public String getMeasure() {
		return measure;
	}

	public MeasurementContent setMeasure(String measure) {
		this.measure = measure;
		return this;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public MeasurementContent setDeviceID(String deviceID) {
		this.deviceID = deviceID;
		return this;
	}
	

}
