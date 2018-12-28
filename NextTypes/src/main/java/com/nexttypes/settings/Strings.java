/*
 * Copyright 2015-2018 Alejandro Sánchez <alex@nexttypes.com>
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

package com.nexttypes.settings;

import java.util.ArrayList;
import java.util.Properties;

import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.system.KeyWords;

public class Strings extends TypeSettings {

	public Strings(ArrayList<Properties> settings) {
		super(settings);
	}

	public String getTypeName(String type) {
		return getTypeString(type, KeyWords.TYPE + "." + KeyWords.NAME, type);
	}

	public String getIdName(String type) {
		return getTypeString(type, KeyWords.ID, KeyWords.ID);
	}

	public String getObjectsName(String type) {
		return getTypeString(type, KeyWords.OBJECTS, KeyWords.OBJECTS);
	}

	public String getFieldName(String type, String field) {
		return getFieldString(type, field, KeyWords.NAME, field);
	}
	
	public String getActionName(String action) {
		return getActionName(null, action);
	}

	public String getActionName(String type, String action) {
		return getActionString(type, action, KeyWords.NAME, action);
	}

	public String getActionFieldName(String type, String action, String field) {
		return getActionFieldString(type, action, field, KeyWords.NAME, field);
	}

	public String getReferenceName(String type, FieldReference ref) {
		String refType = ref != null ? ref.getType() : null;
		String name = getTypeString(refType, KeyWords.REFERENCES + "." + type + "." + KeyWords.NAME);
		if (name == null) {
			name = type;
			if (ref != null) {
				name += " (" + ref.getField() + ")";
			}
		}
		return name;
	}
	
	public String getComparisonName(String type, String comparison) {
		return getTypeString(type, KeyWords.COMPARISONS + "." + comparison + "." + KeyWords.NAME);
	}
}