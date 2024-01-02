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

package com.nexttypes.exceptions;

import com.nexttypes.settings.LanguageSettings;
import com.nexttypes.system.KeyWords;

public class CertificateNotFoundException extends NXException {
	private static final long serialVersionUID = 1L;

	protected String subject;

	public CertificateNotFoundException(String subject) {
		super(KeyWords.CERTIFICATE_NOT_FOUND);
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	@Override
	public String getMessage(LanguageSettings languageSettings) {
		return languageSettings.gts(KeyWords.CERTIFICATE_NOT_FOUND) + ": " + subject;
	}
}