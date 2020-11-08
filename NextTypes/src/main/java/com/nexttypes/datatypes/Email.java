/*
 * Copyright 2015-2020 Alejandro Sánchez <alex@nexttypes.com>
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

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import com.nexttypes.enums.Format;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.Constants;
import com.nexttypes.system.KeyWords;

public class Email {
	
	protected static final String MAIL_SMTP_HOST = "mail.smtp.host";
	public static final String FROM = "from";
	public static final String TO = "to";
	public static final String SUBJECT = "subject";
	
	protected MimeMessage message;
	
	public Email(Tuple tuple) {
		Properties properties = System.getProperties();
		properties.setProperty(MAIL_SMTP_HOST, URL.LOCALHOST);
		Session session = Session.getDefaultInstance(properties);

		message = new MimeMessage(session);
		
		try {
			message.setFrom(tuple.getEmail(FROM));
			message.addRecipient(Message.RecipientType.TO, tuple.getEmail(TO));
			message.setSubject(tuple.getString(SUBJECT));
			message.setContent(tuple.getHTML(KeyWords.MESSAGE).toString(), Format.HTML.getContentType()
					+ "; " + HTML.CHARSET + "=" + Constants.UTF_8_CHARSET);	
		} catch (MessagingException e) {
			throw new NXException(e);
		}
				
	}
	
	public void send() {
		try {
			Transport.send(message);
		} catch (MessagingException e) {
			throw new NXException(e);
		}
	}
	
	public MimeMessage getMessage() {
		return message;
	}
}
