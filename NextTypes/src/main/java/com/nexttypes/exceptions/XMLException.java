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

import org.xml.sax.SAXParseException;

import com.nexttypes.settings.LanguageSettings;

public class XMLException extends NXException {
	private static final long serialVersionUID = 1L;

	protected int line;
	protected int column;
	protected String message;

	public XMLException(SAXParseException e) {
		super(e);
		this.line = e.getLineNumber();
		this.column = e.getColumnNumber();
		this.message = e.getMessage();
	}
	
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}

	@Override
	public String getMessage() {
		return line + ": " + message;
	}
	
	@Override
	public String getMessage(LanguageSettings languageSettings) {
		return getMessage();
	}
}