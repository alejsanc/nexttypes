/*
 * Copyright 2015-2026 Alejandro Sánchez <alex@nexttypes.com>
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

package com.nexttypes.protocol.http;

import java.util.LinkedHashMap;

import com.nexttypes.datatypes.ObjectInfo;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.URL;
import com.nexttypes.datatypes.XML;

public class HTTPSitemap extends XML {
	private static final long serialVersionUID = 1L;

	public static final String URLSET = "urlset";
	public static final String LOC = "loc";
	public static final String LASTMOD = "lastmod";

	public HTTPSitemap(String host, Integer port, LinkedHashMap<String, ObjectInfo[]> objectsInfo) {
		super();

		String portString = port != null && port != 443 ? ":" + port : "";

		Element urlSet = setDocumentElement(URLSET);
		urlSet.setAttribute(XMLNS, "https://www.sitemaps.org/schemas/sitemap/0.9");

		for (String type : objectsInfo.keySet()) {
			for (ObjectInfo info : objectsInfo.get(type)) {
				Element url = urlSet.appendElement(PT.URL);
				url.appendElement(LOC).appendText(URL.HTTPS + "://" + host
						+ portString + "/" + type + "/" + info.getId());
				url.appendElement(LASTMOD).appendText(info.getUDate().toString());
			}
		}
	}
}