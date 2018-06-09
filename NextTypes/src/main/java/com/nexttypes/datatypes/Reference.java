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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.system.Constants;

@JsonPropertyOrder({ Constants.REFERENCED_TYPE, Constants.REFERENCING_TYPE, Constants.REFERENCING_FIELD })
public class Reference {

	protected String referencedType;
	protected String referencingType;
	protected String referencingField;

	public Reference(String referencedType, String referencingType, String referencingField) {
		this.referencedType = referencedType;
		this.referencingType = referencingType;
		this.referencingField = referencingField;
	}

	@JsonProperty(Constants.REFERENCED_TYPE)
	public String getReferencedType() {
		return referencedType;
	}

	@JsonProperty(Constants.REFERENCING_TYPE)
	public String getReferencingType() {
		return referencingType;
	}

	@JsonProperty(Constants.REFERENCING_FIELD)
	public String getReferencingField() {
		return referencingField;
	}

	public void setReferencedType(String referencedType) {
		this.referencedType = referencedType;
	}

	public void setReferencingType(String referencingType) {
		this.referencingType = referencingType;
	}

	public void setReferencingField(String referencingField) {
		this.referencingField = referencingField;
	}
}