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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Serial;
import com.nexttypes.datatypes.URL;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.NodeMode;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.interfaces.TypesStream;
import com.nexttypes.logging.Logger;
import com.nexttypes.nodes.Node;
import com.nexttypes.settings.Settings;

public class BackupTask extends Task {

	public static final String INCR = "incr";

	protected Context context;
	protected Settings settings;
	protected String directory;
	protected String prefix;
	protected String lang;
	protected long interval;
	protected int incremental;
	protected int count = 0;
	protected Logger logger;
	protected boolean finished = false;
	protected boolean running = false;
	protected long previousFileTime = 0;

	public BackupTask(Context context) {
		this.context = context;
		settings = context.getSettings(Settings.BACKUP_SETTINGS);
		directory = Utils.readDirectory(settings.getString(KeyWords.DIRECTORY));
		prefix = settings.getString(KeyWords.PREFIX);
		lang = settings.getString(KeyWords.LANG);
		interval = settings.getInt32(KeyWords.INTERVAL) * Constants.MINUTE_MILLISECONDS;
		incremental = settings.getInt32(KeyWords.INCREMENTAL);
		logger = context.getLogger();

		ArrayList<String> filesByDate = new ArrayList<>();

		try (DirectoryStream<Path> files = Files.newDirectoryStream(Paths.get(directory))) {
			for (Path file : files) {
				String fileName = file.getFileName().toString();
				filesByDate.add(fileName.substring(prefix.length(), prefix.length() + 29));
			}
		} catch (IOException e) {
			throw new NXException(e);
		}

		if (filesByDate.size() > 0) {

			filesByDate.sort(Collections.reverseOrder());

			previousFileTime = Duration.between(ZonedDateTime.parse(filesByDate.get(0).substring(0, 24)),
					ZonedDateTime.now(ZoneOffset.UTC)).toMillis();

			if (incremental > 0) {
				for (String file : filesByDate) {
					if (file.substring(file.length() - 4, file.length()).equals(INCR)) {
						count++;
					} else {
						break;
					}

					if (count == incremental) {
						break;
					}
				}
			}
		}
	}

	public void run() {
		try {
			long initTime;

			if (previousFileTime > 0) {
				if (previousFileTime >= interval) {
					initTime = 0;
				} else {
					initTime = interval - previousFileTime;
				}
			} else {
				initTime = interval;
			}

			sleep(initTime);

			backup();

			while (!finished) {

				sleep(interval);

				backup();

			}
		} catch (InterruptedException e) {
			if (!finished) {
				logger.severe(Auth.BACKUP, URL.LOCALHOST, e);
			}
		}
	}

	public void backup() {
		running = true;

		try (Node nextNode = Loader.loadNode(settings.getString(KeyWords.NEXT_NODE),
				new Auth(Auth.BACKUP), NodeMode.WRITE, lang, URL.LOCALHOST, context, true)) {

			ZonedDateTime dateTime = ZonedDateTime.now(ZoneOffset.UTC);

			StringBuilder filePath = new StringBuilder(directory + prefix + dateTime);

			boolean full;

			if (count == incremental) {
				filePath.append("-" + KeyWords.FULL);
				count = 0;
				full = true;
			} else {
				filePath.append("-" + INCR);
				count++;
				full = false;
			}

			filePath.append("." + Format.JSON.getExtension());

			try (TypesStream types = nextNode.backup(full)) {

				try (FileOutputStream file = new FileOutputStream(filePath.toString())) {
					new Serial(types, Format.JSON).write(file);
				}
			}

			nextNode.commit();

		} catch (Exception e) {
			logger.severe(Auth.BACKUP, URL.LOCALHOST, e);
		}

		running = false;
	}

	public void finish() {
		finished = true;

		if (!running) {
			interrupt();
		}
	}
}