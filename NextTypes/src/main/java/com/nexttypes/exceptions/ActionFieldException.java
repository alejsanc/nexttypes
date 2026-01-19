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

package com.nexttypes.exceptions;

import org.apache.commons.lang3.StringUtils;

import com.nexttypes.settings.LanguageSettings;

public class ActionFieldException extends ActionException {
	protected static final long serialVersionUID = 1L;

	protected String[] objects;
	protected String field;
	protected Object value;

	public ActionFieldException(String type, String[] objects, String action, String field, String setting) {
		this(type, objects, action, field, setting, null);
	}

	public ActionFieldException(String type, String[] objects, String action, String field, String setting, Object value) {
		super(type, action, setting);
		this.objects = objects;
		this.field = field;
		this.value = value;
	}
	
	public String[] getObjects() {
		return objects;
	}

	public String getField() {
		return field;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String getMessage(LanguageSettings languageSettings) {
		String typeName = languageSettings.getTypeName(type);
		String actionName = languageSettings.getActionName(type, action);
		String fieldName = languageSettings.getActionFieldName(type, action, field);
		
		StringBuilder message = new StringBuilder(languageSettings.gts(type, setting) + ": " + typeName
				+ "::" + actionName + "::" + fieldName);
		
		if (value != null) {
			message.append(" -> " + value);
		}
		
		if (objects != null && objects.length > 0) {
			message.append(" (" + StringUtils.join(objects, ",") + ")");
		}
		
		return message.toString();
	}
}