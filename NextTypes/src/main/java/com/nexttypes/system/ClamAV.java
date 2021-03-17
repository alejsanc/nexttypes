package com.nexttypes.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import com.nexttypes.datatypes.File;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.exceptions.ActionFieldException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.settings.Settings;

public class ClamAV {
	
	protected static final String OK = "stream: OK";
	protected static final byte[] INSTREAM = "zINSTREAM\0".getBytes();
	protected static final byte[] END = new byte[]{0,0,0,0};
	
	protected Settings settings;
	protected String host;
	protected int port;
	
	public ClamAV(Context context) {
		settings = context.getSettings(Settings.CLAMAV_SETTINGS);
		host = settings.getString(KeyWords.HOST);
		port = settings.getInt32(KeyWords.PORT);
	}
	
	public void scan(String type, Object[] parameters, LinkedHashMap<String, TypeField> typeFields,
			String action) {
		
		int x = 0;
		
		for(Map.Entry<String, TypeField> entry : typeFields.entrySet()) {
						
			Object value = parameters[x];
			x++;
			
			if (value != null) {
				
				String field = entry.getKey();
				String fieldType = entry.getValue().getType();
				
				switch (fieldType) {
				case PT.BINARY:
					scan(type, action, field, (byte[])value);
					break;
					
				case PT.FILE:
				case PT.DOCUMENT:
				case PT.IMAGE:
				case PT.AUDIO:
				case PT.VIDEO:
					scan(type, action, field, ((File)value).getContent());
					break;
				}
			}
		}
	}
	
	public void scan(NXObject object, LinkedHashMap<String, TypeField> typeFields, String action) {
		
		for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
				
			Object value = entry.getValue();
				
			if (value != null) {
				
				String type = object.getType();
				String field = entry.getKey();
				String fieldType = typeFields.get(field).getType();
			
				switch (fieldType) {
				case PT.BINARY:
					scan(type, action, field, (byte[])value);
					break;
					
				case PT.FILE:
				case PT.DOCUMENT:
				case PT.IMAGE:
				case PT.AUDIO:
				case PT.VIDEO:
					scan(type, action, field, ((File)value).getContent());
					break;
				}
			}
		}
	}
	
	public void scan(String type, String action, String field, String fieldType, Object value) {
		
		if (value != null) {
			
			switch (fieldType) {
			case PT.BINARY:
				scan(type, action, field, (byte[])value);
				break;
				
			case PT.FILE:
			case PT.DOCUMENT:
			case PT.IMAGE:
			case PT.AUDIO:
			case PT.VIDEO:
				scan(type, action, field, (byte[])value);
				break;
			}
		}
	}
	
	public void scan(String type, String action, String field, byte[] data) {
		
		try (
				Socket socket = new Socket(host, port);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				OutputStream output = socket.getOutputStream();
		) {
			byte[] size = ByteBuffer.allocate(4).putInt(data.length).array();
			
			output.write(INSTREAM);
			output.write(size);
			output.write(data);
			output.write(END);
			output.flush();
			
			System.out.println("antes readline");
			String result = input.readLine().trim();
			System.out.println("despues readline");
							        
		    if(!OK.equals(result)) {
		    	
		    	result = result.substring(8, result.length()-6);
		    	
		    	throw new ActionFieldException(type, action, field, KeyWords.VIRUS_FOUND, result);
		    } 
		    
		} catch (IOException e) {
			throw new NXException(e);
		} 
	}
}
