package it.eduman.smartHome.userMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageContent {
	public static final int GENERIC_INFO_CODE = 0;
	public static final int HOME_CODE = 1;
	public static final int LIGHT_CODE = 2;
	public static final int HEATING_CODE = 3;
	public static final int COOLING_CODE = 4;
//	public static final int DEVICE_CODE = 5;
	
	private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
	private Long id;
	private String text;
	private int code = GENERIC_INFO_CODE;
	private Date timestamp;
	private long userID;
	
	public MessageContent (String text, int code, Date timestamp){
		this.text = text;
		this.code = code;
		this.timestamp = timestamp;
	}

	public Long getId() {
		return id;
	}

	public MessageContent setId(Long id) {
		this.id = id;
		return this;
	}

	public String getText() {
		return text;
	}

	public MessageContent setText(String text) {
		this.text = text;
		return this;
	}

	public int getCode() {
		return code;
	}

	public MessageContent setCode(int code) {
		this.code = code;
		return this;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public MessageContent setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	
	public String getFormattedTimeStamp(){
		SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return sdf.format(this.timestamp); 
	}
	
	public String getFormattedTimeStamp(String dateFormat){
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(this.timestamp); 
	}
	
	public long getUserID() {
		return userID;
	}

	public MessageContent setUserID(long userID) {
		this.userID = userID;
		return this;
	}
	
}
