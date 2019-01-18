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

package com.nexttypes.exceptions;

import com.nexttypes.settings.Strings;
import com.nexttypes.system.KeyWords;

public class UnauthorizedReferenceException extends UnauthorizedException {
	protected static final long serialVersionUID = 1L;

	protected String referencedType;
	protected String referencedId;
	protected String referencingType;
	protected String referencingId;
	protected String referencingField;
	
	public UnauthorizedReferenceException(String referencedType, String referencedId,
			String referencingType, String referencingId, String referencingField) {
		super(KeyWords.UNAUTHORIZED_REFERENCE);
		
		this.referencedType = referencedType;
		this.referencedId = referencedId;
		this.referencingType = referencingType;
		this.referencingId = referencingId;
		this.referencingField = referencingField;
	}
	
	public String getReferencedType() {
		return referencedType;
	}

	public String getReferencedId() {
		return referencedId;
	}
	
	public String getReferencingType() {
		return referencingType;
	}
	
	public String getReferencingId() {
		return referencingId;
	}
	
	public String getReferencingField() {
		return referencingField;
	}
	
	@Override
	public String getMessage(Strings strings) {
		String referencedTypeName = strings.getTypeName(referencedType);
		String referencingTypeName = strings.getTypeName(referencingType);
		String referencingFieldName = strings.getFieldName(referencingType, referencingField);
		
		StringBuilder message = new StringBuilder(strings.gts(type, KeyWords.UNAUTHORIZED_REFERENCE)
				+ ": " + referencingTypeName);
		
		if (referencingId != null) {
			message.append("::" + referencingId);
		}
		
		message.append("::" + referencingFieldName + " -> " + referencedTypeName + "::" + referencedId);
		
		return message.toString();
	}
}