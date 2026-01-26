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

package com.nexttypes.protocol.smtp;

import java.net.InetAddress;
import java.net.ServerSocket;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.URL;
import com.nexttypes.logging.Logger;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.LanguageSettings;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Context;

public class SMTPServer extends Thread {
	protected Settings settings;
	protected LanguageSettings languageSettings;
	protected ServerSocket socket;
	protected Context context;
	protected Logger logger;
	protected boolean open;

	public SMTPServer(Context context) {
		try {
			this.context = context;
			logger = context.getLogger();
			settings = context.getSettings(Settings.SMTP_SETTINGS);
			languageSettings = context.getLanguageSettings(settings.getString(KeyWords.LANG));
			socket = new ServerSocket(settings.getInt32(KeyWords.PORT), settings.getInt32(Settings.BACKLOG),
					InetAddress.getByName((settings.getString(Settings.BIND_ADDRESS))));
			open = true;
		} catch (Exception e) {
			logger.severe(Auth.SMTP, URL.LOCALHOST, e);
		}
	}

	public void run() {
		while (!socket.isClosed()) {
			try {
				SMTPServerConnection connection = new SMTPServerConnection(socket.accept(), context, settings, languageSettings,
						logger);
				connection.start();
			} catch (Exception e) {
				if (open) {
					logger.severe(Auth.SMTP, URL.LOCALHOST, e);
				}
			}
		}
	}

	public void close() {
		open = false;

		try {
			socket.close();
		} catch (Exception e) {
			logger.severe(Auth.SMTP, URL.LOCALHOST, e);
		}
	}
}
