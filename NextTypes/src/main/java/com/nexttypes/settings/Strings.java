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
import com.nexttypes.system.Constants;

public class Strings extends TypeSettings {

	public Strings(ArrayList<Properties> settings) {
		super(settings);
	}

	public String getTypeName(String type) {
		return getTypeString(type, Constants.TYPE + "." + Constants.NAME, type);
	}

	public String getIdName(String type) {
		return getTypeString(type, Constants.ID, Constants.ID);
	}

	public String getObjectsName(String type) {
		return getTypeString(type, Constants.OBJECTS, Constants.OBJECTS);
	}

	public String getFieldName(String type, String field) {
		return getFieldString(type, field, Constants.NAME, field);
	}

	public String getActionName(String type, String action) {
		return getActionString(type, action, Constants.NAME, action);
	}

	public String getActionFieldName(String type, String action, String field) {
		return getActionFieldString(type, action, field, Constants.NAME, field);
	}

	public String getReferenceName(String type, FieldReference ref) {
		String refType = ref != null ? ref.getType() : null;
		String name = getTypeString(refType, Constants.REFERENCES + "." + type + "." + Constants.NAME);
		if (name == null) {
			name = type;
			if (ref != null) {
				name += " (" + ref.getField() + ")";
			}
		}
		return name;
	}
}