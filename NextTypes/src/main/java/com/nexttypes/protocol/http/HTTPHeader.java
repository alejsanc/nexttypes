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

package com.nexttypes.protocol.http;

public enum HTTPHeader {
	ALLOW("Allow"),
	AUTHORIZATION("Authorization"),
	CONTENT_DISPOSITION("Content-Disposition"),
	CONTENT_SECURITY_POLICY("Content-Security-Policy"),
	CONTENT_TYPE("Content-Type"),
	DAV("DAV"),
	ETAG("ETag"),
	IF_NONE_MATCH("If-None-Match"),
	LOCATION("Location"),
	REFERRER_POLICY("Referrer-Policy"),
	USER_AGENT("User-Agent"),
	WWW_AUTHENTICATE("WWW-Authenticate");

	protected String header;

	private HTTPHeader(String header) {
		this.header = header;
	}

	@Override
	public String toString() {
		return header;
	}
}