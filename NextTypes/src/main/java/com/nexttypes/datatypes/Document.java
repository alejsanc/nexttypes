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

package com.nexttypes.datatypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.datatypes.JSON.JSONObject;
import com.nexttypes.system.KeyWords;

@JsonPropertyOrder({ KeyWords.CONTENT, KeyWords.TEXT, KeyWords.CONTENT_TYPE })
public class Document extends File {
	private static final long serialVersionUID = 1L;

	protected String text;
	
	public Document() {
		type = PT.DOCUMENT;
	}
	
	public Document(File file) {
		super(file);
		
		type = PT.DOCUMENT;
		
		text();
	}
	
	public Document(byte[] content) {
		this(null, content);
	}

	public Document(String name, byte[] content) {
		super(name, content);
		
		type = PT.DOCUMENT;

		text();
	}
	
	public Document(JSONObject document) {
		this(document.getBinary(KeyWords.CONTENT), document.getString(KeyWords.TEXT),
				document.getString(KeyWords.CONTENT_TYPE));
	}

	@JsonCreator
	public Document(@JsonProperty(KeyWords.CONTENT) byte[] content, @JsonProperty(KeyWords.TEXT) String text,
			@JsonProperty(KeyWords.CONTENT_TYPE) String contentType) {
		super(content, contentType);
		
		type = PT.DOCUMENT;

		this.text = text;
	}
	
	protected void text() {
		text = handler.toString().trim();
	}

	@JsonProperty(KeyWords.TEXT)
	public String getText() {
		return text;
	}

	@Override
	public String getValue() {
		String textParameter = "\"" + text.replace("\"", "\"\"") + "\"";

		return "(" + hexEncode(content) + "," + textParameter + "," + contentType + ")";
	}

	@Override
	public void setValue(String value) {
		if (value != null && value.length() > 0) {
			int token1 = value.indexOf(',');
			int token2 = value.lastIndexOf(',');

			content = hexDecode(value.substring(1, token1));

			if (value.charAt(token1 + 1) == '"') {
				text = value.substring(token1 + 2, token2 - 1);
				text = text.replace("\"\"", "\"");
			} else {
				text = value.substring(token1 + 1, token2);
			}

			contentType = value.substring(token2 + 1, value.length() - 1);

		}
	}
}
