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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Utils;

@JsonInclude(Include.NON_NULL)
@JacksonXmlRootElement(localName = KeyWords.OBJECT)
@JsonPropertyOrder({ KeyWords.TYPE, KeyWords.ID, KeyWords.NAME, KeyWords.CDATE, KeyWords.UDATE, KeyWords.BACKUP })
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

	public NXObject(String type, String id, String name, ZonedDateTime cdate, ZonedDateTime udate,
			Boolean backup) {
		this(type, id, name, cdate, udate, backup, null);
	}

	public NXObject(String type, String id, String name, ZonedDateTime cdate, ZonedDateTime udate,
			Boolean backup, LinkedHashMap<String, Object> fields) {
		
		if (cdate != null && !cdate.getOffset().equals(ZoneOffset.UTC)) {
			throw new InvalidValueException(KeyWords.INVALID_TIMEZONE, cdate.getZone());
		}

		if (udate != null && !udate.getOffset().equals(ZoneOffset.UTC)) {
			throw new InvalidValueException(KeyWords.INVALID_TIMEZONE, udate.getZone());
		}

		this.type = type;
		this.id = id;
		this.name = name;
		this.cdate = cdate;
		this.udate = udate;
		this.backup = backup;

		if (fields != null) {
			this.fields = fields;
		}
	}

	@JsonProperty(KeyWords.TYPE)
	public String getType() {
		return type;
	}

	@JsonProperty(KeyWords.ID)
	public String getId() {
		return id;
	}

	@JsonProperty(KeyWords.NAME)
	public String getName() {
		return name;
	}

	@JsonProperty(KeyWords.CDATE)
	public ZonedDateTime getCDate() {
		return cdate;
	}

	@JsonProperty(KeyWords.UDATE)
	public ZonedDateTime getUDate() {
		return udate;
	}

	@JsonProperty(KeyWords.BACKUP)
	public Boolean getBackup() {
		return backup;
	}

	public String getETag() {
		return Utils.etag(udate);
	}
}