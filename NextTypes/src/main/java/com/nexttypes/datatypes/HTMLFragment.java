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

package com.nexttypes.datatypes;

import java.util.LinkedHashMap;

public class HTMLFragment extends XMLFragment {
	private static final long serialVersionUID = 1L;

	public HTMLFragment(String html, String lang) {
		super(html, lang, (LinkedHashMap<String, String[]>) null);
	}

	public HTMLFragment(String html, String lang, String allowedTags) {
		super(html, lang, allowedTags);
	}

	public HTMLFragment(byte[] html, String lang, String allowedTags) {
		super(html, lang, allowedTags);
	}
}