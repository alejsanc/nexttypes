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

public class AlterFieldResult {

	protected ZonedDateTime adate;
	protected boolean type = false;
	protected boolean notNull = false;
	protected boolean parameters = false;

	@JsonProperty(Constants.ALTERED)
	public boolean isAltered() {
		return type || notNull || parameters;
	}

	public void setADate(ZonedDateTime adate) {
		this.adate = adate;
	}

	@JsonProperty(Constants.ADATE)
	public ZonedDateTime getADate() {
		return adate;
	}

	public void setTypeAltered() {
		type = true;
	}

	public void setNotNullAltered() {
		notNull = true;
	}

	public void setParametersAltered() {
		parameters = true;
	}

	@JsonProperty(Constants.TYPE_ALTERED)
	public boolean isTypeAltered() {
		return type;
	}

	@JsonProperty(Constants.NOT_NULL_ALTERED)
	public boolean isNotNullAltered() {
		return notNull;
	}

	@JsonProperty(Constants.PARAMETERS_ALTERED)
	public boolean isParametersAltered() {
		return parameters;
	}
}
