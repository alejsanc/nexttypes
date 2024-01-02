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

package com.nexttypes.datatypes;

import java.io.Serializable;
import java.util.LinkedHashMap;

import com.nexttypes.enums.Format;
import com.nexttypes.protocol.http.HTTPHeader;
import com.nexttypes.protocol.http.HTTPStatus;

public class Content implements Serializable {
	protected static final long serialVersionUID = 1L;
	protected Object value;
	protected LinkedHashMap<String, String> headers = new LinkedHashMap<>();
	protected String contentType;
	protected int status;

	public Content() {
		this(null, Format.XHTML, HTTPStatus.OK);
	}

	public Content(HTTPStatus status) {
		this(null, Format.XHTML, status);
	}

	public Content(Object value) {
		this(value, Format.XHTML, HTTPStatus.OK);
	}

	public Content(Object value, HTTPStatus status) {
		this(value, Format.XHTML, status);
	}

	public Content(Object value, Format format) {
		this(value, format.getContentType(), HTTPStatus.OK);
	}

	public Content(Object value, String contentType) {
		this(value, contentType, HTTPStatus.OK);
	}

	public Content(Object value, String contentType, HTTPStatus status) {
		this(value, contentType, status.toInt32());
	}

	public Content(Object value, Format format, HTTPStatus status) {
		this(value, format.getContentType(), status.toInt32());
	}

	public Content(Object value, String contentType, int status) {
		this.value = value;
		this.contentType = contentType;
		this.status = status;
	}

	public String getContentType() {
		return contentType;
	}

	public Object getValue() {
		return value;
	}

	public int getStatus() {
		return status;
	}

	public LinkedHashMap<String, String> getHeaders() {
		return headers;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setValue(String value) {
		this.value = value.getBytes();
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setStatus(HTTPStatus status) {
		this.status = status.toInt32();
	}

	public void setHeader(HTTPHeader header, String value) {
		headers.put(header.toString(), value);
	}

	public void setHeader(String header, String value) {
		headers.put(header, value);
	}
}