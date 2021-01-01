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

public class FieldReference {
	protected String referencingField;
	protected String referencedType;
	protected String referencedId;

	public FieldReference(String referencingField, String referencedType, String referencedId) {
		this.referencingField = referencingField;
		this.referencedType = referencedType;
		this.referencedId = referencedId;
	}

	public String getReferencingField() {
		return referencingField;
	}

	public String getReferencedType() {
		return referencedType;
	}

	public String getReferencedId() {
		return referencedId;
	}

	public void setReferencingField(String referencingField) {
		this.referencingField = referencingField;
	}

	public void setReferencedType(String referencedType) {
		this.referencedType = referencedType;
	}

	public void setReferencedId(String referencedId) {
		this.referencedId = referencedId;
	}
}