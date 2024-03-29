/*
 * Copyright 2015-2024 Alejandro Sánchez <alex@nexttypes.com>
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

import com.nexttypes.settings.LanguageSettings;

public class Message {
	protected String setting;
	protected Object value;

	public Message(String setting) {
		this(setting, null);
	}

	public Message(String setting, Object value) {
		this.setting = setting;
		this.value = value;
	}

	public String getMessage(LanguageSettings languageSettings) {
		String message = languageSettings.gts(setting);

		if (value != null) {
			message += ": " + value;
		}

		return message;
	}
}
