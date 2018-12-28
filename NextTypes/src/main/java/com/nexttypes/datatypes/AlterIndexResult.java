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
import com.nexttypes.system.KeyWords;

public class AlterIndexResult {

	protected ZonedDateTime adate;
	protected boolean mode = false;
	protected boolean fields = false;

	public void setADate(ZonedDateTime adate) {
		this.adate = adate;
	}

	@JsonProperty(KeyWords.ADATE)
	public ZonedDateTime getADate() {
		return adate;
	}

	@JsonProperty(KeyWords.ALTERED)
	public boolean isAltered() {
		return mode || fields;
	}

	public void setModeAltered() {
		mode = true;
	}

	public void setFieldsAltered() {
		fields = true;
	}

	@JsonProperty(KeyWords.MODE_ALTERED)
	public boolean isModeAltered() {
		return mode;
	}

	@JsonProperty(KeyWords.FIELDS_ALTERED)
	public boolean arefieldsAltered() {
		return fields;
	}
}