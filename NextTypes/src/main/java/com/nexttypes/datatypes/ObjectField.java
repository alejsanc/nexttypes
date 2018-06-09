/*
 * Copyright 2015-2018 Alejandro Sánchez <alex@nexttypes.com>
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

import java.time.ZonedDateTime;

import com.nexttypes.system.Utils;

public class ObjectField {
	protected Object value;
	protected ZonedDateTime udate;
	protected String contentType;

	public ObjectField(Object value, ZonedDateTime udate, String contentType) {
		if (value instanceof ObjectReference) {
			value = ((ObjectReference) value).getId();
		}

		this.value = value;
		this.udate = udate;
		this.contentType = contentType;
	}

	public Object getValue() {
		return value;
	}

	public ZonedDateTime getUDate() {
		return udate;
	}

	public String getETag() {
		return Utils.etag(udate);
	}

	public String getContentType() {
		return contentType;
	}
}
