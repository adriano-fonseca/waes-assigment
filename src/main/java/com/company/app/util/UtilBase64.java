package com.company.app.util;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This classes provise some operation to Base64 data
 * @author adriano
 *
 */
public class UtilBase64 {

	
	/**
	 * Method receives text/plain and the charset table  
	 * @param textPlain
	 * @return text encode in Base64
	 */
	public static byte[] encodeBase64(String textPlain){
		Encoder encoder = Base64.getEncoder();
		byte[] textEncoded = null;
		try {
			textEncoded = encoder.encode(textPlain.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return textEncoded;
	}
	
	/**
	 * @param textEncode
	 * @param charset
	 * @return string in a charset received that containts the text decode
	 */
	public static byte[] decodeBase64(String textEncoded, String charset){
		byte[] array = null;
		Decoder decoder = Base64.getDecoder();
		try {
			array = decoder.decode(textEncoded.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return array;
	}
	
	/**
	 * 
	 * @param content
	 * @return this methods decodes Base64 strings default charset
	 */
	public static byte[] decodeBase64(String content){
		byte[] array = null;
		Decoder decoder = Base64.getDecoder();
		array = decoder.decode(content.getBytes());
		return array;
	}
	
	/**
	 * 
	 * @param content
	 * @return this method returns a byte array encoded i Base64
	 */
	public static byte[] encodeBase64(byte[] content){
		Encoder encoder = Base64.getEncoder();
		byte[] encoded = null;
		encoded = encoder.encode(content);
		return encoded;
	}
	/**
	 * For the content to be considered as valid it needs to attend the conditions bellow:
	 * 
	 * Length is a multiple of 4 characters
	 * All character in the set should be one of those A-Z, a-z, 0-9, +, / except for padding at the end which is 0, 1 or 2 '=' characters
	 * 
	 * @param content
	 * @return if the content is considered a valid Base64 encode
	 */
	public static boolean isValidBase64Encode(String content){
		Pattern p = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");
		Matcher m = p.matcher(content);
		return m.matches();
	}
}
