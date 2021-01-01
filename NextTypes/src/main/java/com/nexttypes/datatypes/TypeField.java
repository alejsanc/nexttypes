/*
 * Copyright 2015-2021 Alejandro Sánchez <alex@nexttypes.com>
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

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.nexttypes.system.KeyWords;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({ KeyWords.TYPE, KeyWords.LENGTH, KeyWords.PRECISION, KeyWords.SCALE,
	KeyWords.RANGE, KeyWords.NOT_NULL })
public class TypeField {
	
	public static final String[] RESERVED_NAMES = { KeyWords.ID, KeyWords.CDATE, KeyWords.UDATE,
			KeyWords.BACKUP };

	protected String type;
	protected Integer length;
	protected Integer precision;
	protected Integer scale;
	protected FieldRange range;
	protected Boolean notNull;
	protected String parameters;
	protected String oldName;

	public TypeField() {
	}

	public TypeField(String type, String parameters, Boolean notNull) {
		this(type, parameters, notNull, null);
	}

	public TypeField(String type, String parameters, Boolean notNull, String oldName) {
		this.type = type;
		this.notNull = notNull;
		this.oldName = oldName;

		switch (type) {
		case PT.STRING:
		case PT.URL:
		case PT.EMAIL:
		case PT.TEL:
			if (parameters == null) {
				this.parameters = Type.DEFAULT_STRING_LENGTH + "";
				length = Type.DEFAULT_STRING_LENGTH;
			} else {
				this.parameters = parameters;
				length = Integer.valueOf(parameters);
			}
			break;
		case PT.NUMERIC:
			if (parameters != null) {
				this.parameters = parameters;
				if (parameters.contains(",")) {
					String[] precisionScale = parameters.split(",");
					precision = Integer.valueOf(precisionScale[0]);
					scale = Integer.valueOf(precisionScale[1]);
				} else {
					precision = Integer.valueOf(parameters);
					scale = 0;
				}
			}
			break;
		}
	}

	@JsonCreator
	public TypeField(@JsonProperty(KeyWords.TYPE) String type,
			@JsonProperty(KeyWords.LENGTH) Integer length,
			@JsonProperty(KeyWords.PRECISION) Integer precision,
			@JsonProperty(KeyWords.SCALE) Integer scale,
			@JsonProperty(KeyWords.RANGE) FieldRange range,
			@JsonProperty(KeyWords.NOT_NULL) Boolean notNull) {
		
		if (range != null) {
			range.parse(type, precision, scale);
		} else if (PT.isNumericType(type) && precision != null) {
			range = new FieldRange(type, precision, scale);
		}
		
		this.type = type;
		this.length = length;
		this.precision = precision;
		this.scale = scale;
		this.range = range;
		this.notNull = notNull;
				
		switch (type) {
		case PT.STRING:
		case PT.URL:
		case PT.EMAIL:
		case PT.TEL:
			if (length == null) {
				this.length = Type.DEFAULT_STRING_LENGTH;
				parameters = Type.DEFAULT_STRING_LENGTH + "";
			} else {
				parameters = length.toString();
			}
			break;
		case PT.NUMERIC:
			if (precision != null) {
				parameters = precision.toString();

				if (scale != null) {
					parameters += "," + scale;
				}
			}
			break;
		}
	}

	@JsonProperty(KeyWords.TYPE)
	public String getType() {
		return type;
	}

	@JsonProperty(KeyWords.LENGTH)
	public Integer getLength() {
		return length;
	}

	@JsonProperty(KeyWords.PRECISION)
	public Integer getPrecision() {
		return precision;
	}

	@JsonProperty(KeyWords.SCALE)
	public Integer getScale() {
		return scale;
	}
	
	@JsonProperty(KeyWords.RANGE)
	public FieldRange getRange() {
		return range;
	}

	@JsonProperty(KeyWords.NOT_NULL)
	public Boolean isNotNull() {
		return notNull;
	}
	
	public String getParameters() {
		return parameters;
	}

	public String getOldName() {
		return oldName;
	}
	
	public static boolean isReservedName(String field) {
		return ArrayUtils.contains(RESERVED_NAMES, field);
	}
	
	public TypeField getStringTypeField() {
		return new TypeField(PT.STRING, length, null, null, null, notNull);
	}	
}