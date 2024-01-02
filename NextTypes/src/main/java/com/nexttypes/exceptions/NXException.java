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

public class NXException extends RuntimeException {
	protected static final long serialVersionUID = 1L;

	protected String type;
	protected String setting;
	
	public NXException(String setting) {
		this(null, setting);
	}

	public NXException(String type, String setting) {
		this.type = type;
		this.setting = setting;
	}
	
	public NXException(String type, String setting, Throwable cause) {
		super(cause);
		this.type = type;
		this.setting = setting;
	}

	public NXException(Throwable cause) {
		super(cause);
	}

	public String getType() {
		return type;
	}

	public String getSetting() {
		return setting;
	}

	public String getMessage(LanguageSettings languageSettings) {
		String message = null;
		Throwable cause = getCause();
		
		if (cause == null) {
			message = languageSettings.gts(type, setting);
		} else {			
			message = getMessage(cause);
		}
		
		return message;
	}

	public static String getMessage(Throwable e) {
		String className = e.getClass().getName();
		String message = e.getMessage();
		
		
		if (message == null) {
			message = className;
		} else {
			message = className + ": " + message;
		}

		return message;
	}
}