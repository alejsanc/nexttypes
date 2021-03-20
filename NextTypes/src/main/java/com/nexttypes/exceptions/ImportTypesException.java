/*
 * Copyright 2015-2021 Alejandro Sánchez <alex@nexttypes.com>
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

public class ImportTypesException extends NXException {
	protected static final long serialVersionUID = 1L;
	
	protected NXException e;

	public ImportTypesException(String type, NXException e) {
		super(type, null);
		this.e = e;
	}

	@Override
	public String getMessage(LanguageSettings languageSettings) {
		String message = "";
		
		if (type != null) {
			message += type + ": ";
		}
		
		return message + e.getMessage(languageSettings);
	}
	
	public NXException getException() {
		return e;
	}
}