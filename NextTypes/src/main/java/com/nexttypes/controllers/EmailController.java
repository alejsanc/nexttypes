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

package com.nexttypes.controllers;

import java.time.ZonedDateTime;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.URI;
import com.nexttypes.enums.Format;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.interfaces.Node;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Controller;

public class EmailController extends Controller {

	public static final String MAIL_SMTP_HOST = "mail.smtp.host";
	public static final String FROM = "from";
	public static final String TO = "to";
	public static final String SUBJECT = "subject";
	
	public EmailController(String type, String[] objects, String user, String[] groups, Node nextNode) {
		super(type, objects, user, groups, nextNode);
	}

	@Override
	public ZonedDateTime insert(NXObject object) {
		Properties properties = System.getProperties();
		properties.setProperty(MAIL_SMTP_HOST, URI.LOCALHOST);
		Session session = Session.getDefaultInstance(properties);

		ZonedDateTime udate = nextNode.insert(object);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(object.getEmail(FROM));
			message.addRecipient(Message.RecipientType.TO, object.getEmail(TO));
			message.setSubject(object.getString(SUBJECT));
			message.setContent(object.getHTML(Constants.MESSAGE).toString(), Format.XHTML.getContentType());
			Transport.send(message);

		} catch (MessagingException e) {
			throw new NXException(e);
		}

		return udate;
	}
}
