/*
 * Copyright 2015-2024 Alejandro Sánchez <alex@nexttypes.com>
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

import com.nexttypes.settings.LanguageSettings;

public class ElementException extends FieldException {
	protected static final long serialVersionUID = 1L;

	protected String element;

	public ElementException(String type, String field, String element, String setting) {
		super(type, field, setting);
		this.element = element;
	}

	public String geElement() {
		return element;
	}

	@Override
	public String getMessage(LanguageSettings languageSettings) {
		String typeName = languageSettings.getTypeName(type);
		String fieldName = languageSettings.getFieldName(type, field);
		
		return languageSettings.gts(type, setting) + ": " + typeName + "::" + fieldName + "::" + element;
	}
}