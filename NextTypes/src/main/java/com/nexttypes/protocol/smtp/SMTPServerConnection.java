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

package com.nexttypes.protocol.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.enums.NodeMode;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.interfaces.Node;
import com.nexttypes.logging.Logger;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.Strings;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Context;
import com.nexttypes.system.Loader;

public class SMTPServerConnection extends Thread {
	protected String host;
	protected Settings settings;
	protected Strings strings;
	protected Socket socket;
	protected String remoteAddress;
	protected Context context;
	protected Logger logger;
	protected Node nextNode;
	protected BufferedReader input;
	protected PrintStream output;

	protected String mailFrom;
	protected StringBuilder rcptTo = new StringBuilder();
	protected StringBuilder data = new StringBuilder();

	public SMTPServerConnection(Socket socket, Context context, Settings settings, Strings strings, Logger logger) {
		this.socket = socket;
		this.remoteAddress = socket.getRemoteSocketAddress().toString();
		this.context = context;
		this.settings = settings;
		this.strings = strings;
		this.logger = logger;

		host = settings.getString(KeyWords.HOST);
	}

	public void init() {
		write("220 " + host + " NextTypes SMTP");
	}

	public void ok() {
		write("250 OK");
	}

	public void ehlo() {
		write("250-" + host);
		write("250 SIZE 10240000");
	}

	public void mailFrom(String command) {
		mailFrom = command.substring(10, command.length());
		ok();
	}

	public void rcptTo(String command) {
		rcptTo.append(command.substring(8, command.length()) + ",");
		ok();
	}

	public void data() throws IOException {
		write("354 Start mail input; end with <CRLF>.<CRLF>");

		String line = null;

		while (true) {
			line = input.readLine();
			if (line.equals(".")) {
				insertRawEmailObject();
				break;
			} else {
				data.append(line + "\n");
			}
		}
	}

	public void rset() {
		mailFrom = null;
		rcptTo = new StringBuilder();
		data = new StringBuilder();
		ok();
	}

	public void noop() {
		ok();
	}

	public void write(String message) {
		output.println(message);
	}

	public void quit() {
		write("221 " + host + " NextTypes SMTP closing connection.");
	}

	public void insertRawEmailObject() {
		try {
			NXObject object = new NXObject("raw_email");
			object.put("mail_from", mailFrom);
			object.put("rcpt_to", rcptTo.deleteCharAt(rcptTo.length() - 1).toString());
			object.put("data", data.toString());
			nextNode.insert(object);
			nextNode.commit();
			ok();
		} catch (NXException e) {
			logger.severe(Auth.SMTP, remoteAddress, e);
			error(e.getMessage(strings));
		}
	}

	public void error(String message) {
		write("451 Error: " + message);
	}

	public void run() {

		try (Node nextNode = Loader.loadNode(settings.getString(KeyWords.NEXT_NODE), Auth.SMTP, null, NodeMode.WRITE,
				settings.getString(KeyWords.LANG), remoteAddress, context, true);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintStream output = new PrintStream(socket.getOutputStream())) {

			this.nextNode = nextNode;
			this.input = input;
			this.output = output;

			init();

			String command = null;

			while (true) {
				command = input.readLine();

				if (command.toUpperCase().startsWith("EHLO")) {
					ehlo();
				} else if (command.toUpperCase().startsWith("MAIL FROM:")) {
					mailFrom(command);
				} else if (command.toUpperCase().startsWith("RCPT TO:")) {
					rcptTo(command);
				} else if (command.toUpperCase().startsWith("DATA")) {
					data();
				} else if (command.toUpperCase().startsWith("RSET")) {
					rset();
				} else if (command.toUpperCase().startsWith("NOOP")) {
					noop();
				} else if (command.toUpperCase().startsWith("QUIT")) {
					quit();
					break;
				}
			}

		} catch (Exception e) {
			logger.severe(Auth.SMTP, remoteAddress, e);

		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				logger.severe(Auth.SMTP, remoteAddress, e);
			}
		}
	}
}