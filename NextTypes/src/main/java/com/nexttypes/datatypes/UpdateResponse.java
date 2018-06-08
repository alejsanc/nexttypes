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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.system.Constants;

public class UpdateResponse {

	protected String message;
	protected ZonedDateTime udate;

	public UpdateResponse(String message, ZonedDateTime udate) {
		this.message = message;
		this.udate = udate;
	}

	@JsonProperty(Constants.MESSAGE)
	public String getMessage() {
		return message;
	}

	@JsonProperty(Constants.UDATE)
	public ZonedDateTime getUDate() {
		return udate;
	}
}