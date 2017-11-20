package com.company.app.util;

import java.util.HashMap;

public class UtilMessages {

	public static  HashMap<String, String>  getMessage(String key, String msg){
		HashMap<String, String> map = new HashMap<String, String>();
		map = new HashMap<String, String>();
		map.put(key, msg);
		return map;
	}
	
	

	public enum Messages {
		RECORD_NOT_FOUND("Record Not Found"), 
		INSUFICIENT_DATA("Provide left and right to compare"), 
		RECORD_REMOVED("Record Removed"), 
		PUT_TO_UPDATE("Use PUT to update Data"),
		INVALID_BASE64_ENCODE("Invalid Base64 encode"),
		DIFF_ERROR("It was not possible to run a diff in data input provided");
		
		private final String msg;
		
		Messages(String value){
			this.msg = value;
		}
		
		public String getMsg(){
			return this.msg;
		}
	}
}
