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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.system.Constants;

@JsonPropertyOrder({ Constants.NAME, Constants.CDATE, Constants.ADATE, Constants.FIELDS, Constants.INDEXES,
		Constants.ACTIONS })
public class Type {

	public static final int MAX_ID_LENGTH = 100;
	public static final int MAX_TYPE_NAME_LENGTH = 30;
	public static final int MAX_FIELD_NAME_LENGTH = 30;
	public static final int MAX_INDEX_NAME_LENGTH = 30;
	public static final int MAX_ACTION_NAME_LENGTH = 30;
	public static final int DEFAULT_STRING_LENGTH = 250;

	public static final String[] SYSTEM_TYPES = new String[] { "language", "language_language", "user",
			"user_certificate", "group", "group_user", "group_language" };

	protected String name;
	protected ZonedDateTime cdate;
	protected ZonedDateTime adate;
	protected LinkedHashMap<String, TypeField> fields;
	protected LinkedHashMap<String, TypeIndex> indexes;
	protected LinkedHashMap<String, LinkedHashMap<String, TypeField>> actions;

	public Type(String name) {
		this.name = name;
		this.fields = new LinkedHashMap<>();
		this.indexes = new LinkedHashMap<>();
		this.actions = new LinkedHashMap<>();
	}

	public Type(String name, ZonedDateTime cdate, ZonedDateTime adate, LinkedHashMap<String, TypeField> fields,
			LinkedHashMap<String, TypeIndex> indexes) {
		this(name, cdate, adate, fields, indexes, null);
	}

	@JsonCreator
	public Type(@JsonProperty(Constants.NAME) String name, @JsonProperty(Constants.CDATE) ZonedDateTime cdate,
			@JsonProperty(Constants.ADATE) ZonedDateTime adate,
			@JsonProperty(Constants.FIELDS) LinkedHashMap<String, TypeField> fields,
			@JsonProperty(Constants.INDEXES) LinkedHashMap<String, TypeIndex> indexes,
			@JsonProperty(Constants.ACTIONS) LinkedHashMap<String, LinkedHashMap<String, TypeField>> actions) {

		if (cdate != null && !cdate.getOffset().equals(ZoneOffset.UTC)) {
			throw new InvalidValueException(Constants.INVALID_TIMEZONE, cdate.getZone());
		}

		if (adate != null && !adate.getOffset().equals(ZoneOffset.UTC)) {
			throw new InvalidValueException(Constants.INVALID_TIMEZONE, adate.getZone());
		}

		this.name = name;
		this.cdate = cdate;
		this.adate = adate;
		this.fields = fields != null ? fields : new LinkedHashMap<>();
		this.indexes = indexes != null ? indexes : new LinkedHashMap<>();
		this.actions = actions != null ? actions : new LinkedHashMap<>();
	}

	@JsonProperty(Constants.NAME)
	public String getName() {
		return name;
	}

	@JsonProperty(Constants.CDATE)
	public ZonedDateTime getCDate() {
		return cdate;
	}

	@JsonProperty(Constants.ADATE)
	public ZonedDateTime getADate() {
		return adate;
	}

	@JsonProperty(Constants.FIELDS)
	public LinkedHashMap<String, TypeField> getFields() {
		return fields;
	}

	@JsonProperty(Constants.INDEXES)
	public LinkedHashMap<String, TypeIndex> getIndexes() {
		return indexes;
	}

	@JsonProperty(Constants.ACTIONS)
	public LinkedHashMap<String, LinkedHashMap<String, TypeField>> getActions() {
		return actions;
	}

	public void setFields(LinkedHashMap<String, TypeField> fields) {
		this.fields = fields;
	}

	public void setIndexes(LinkedHashMap<String, TypeIndex> indexes) {
		this.indexes = indexes;
	}

	public void setActions(LinkedHashMap<String, LinkedHashMap<String, TypeField>> actions) {
		this.actions = actions;
	}
}