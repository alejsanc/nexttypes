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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.system.Constants;

@JsonPropertyOrder({ Constants.NAME, Constants.UDATE, Constants.OBJECTS, Constants.SIZE })
public class TypeInfo {

	protected String name;
	protected ZonedDateTime udate;
	protected Long objects;
	protected Long size;

	public TypeInfo(String name, Long size) {
		this.name = name;
		this.size = size;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUDate(ZonedDateTime udate) {
		this.udate = udate;
	}

	public void setObjects(Long objects) {
		this.objects = objects;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	@JsonProperty(Constants.NAME)
	public String getName() {
		return name;
	}

	@JsonProperty(Constants.UDATE)
	public ZonedDateTime getUDate() {
		return udate;
	}

	@JsonProperty(Constants.OBJECTS)
	public Long getObjects() {
		return objects;
	}

	@JsonProperty(Constants.SIZE)
	public Long getSize() {
		return size;
	}
}