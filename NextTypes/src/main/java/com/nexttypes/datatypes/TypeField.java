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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.system.Constants;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({ Constants.TYPE, Constants.LENGTH, Constants.PRECISION, Constants.SCALE, Constants.NOT_NULL })
public class TypeField {

	protected String type;
	protected Integer length;
	protected Integer precision;
	protected Integer scale;
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
	public TypeField(@JsonProperty(Constants.TYPE) String type,
			@JsonProperty(Constants.LENGTH) Integer length,
			@JsonProperty(Constants.PRECISION) Integer precision,
			@JsonProperty(Constants.SCALE) Integer scale,
			@JsonProperty(Constants.NOT_NULL) Boolean notNull) {
		
		this.type = type;
		this.length = length;
		this.precision = precision;
		this.scale = scale;
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

	@JsonProperty(Constants.TYPE)
	public String getType() {
		return type;
	}

	@JsonProperty(Constants.LENGTH)
	public Integer getLength() {
		return length;
	}

	@JsonProperty(Constants.PRECISION)
	public Integer getPrecision() {
		return precision;
	}

	@JsonProperty(Constants.SCALE)
	public Integer getScale() {
		return scale;
	}

	@JsonProperty(Constants.NOT_NULL)
	public Boolean isNotNull() {
		return notNull;
	}
	
	public String getParameters() {
		return parameters;
	}

	public String getOldName() {
		return oldName;
	}
}