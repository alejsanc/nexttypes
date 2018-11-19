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

package com.nexttypes.logging;

import java.util.logging.Level;

import com.nexttypes.datatypes.Message;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.Strings;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Context;

public class Logger extends java.util.logging.Logger {

	protected Settings settings;
	protected Strings strings;
	protected FileHandler handler;

	public Logger(Context context) {
		super(null, null);
		settings = context.getSettings(Settings.LOGGER_SETTINGS);
		strings = context.getStrings(settings.getString(Constants.LANG));
		setLevel(Level.parse(settings.getString(Constants.LEVEL).toUpperCase()));
		handler = new FileHandler(settings);
		addHandler(handler);
	}

	public void info(Object source, String user, String remoteAddress, String message) {
		log(Level.INFO, source, user, remoteAddress, message);
	}

	public void warning(Object source, String user, String remoteAddress, String message) {
		log(Level.WARNING, source, user, remoteAddress, message);
	}

	public void severe(Object source, String user, String remoteAddress, String message) {
		log(Level.SEVERE, source, user, remoteAddress, message);
	}

	public void info(String user, String remoteAddress, Exception e) {
		log(Level.INFO, user, remoteAddress, e);
	}

	public void warning(String user, String remoteAddress, Exception e) {
		log(Level.WARNING, user, remoteAddress, e);
	}

	public void severe(String user, String remoteAddress, Exception e) {
		log(Level.SEVERE, user, remoteAddress, e);
	}

	public void info(Object source, String user, String remoteAddress, Message message) {
		log(Level.INFO, source, user, remoteAddress, message);
	}

	public void warning(Object source, String user, String remoteAddress, Message message) {
		log(Level.WARNING, source, user, remoteAddress, message);
	}

	public void severe(Object source, String user, String remoteAddress, Message message) {
		log(Level.SEVERE, source, user, remoteAddress, message);
	}

	public void log(Level level, Object source, String user, String remoteAddress, String message) {
		log(level, source.getClass().getName(), user, remoteAddress, message);
	}

	public void log(Level level, String user, String remoteAddress, Exception e) {
		String message = null;

		if (e instanceof NXException) {
			message = ((NXException) e).getMessage(strings);
		} else {
			message = NXException.getMessage(e);
		}
		
		String sourceClass = null;
		
		StackTraceElement[] stackTraceElements = e.getStackTrace();
		if (stackTraceElements != null && stackTraceElements.length > 0) {
			sourceClass = stackTraceElements[0].getClassName();
		} else {
			sourceClass = "-";
		}

		log(level, sourceClass, user, remoteAddress, message);
	}

	public void log(Level level, Object source, String user, String remoteAddress, Message message) {
		log(level, source.getClass().getName(), user, remoteAddress, message.getMessage(strings));
	}

	public void log(Level level, String sourceClass, String user, String remoteAddress, String message) {
		log(level, sourceClass + " " + user + " " + remoteAddress + " " + message);
	}

	public void close() {
		handler.close();
	}
}