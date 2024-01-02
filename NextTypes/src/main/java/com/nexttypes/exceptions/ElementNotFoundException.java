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
import com.nexttypes.system.KeyWords;

public class ElementNotFoundException extends NotFoundException {
	private static final long serialVersionUID = 1L;

	protected String id;
	protected String field;
	protected String element;

	public ElementNotFoundException(String type, String id, String field, String element) {
		super(type, KeyWords.ELEMENT_NOT_FOUND);
		this.id = id;
		this.field = field;
		this.element = element;
	}

	public String getId() {
		return id;
	}

	public String getField() {
		return field;
	}

	public String getElement() {
		return element;
	}

	public String getMessage(LanguageSettings languageSettings) {
		String typeName = languageSettings.getTypeName(type);
		String fieldName = languageSettings.getFieldName(type, field);
		
		return languageSettings.gts(type, KeyWords.ELEMENT_NOT_FOUND)
				+ ": " + typeName + "::" + id + "::" + fieldName + "::" + element;
	}
}