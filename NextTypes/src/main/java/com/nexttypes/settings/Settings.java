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

package com.nexttypes.settings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Properties;

import com.nexttypes.datatypes.Tuple;
import com.nexttypes.system.Utils;

public class Settings {

	public static final String LOGGER_SETTINGS = "logger.properties";
	public static final String CONTROLLERS_SETTINGS = "controllers.properties";
	public static final String POSTGRESQL_SETTINGS = "postgresql.properties";
	public static final String HTTP_SETTINGS = "http.properties";
	public static final String SMTP_SETTINGS = "smtp.properties";
	public static final String BACKUP_SETTINGS = "backup.properties";
	public static final String CONSOLE_SETTINGS = "console.properties";
	public static final String TASKS_SETTINGS = "tasks.properties";
	public static final String HTML_SETTINGS = "html.properties";
	public static final String ICALENDAR_SETTINGS = "ical.properties";
	public static final String RSS_SETTINGS = "rss.properties";
	public static final String SERIAL_SETTINGS = "serial.properties";
	public static final String WEBDAV_SETTINGS = "webdav.properties";
	public static final String TYPES_SETTINGS = "types.properties";
	public static final String PERMISSIONS_SETTINGS = "permissions.properties";

	public static final String DEFAULT_SETTINGS = "/com/nexttypes/settings/defaults/";

	protected ArrayList<Properties> settings;

	public Settings(ArrayList<Properties> settings) {
		this.settings = settings;
	}

	public String getString(String prefix, String[] settings) {
		String value = null;

		for (String setting : settings) {
			value = getString(prefix + "." + setting);
			if (value != null) {
				break;
			}
		}

		return value;
	}

	public String getString(String setting) {
		String value = null;

		for (Properties file : settings) {
			value = Utils.trim(file.getProperty(setting));

			if (value != null) {
				break;
			}
		}

		return value;
	}

	public String[] getStringArray(String setting) {
		return Utils.split(getString(setting), ",");
	}

	public Short getInt16(String setting) {
		return Tuple.parseInt16(getString(setting));
	}

	public Integer getInt32(String setting) {
		return Tuple.parseInt32(getString(setting));
	}

	public Long getInt64(String setting) {
		return Tuple.parseInt64(getString(setting));
	}

	public Float getFloat32(String setting) {
		return Tuple.parseFloat32(getString(setting));
	}

	public Double getFloat64(String setting) {
		return Tuple.parseFloat64(getString(setting));
	}

	public BigDecimal getNumeric(String setting) {
		return Tuple.parseNumeric(getString(setting));
	}

	public Boolean getBoolean(String setting) {
		return Tuple.parseBoolean(getString(setting));
	}

	public Tuple getTuple(String setting) {
		return Tuple.parseTuple(getString(setting));
	}
}