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

public class InvalidValueException extends NXException {
	private static final long serialVersionUID = 1L;

	protected Object value;

	public InvalidValueException(String setting, Object value) {
		super(setting);
		this.value = value;
	}

	public InvalidValueException(String type, String setting, Object value) {
		super(type, setting);
		this.value = value;
	}

	@Override
	public String getMessage(Strings strings) {
		return strings.gts(type, setting) + ": " + value;
	}

	public Object getValue() {
		return value;
	}
}
