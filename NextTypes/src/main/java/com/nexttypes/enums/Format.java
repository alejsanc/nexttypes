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

package com.nexttypes.enums;

public enum Format {
	BINARY("application/octet-stream", "bin"),
	ICALENDAR("text/calendar", "ics"),
	IMAGES("image/*", "*"),
	JAVASCRIPT("application/javascript", "js"),
	JSON("application/json", "json"),
	JSON_LD("application/ld+json", "json"),
	PNG("image/png", "png"),
	RSS("application/rss+xml", "rss"),
	SMILE("application/x-jackson-smile", "sml"),
	TEXT("text/plain", "txt"),
	XHTML("application/xhtml+xml", "html"),
	XML("application/xml", "xml");

	protected String contentType;
	protected String extension;

	private Format(String contentType, String extension) {
		this.contentType = contentType;
		this.extension = extension;
	}

	public String getContentType() {
		return contentType;
	}

	public String getExtension() {
		return extension;
	}
}