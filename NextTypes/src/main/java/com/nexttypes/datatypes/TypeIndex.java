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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.enums.IndexMode;
import com.nexttypes.system.Constants;

@JsonPropertyOrder({ Constants.MODE, Constants.FIELDS })
public class TypeIndex {

	protected IndexMode mode;
	protected String[] fields;
	protected String oldName;

	public TypeIndex(IndexMode mode, String[] fields, String oldName) {
		this(mode, fields);
		this.oldName = oldName;
	}

	@JsonCreator
	public TypeIndex(@JsonProperty(Constants.MODE) String mode, @JsonProperty(Constants.FIELDS) String fields[]) {
		this(IndexMode.valueOf(mode.toUpperCase()), fields);
	}

	public TypeIndex(IndexMode mode, String fields[]) {
		this.mode = mode;
		this.fields = fields;
	}

	@JsonProperty(Constants.MODE)
	public IndexMode getMode() {
		return mode;
	}

	@JsonProperty(Constants.FIELDS)
	public String[] getFields() {
		return fields;
	}

	public String getOldName() {
		return oldName;
	}
}