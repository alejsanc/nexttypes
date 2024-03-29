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

public class RenameResult {

	protected String message;
	protected ZonedDateTime adate;

	public RenameResult(String message, ZonedDateTime adate) {
		this.message = message;
		this.adate = adate;
	}

	@JsonProperty(KeyWords.MESSAGE)
	public String getMessage() {
		return message;
	}

	@JsonProperty(KeyWords.ADATE)
	public ZonedDateTime getADate() {
		return adate;
	}
}