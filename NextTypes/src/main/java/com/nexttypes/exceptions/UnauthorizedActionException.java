/*
 * Copyright 2015-2020 Alejandro Sánchez <alex@nexttypes.com>
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

import org.apache.commons.lang3.StringUtils;

import com.nexttypes.settings.LanguageSettings;
import com.nexttypes.system.KeyWords;

public class UnauthorizedActionException extends UnauthorizedException {
	protected static final long serialVersionUID = 1L;

	protected String type;
	protected String action;
	protected String[] objects;
	
	public UnauthorizedActionException(String type, String action) {
		this(type, (String[]) null, action);
	}
	
	public UnauthorizedActionException(String type, String id, String action) {
		this(type, id != null ? new String[] { id } : null, action);
	}
	
	public UnauthorizedActionException(String type, String[] objects, String action) {
		super(KeyWords.UNAUTHORIZED_ACTION);
		
		this.type = type;
		this.objects = objects;
		this.action = action;
	}
	
	public String getType() {
		return type;
	}
	
	public String[] getObjects() {
		return objects;
	}
	
	public String getAction() {
		return action;
	}

	@Override
	public String getMessage(LanguageSettings languageSettings) {
		StringBuilder message = new StringBuilder(languageSettings.gts(type, KeyWords.UNAUTHORIZED_ACTION) + ": ");
		
		if (type != null) {
			message.append(languageSettings.getTypeName(type) + "::");
		}
		
		message.append(languageSettings.getActionName(type, action));
		
		if (objects != null && objects.length > 0) {
			message.append(" -> " + StringUtils.join(objects, ","));
		}
		
		return message.toString();
	}
}