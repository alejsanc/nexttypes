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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.logging.LogRecord;

import com.nexttypes.settings.Settings;
import com.nexttypes.system.Constants;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Utils;

public class FileHandler extends java.util.logging.Handler {
	protected String directory;
	protected String prefix;
	protected PrintStream logger;
	protected LocalDate loggerDate;

	public FileHandler(Settings settings) {
		directory = Utils.readDirectory(settings.getString(KeyWords.DIRECTORY));
		prefix = settings.getString(KeyWords.PREFIX);
	}

	@Override
	public void close() throws SecurityException {
		if (logger != null) {
			logger.close();
		}
	}

	@Override
	public void flush() {
		if (logger != null) {
			logger.flush();
		}
	}

	@Override
	public synchronized void publish(LogRecord record) {
		try {
			ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(record.getMillis()), ZoneOffset.UTC);
			LocalDate date = dateTime.toLocalDate();

			if (!date.equals(loggerDate)) {
				if (logger != null) {
					logger.close();
				} 

				FileOutputStream file = new FileOutputStream(directory + prefix + date + ".log", true);
				logger = new PrintStream(file, true, Constants.UTF_8_CHARSET);
				loggerDate = date;
			}

			logger.println(dateTime + " " + record.getLevel() + " " + record.getMessage());

		} catch (IOException e) {
			System.err.println("NextTypes Logger Error:" + e.getMessage());
		}
	}
}