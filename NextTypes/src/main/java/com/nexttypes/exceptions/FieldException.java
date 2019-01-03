/*
 * Copyright 2015-2019 Alejandro Sánchez <alex@nexttypes.com>
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

public class FieldException extends TypeException {
	protected static final long serialVersionUID = 1L;

	protected String field;
	protected Object value;

	public FieldException(String type, String field, String setting) {
		this(type, field, setting, null);
	}

	public FieldException(String type, String field, String setting, Object value) {
		super(type, setting);
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
		String fieldName = strings.getFieldName(type, field);

		String message = strings.gts(type, setting) + ": " + typeName + "::" + fieldName;

		if (value != null) {
			message += " -> " + value;
		}

		return message;
	}
}