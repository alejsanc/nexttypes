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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.postgresql.util.PGobject;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.interfaces.ComplexType;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Utils;

@JsonPropertyOrder({ Constants.CONTENT, Constants.CONTENT_TYPE })
public class Video extends PGobject implements ComplexType {
	private static final long serialVersionUID = 1L;

	protected BodyContentHandler handler;
	protected Metadata metadata;

	protected byte[] content;
	protected String contentType;
	protected String name;

	public Video() {
		type = PT.VIDEO;
	}

	public Video(File file) {
		this(file.getContent());
		name = file.getName();
	}

	public Video(byte[] content) {
		type = PT.VIDEO;

		this.content = content;

		video();
	}

	@JsonCreator
	public Video(@JsonProperty(Constants.CONTENT) byte[] content,
			@JsonProperty(Constants.CONTENT_TYPE) String contentType) {
		type = PT.VIDEO;

		this.content = content;
		this.contentType = contentType;
	}

	protected void video() {
		try (ByteArrayInputStream input = new ByteArrayInputStream(content)) {
			AutoDetectParser parser = new AutoDetectParser();
			handler = new BodyContentHandler(-1);
			metadata = new Metadata();
			parser.parse(input, handler, metadata);
			contentType = metadata.get(Metadata.CONTENT_TYPE);
		} catch (IOException | TikaException | SAXException e) {
			throw new NXException(e);
		}
	}

	@JsonProperty(Constants.CONTENT)
	@Override
	public byte[] getContent() {
		return content;
	}

	@JsonProperty(Constants.CONTENT_TYPE)
	@Override
	public String getContentType() {
		return contentType;
	}

	public BodyContentHandler getHandler() {
		if (handler == null) {
			video();
		}
		return handler;
	}

	public Metadata getMetadata() {
		if (metadata == null) {
			video();
		}
		return metadata;
	}

	public String getTitle() {
		return getMetadata().get(TikaCoreProperties.TITLE);
	}

	public String getDescription() {
		return getMetadata().get(TikaCoreProperties.DESCRIPTION);
	}

	public String getCreator() {
		return getMetadata().get(TikaCoreProperties.CREATOR);
	}

	@Override
	public String getValue() {
		return "(" + Utils.hexEncode(content) + "," + contentType + ")";
	}

	@Override
	public void setValue(String value) {
		if (value != null && value.length() > 0) {
			int token = value.indexOf(',');

			content = Utils.hexDecode(value.substring(1, token));
			contentType = value.substring(token + 1, value.length() - 1);
		}
	}

	public String getName() {
		return name;
	}
}