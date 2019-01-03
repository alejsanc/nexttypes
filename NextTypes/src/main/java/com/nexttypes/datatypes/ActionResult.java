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

package com.nexttypes.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.system.KeyWords;

public class ActionResult {

	protected String message;
	protected Object value;

	public ActionResult(String message) {
		this(message, null);
	}

	public ActionResult(String message, Object value) {
		this.message = message;
		this.value = value;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@JsonProperty(KeyWords.MESSAGE)
	public String getMessage() {
		return message;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@JsonProperty(KeyWords.VALUE)
	public Object getValue() {
		return value;
	}
}