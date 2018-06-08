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

package com.nexttypes.exceptions;

import com.nexttypes.settings.Strings;

public class NXException extends RuntimeException {
	protected static final long serialVersionUID = 1L;

	protected String type;
	protected String setting;
	protected Exception parentException;

	public NXException(String setting) {
		this(null, setting);
	}

	public NXException(String type, String setting) {
		this.type = type;
		this.setting = setting;
	}

	public NXException(Exception parentException) {
		this.parentException = parentException;
	}

	public String getType() {
		return type;
	}

	public String getSetting() {
		return setting;
	}

	public Exception getParentException() {
		return parentException;
	}

	public String getMessage(Strings strings) {
		String message = null;

		if (parentException != null) {
			message = parentException.getMessage();
			if (message == null) {
				message = parentException.getClass().getName();
			}
		} else {
			message = strings.gts(type, setting);
		}

		return message;
	}
}