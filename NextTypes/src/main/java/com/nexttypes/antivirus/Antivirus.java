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

package com.nexttypes.antivirus;

import java.util.LinkedHashMap;
import java.util.Map;

import com.nexttypes.datatypes.File;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.exceptions.ActionFieldException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.exceptions.ObjectFieldException;
import com.nexttypes.system.Context;

public abstract class Antivirus {
	
	public Antivirus() {}
	
	public Antivirus(Context context) {}
	
	public void scan(String type, String[] objects, String action, Object[] parameters, LinkedHashMap<String, TypeField> typeFields) {
		
		int x = 0;
		
		for(Map.Entry<String, TypeField> entry : typeFields.entrySet()) {
						
			Object value = parameters[x];
			x++;
			
			if (value != null) {
				
				String field = entry.getKey();
				String fieldType = entry.getValue().getType();
				
				if (PT.BINARY.equals(fieldType)) {
					scan(type, objects, action, field, (byte[])value);
				} else if (PT.isFileType(fieldType)) {
					scan(type, objects, action, field, ((File)value).getContent());
				}
			}
		}
	}
	
	public void scan(NXObject object, LinkedHashMap<String, TypeField> typeFields) {
		
		for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
				
			Object value = entry.getValue();
				
			if (value != null) {
				
				String type = object.getType();
				String id = object.getId();
				String field = entry.getKey();
				String fieldType = typeFields.get(field).getType();
			
				if (PT.BINARY.equals(fieldType)) {
					scan(type, id, field, (byte[])value);
				} else if (PT.isFileType(fieldType)) {
					scan(type, id, field, ((File)value).getContent());
				}
			}
		}
	}
	
	public void scan(String type, String id, String field, String fieldType, Object value) {
		
		if (value != null && PT.isBinaryType(fieldType)) {
			scan(type, id, field, (byte[])value);
		}
	}
	
	public void scan(String type, String[] objects, String action, String field, byte[] data) {
		String result = scan(data);
		
		if (result != null) {
			throw new ActionFieldException(type, objects, action, field, NXException.VIRUS_FOUND, result);
		}
	}
	
	public void scan(String type, String id, String field, byte[] data) {
		String result = scan(data);
		
		if (result != null) {
			throw new ObjectFieldException(type, id, field, NXException.VIRUS_FOUND, result);
		}
	}
		
	public abstract String scan(byte[] data);

}
