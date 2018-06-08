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
	protected Long length;
	protected Long precision;
	protected Long scale;
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
		case PT.URI:
		case PT.EMAIL:
		case PT.TEL:
			if (parameters == null) {
				this.parameters = Type.DEFAULT_STRING_LENGTH + "";
				length = Long.valueOf(Type.DEFAULT_STRING_LENGTH);
			} else {
				this.parameters = parameters;
				length = Long.valueOf(parameters);
			}
			break;
		case PT.NUMERIC:
			if (parameters != null) {
				this.parameters = parameters;
				if (parameters.contains(",")) {
					String[] precisionScale = parameters.split(",");
					precision = Long.valueOf(precisionScale[0]);
					scale = Long.valueOf(precisionScale[1]);
				} else {
					precision = Long.valueOf(parameters);
					scale = 0L;
				}
			}
			break;
		}
	}

	@JsonCreator
	public TypeField(@JsonProperty(Constants.TYPE) String type, @JsonProperty(Constants.LENGTH) Long length,
			@JsonProperty(Constants.PRECISION) Long precision, @JsonProperty(Constants.SCALE) Long scale,
			@JsonProperty(Constants.NOT_NULL) Boolean notNull) {
		this.type = type;
		this.length = length;
		this.precision = precision;
		this.scale = scale;
		this.notNull = notNull;

		switch (type) {
		case PT.STRING:
		case PT.URI:
		case PT.EMAIL:
		case PT.TEL:
			if (length == null) {
				this.length = Long.valueOf(Type.DEFAULT_STRING_LENGTH);
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
	public Long getLength() {
		return length;
	}

	@JsonProperty(Constants.PRECISION)
	public Long getPrecision() {
		return precision;
	}

	@JsonProperty(Constants.SCALE)
	public Long getScale() {
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