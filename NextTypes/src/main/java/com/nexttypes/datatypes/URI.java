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

import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.system.Constants;

public class URI {
	public static final String LOCALHOST = "127.0.0.1";
	public static final String HTTPS = "https";
	public static final String ROBOTS_FILE = "robots.txt";
	public static final String SITEMAP_FILE = "sitemap.xml";

	protected URIBuilder uri;

	public URI(HttpServletRequest request) {
		String uriString = request.getRequestURL().toString();
		String queryString = request.getQueryString();

		if (queryString != null && queryString.length() > 0) {
			uriString += "?" + queryString;
		}

		setURI(uriString);
	}

	public URI(String uri) {
		setURI(uri);
	}

	protected void setURI(String uri) {
		try {
			this.uri = new URIBuilder(uri);
		} catch (URISyntaxException e) {
			throw new InvalidValueException(Constants.INVALID_URI, uri);
		}
	}

	public String getScheme() {
		return uri.getScheme();
	}

	public String getPath() {
		return uri.getPath();
	}

	public String getHost() {
		return uri.getHost();
	}

	public String getParameter(String name) {
		String value = null;

		for (NameValuePair parameter : uri.getQueryParams()) {
			if (parameter.getName().equals(name)) {
				value = parameter.getValue();
				break;
			}
		}

		return value;
	}

	public List<NameValuePair> getParameters() {
		return uri.getQueryParams();
	}

	public int getPort() {
		return uri.getPort();
	}

	public void setScheme(String scheme) {
		uri.setScheme(scheme);
	}

	public void setPath(String path) {
		uri.setPath(path);
	}

	public void setHost(String host) {
		uri.setHost(host);
	}

	public void setPort(int port) {
		uri.setPort(port);
	}

	public void setParameter(String parameter, String value) {
		uri.setParameter(parameter, value);
	}

	public void setParameters(List<NameValuePair> parameters) {
		uri.setParameters(parameters);
	}

	public String getRoot() {
		URIBuilder root = new URIBuilder();
		root.setScheme(uri.getScheme());
		root.setHost(uri.getHost());
		root.setPort(uri.getPort());
		return root.toString();
	}

	public boolean isRobots() {
		return uri.getPath().equals("/" + ROBOTS_FILE);
	}

	public boolean isSitemap() {
		return uri.getPath().equals("/" + SITEMAP_FILE);
	}

	@Override
	public String toString() {
		return uri.toString();
	}
}
