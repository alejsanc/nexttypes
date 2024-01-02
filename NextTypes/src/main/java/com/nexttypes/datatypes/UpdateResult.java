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

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.system.KeyWords;

public class UpdateResult {

	protected String message;
	protected ZonedDateTime udate;

	public UpdateResult(String message, ZonedDateTime udate) {
		this.message = message;
		this.udate = udate;
	}

	@JsonProperty(KeyWords.MESSAGE)
	public String getMessage() {
		return message;
	}

	@JsonProperty(KeyWords.UDATE)
	public ZonedDateTime getUDate() {
		return udate;
	}
}