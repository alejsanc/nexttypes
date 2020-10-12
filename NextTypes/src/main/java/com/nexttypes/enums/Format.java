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

package com.nexttypes.enums;

public enum Format {
	BINARY("binary", "application/octet-stream", "bin"),
	ICALENDAR("icalendar", "text/calendar", "ics"),
	IMAGES("images", "image/*", "*"),
	JAVASCRIPT("javascript", "application/javascript", "js"),
	JSON("json", "application/json", "json"),
	JSON_LD("json_ld", "application/ld+json", "json"),
	PNG("png", "image/png", "png"),
	RSS("rss", "application/rss+xml", "rss"),
	SMILE("smile", "application/x-jackson-smile", "sml"),
	TEXT("text", "text/plain", "txt"),
	XHTML("xhtml", "application/xhtml+xml", "html"),
	XML("xml", "application/xml", "xml");

	protected String format;
	protected String contentType;
	protected String extension;

	private Format(String format, String contentType, String extension) {
		this.format = format;
		this.contentType = contentType;
		this.extension = extension;
	}

	public String getContentType() {
		return contentType;
	}

	public String getExtension() {
		return extension;
	}
	
	@Override
	public String toString() {
		return format;
	}
}