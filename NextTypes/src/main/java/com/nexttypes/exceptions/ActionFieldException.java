/*
 * Copyright 2015-2020 Alejandro Sánchez <alex@nexttypes.com>
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

import com.nexttypes.settings.Strings;

public class ActionFieldException extends ActionException {
	protected static final long serialVersionUID = 1L;

	protected String field;
	protected Object value;

	public ActionFieldException(String type, String action, String field, String setting) {
		this(type, action, field, setting, null);
	}

	public ActionFieldException(String type, String action, String field, String setting, Object value) {
		super(type, action, setting);
		this.field = field;
		this.value = value;
	}

	public String getField() {
		return field;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String getMessage(Strings strings) {
		String typeName = strings.getTypeName(type);
		String actionName = strings.getActionName(type,  action);
		String fieldName = strings.getActionFieldName(type, action, field);

		String message = strings.gts(type, setting) + ": " + typeName + "::" + actionName + "::" + fieldName;

		if (value != null) {
			message += " -> " + value;
		}

		return message;
	}
}