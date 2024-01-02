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

package com.nexttypes.protocol.http;

import com.nexttypes.datatypes.URL;

public class HTTPRobots {

	public static final String USER_AGENT = "User-agent";
	public static final String DISALLOW = "Disallow";
	public static final String ALLOW = "Allow";
	public static final String SITEMAP = "Sitemap";

	protected StringBuilder robots = new StringBuilder();

	public HTTPRobots(String host, Integer port, String[] types) {

		robots.append(USER_AGENT + ": *\n");
		robots.append(DISALLOW + ": /\n");

		if (types != null) {
			for (String type : types) {
				robots.append(ALLOW + ": /" + type + "/\n");
			}
		}

		robots.append(SITEMAP + ": " + URL.HTTPS + "://" + host);

		if (port != null && port != 443) {
			robots.append(":" + port);
		}

		robots.append("/" + URL.SITEMAP_FILE);
	}

	@Override
	public String toString() {
		return robots.toString();
	}
}