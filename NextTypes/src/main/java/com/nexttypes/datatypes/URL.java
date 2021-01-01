/*
 * Copyright 2015-2021 Alejandro Sánchez <alex@nexttypes.com>
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

import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.system.KeyWords;

public class URL {
	public static final String LOCALHOST = "127.0.0.1";
	public static final String HTTPS = "https";
	public static final String ROBOTS_FILE = "robots.txt";
	public static final String SITEMAP_FILE = "sitemap.xml";

	protected URIBuilder url;

	public URL(HttpServletRequest request) {
		String urlString = request.getRequestURL().toString();
		String queryString = request.getQueryString();

		if (queryString != null && queryString.length() > 0) {
			urlString += "?" + queryString;
		}

		setURL(urlString);
	}

	public URL(String url) {
		setURL(url);
	}

	protected void setURL(String url) {
		try {
			this.url = new URIBuilder(url);
		} catch (URISyntaxException e) {
			throw new InvalidValueException(KeyWords.INVALID_URL, url);
		}
	}

	public String getScheme() {
		return url.getScheme();
	}

	public String getPath() {
		return url.getPath();
	}

	public String getHost() {
		return url.getHost();
	}

	public String getParameter(String name) {
		String value = null;

		for (NameValuePair parameter : url.getQueryParams()) {
			if (parameter.getName().equals(name)) {
				value = parameter.getValue();
				break;
			}
		}

		return value;
	}

	public List<NameValuePair> getParameters() {
		return url.getQueryParams();
	}

	public int getPort() {
		return url.getPort();
	}

	public void setScheme(String scheme) {
		url.setScheme(scheme);
	}

	public void setPath(String path) {
		url.setPath(path);
	}

	public void setHost(String host) {
		url.setHost(host);
	}

	public void setPort(int port) {
		url.setPort(port);
	}

	public void setParameter(String parameter, String value) {
		url.setParameter(parameter, value);
	}

	public void setParameters(List<NameValuePair> parameters) {
		url.setParameters(parameters);
	}

	public String getRoot() {
		URIBuilder root = new URIBuilder();
		root.setScheme(url.getScheme());
		root.setHost(url.getHost());
		root.setPort(url.getPort());
		return root.toString();
	}

	public boolean isRobots() {
		return url.getPath().equals("/" + ROBOTS_FILE);
	}

	public boolean isSitemap() {
		return url.getPath().equals("/" + SITEMAP_FILE);
	}

	@Override
	public String toString() {
		return url.toString();
	}
}
