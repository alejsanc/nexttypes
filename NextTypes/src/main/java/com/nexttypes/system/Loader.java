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

package com.nexttypes.system;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Properties;

import com.nexttypes.enums.NodeMode;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.nodes.Node;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.settings.Permissions;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.views.HTMLView;
import com.nexttypes.views.View;
import com.nexttypes.views.WebDAVView;

public class Loader {
	public static Object load(String className) {
		return Loader.load(className, new Class[] {}, new Object[] {});
	}

	public static Object load(String className, Class parameterType, Object parameter) {
		return Loader.load(className, new Class[] { parameterType }, new Object[] { parameter });
	}

	public static Object load(String className, Class[] parameterTypes, Object[] parameters) {
		try {
			ClassLoader classLoader = Loader.class.getClassLoader();
			Class c = classLoader.loadClass(className);
			return c.getDeclaredConstructor(parameterTypes).newInstance(parameters);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			
			if (cause instanceof NXException) {
				throw (NXException) cause;
			} else {
				throw new NXException(cause);
			}
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException
				| NoSuchMethodException e) {
			throw new NXException(e);
		}
	}

	public static Node loadNode(String className, String user, String[] groups, NodeMode mode, String lang,
			String remoteAddress, Context context, boolean useConnectionPool) {
		return (Node) Loader.load(className,
				new Class[] { String.class, String[].class, NodeMode.class, String.class, String.class, Context.class,
						boolean.class },
				new Object[] { user, groups, mode, lang, remoteAddress, context, useConnectionPool });
	}

	public static Node loadNode(String className, HTTPRequest request, NodeMode mode) {
		return (Node) Loader.load(className, new Class[] { HTTPRequest.class, NodeMode.class },
				new Object[] { request, mode });
	}
	
	public static void initNode(String className, Context context) {
		Loader.load(className, Context.class, context);
	}

	public static View loadView(String className, HTTPRequest request) {
		return (View) Loader.load(className, HTTPRequest.class, request);
	}

	public static HTMLView loadHTMLView(String className, String type, HTMLView parent) {
		return (HTMLView) Loader.load(className,
				new Class[] { String.class, HTMLView.class},
				new Object[] { type, parent });
	}
	
	public static WebDAVView loadWebDAVView(String className, HTTPRequest request) {
		return (WebDAVView) Loader.load(className, HTTPRequest.class, request);
	}

	public static Controller loadController(String className, String type, String user, String[] groups,
			Node nextNode) {
		return (Controller) Loader.load(className,
				new Class[] { String.class, String.class, String[].class, Node.class },
				new Object[] { type, user, groups, nextNode });
	}

	public static Task loadTask(String className, Context context) {
		return (Task) Loader.load(className, Context.class, context);
	}
	
	public static Permissions loadPermissions(String className, ArrayList<Properties> settings,
			String user, String[] groups, Node nextNode) {
		return (Permissions) Loader.load(className,
				new Class[] { ArrayList.class, String.class, String[].class, Node.class },
				new Object[] { settings, user, groups, nextNode });
	}
}