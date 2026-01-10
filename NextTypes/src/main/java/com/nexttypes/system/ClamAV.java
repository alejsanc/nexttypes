/*
 * Copyright 2015-2026 Alejandro Sánchez <alex@nexttypes.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.nexttypes.exceptions.FieldException;
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
	
	public void scan(String type, Object[] parameters, LinkedHashMap<String, TypeField> typeFields) {
		
		int x = 0;
		
		for(Map.Entry<String, TypeField> entry : typeFields.entrySet()) {
						
			Object value = parameters[x];
			x++;
			
			if (value != null) {
				
				String field = entry.getKey();
				String fieldType = entry.getValue().getType();
				
				if (PT.BINARY.equals(fieldType)) {
					scan(type, field, (byte[])value);
				} else if (PT.isFileType(fieldType)) {
					scan(type, field, ((File)value).getContent());
				}
			}
		}
	}
	
	public void scan(NXObject object, LinkedHashMap<String, TypeField> typeFields) {
		
		for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
				
			Object value = entry.getValue();
				
			if (value != null) {
				
				String type = object.getType();
				String field = entry.getKey();
				String fieldType = typeFields.get(field).getType();
			
				if (PT.BINARY.equals(fieldType)) {
					scan(type, field, (byte[])value);
				} else if (PT.isFileType(fieldType)) {
					scan(type, field, ((File)value).getContent());
				}
			}
		}
	}
	
	public void scan(String type, String field, String fieldType, Object value) {
		
		if (value != null && PT.isBinaryType(fieldType)) {
			scan(type, field, (byte[])value);
		}
	}
	
	public void scan(String type, String field, byte[] data) {
		
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
			
			String result = input.readLine().trim();
										        
		    if(!OK.equals(result)) {
		    	
		    	result = result.substring(8, result.length()-6);
		    	
		    	throw new FieldException(type, field, KeyWords.VIRUS_FOUND, result);
		    } 
		    
		} catch (IOException e) {
			throw new NXException(e);
		} 
	}
}
