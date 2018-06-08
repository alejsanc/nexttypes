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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Utils;

@JacksonXmlRootElement(localName = Constants.OBJECT)
@JsonPropertyOrder({ Constants.TYPE, Constants.ID, Constants.NAME, Constants.CDATE, Constants.UDATE, Constants.BACKUP })
public class NXObject extends Tuple {

	protected String type;
	protected String id;
	protected String name;
	protected ZonedDateTime cdate;
	protected ZonedDateTime udate;
	protected Boolean backup;
	protected String signature;

	public NXObject() {
		this(null, null, null, null, null, null, null);
	}

	public NXObject(String type) {
		this(type, null, null, null, null, null, null);
	}

	public NXObject(String type, String id) {
		this(type, id, null, null, null, null, null);
	}

	public NXObject(String type, String id, String name) {
		this(type, id, name, null, null, null, null);
	}

	public NXObject(String type, String id, String name, ZonedDateTime cdate, ZonedDateTime udate, Boolean backup) {
		this(type, id, name, cdate, udate, backup, null);
	}

	public NXObject(String type, String id, String name, ZonedDateTime cdate, ZonedDateTime udate, Boolean backup,
			LinkedHashMap<String, Object> fields) {

		if (cdate != null && !cdate.getOffset().equals(ZoneOffset.UTC)) {
			throw new InvalidValueException(Constants.INVALID_TIMEZONE, cdate.getZone());
		}

		if (udate != null && !udate.getOffset().equals(ZoneOffset.UTC)) {
			throw new InvalidValueException(Constants.INVALID_TIMEZONE, udate.getZone());
		}

		this.type = type;
		this.id = id;
		this.name = name != null ? name : id;
		this.cdate = cdate;
		this.udate = udate;
		this.backup = backup;

		if (fields != null) {
			this.fields = fields;
		}
	}

	@JsonProperty(Constants.TYPE)
	public String getType() {
		return type;
	}

	@JsonProperty(Constants.ID)
	public String getId() {
		return id;
	}

	@JsonProperty(Constants.NAME)
	public String getName() {
		return name;
	}

	@JsonProperty(Constants.CDATE)
	public ZonedDateTime getCDate() {
		return cdate;
	}

	@JsonProperty(Constants.UDATE)
	public ZonedDateTime getUDate() {
		return udate;
	}

	@JsonProperty(Constants.BACKUP)
	public Boolean getBackup() {
		return backup;
	}

	public String getETag() {
		return Utils.etag(udate);
	}
}