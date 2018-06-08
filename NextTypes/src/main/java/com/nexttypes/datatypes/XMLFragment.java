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

import java.util.LinkedHashMap;

import org.w3c.dom.NodeList;

public class XMLFragment extends XML {
	private static final long serialVersionUID = 1L;

	public static final String FRAGMENT = "fragment";

	public XMLFragment(String xml, String lang) {
		this(xml, lang, (LinkedHashMap<String, String[]>) null);
	}

	public XMLFragment(String xml, String lang, String allowedTags) {
		this(xml, lang, parseAllowedTags(allowedTags));
	}

	public XMLFragment(String xml, String lang, LinkedHashMap<String, String[]> allowedTags) {
		super("<" + FRAGMENT + ">" + xml + "</" + FRAGMENT + ">", lang, addFragmentTag(allowedTags));
		setXMLDeclaration(false);
		setIndentation(false);
	}

	@Override
	public String toString() {
		StringBuilder fragment = new StringBuilder();
		NodeList nodes = document.getDocumentElement().getChildNodes();
		for (int x = 0; x < nodes.getLength(); x++) {
			fragment.append(nodeToString(nodes.item(x)));
		}
		return fragment.toString();
	}

	protected static LinkedHashMap<String, String[]> addFragmentTag(LinkedHashMap<String, String[]> allowedTags) {
		if (allowedTags != null) {
			allowedTags.put(FRAGMENT, new String[] {});
		}
		return allowedTags;
	}
}