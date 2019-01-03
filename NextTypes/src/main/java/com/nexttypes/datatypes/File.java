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
import com.nexttypes.datatypes.JSON.JSONObject;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Utils;

@JsonPropertyOrder({ KeyWords.CONTENT, KeyWords.CONTENT_TYPE })
public class File extends PGobject{
	private static final long serialVersionUID = 1L;
	
	protected BodyContentHandler handler;
	protected Metadata metadata;
	
	protected String name;
	protected byte[] content;
	protected String contentType;
	
	public File() {
		type = PT.FILE;
	}
	
	public File(File file) {
		type = PT.FILE;
		
		this.handler = file.getHandler();
		this.metadata = file.getMetadata();
		
		this.name = file.getName();
		this.content = file.getContent();
		this.contentType = file.getContentType();
	}

	public File(byte[] content) {
		this(null, content);
	}
	
	public File(String name, byte[] content) {
		type = PT.FILE;
		
		this.name = name;
		this.content = content;
		
		init();
	}
	
	public File(JSONObject file) {
		this(file.getBinary(KeyWords.CONTENT), file.getString(KeyWords.CONTENT_TYPE));
	}
	
	@JsonCreator
	public File(@JsonProperty(KeyWords.CONTENT) byte[] content,
			@JsonProperty(KeyWords.CONTENT_TYPE) String contentType) {
		type = PT.FILE;

		this.content = content;
		this.contentType = contentType;
	}
	
	protected void init() {
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
	
	public BodyContentHandler getHandler() {
		if (handler == null) {
			init();
		}
		return handler;
	}

	public Metadata getMetadata() {
		if (metadata == null) {
			init();
		}
		return metadata;
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

	@JsonProperty(KeyWords.CONTENT)
	public byte[] getContent() {
		return content;
	}
	
	@JsonProperty(KeyWords.CONTENT_TYPE)
	public String getContentType() {
		return contentType;
	}
}