/*
 * Copyright 2015-2024 Alejandro Sánchez <alex@nexttypes.com>
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

import java.io.IOException;
import java.io.OutputStream;

import jakarta.mail.internet.InternetAddress;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.IndexMode;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.serialization.ArrayXMLSerializer;
import com.nexttypes.system.KeyWords;

public class Serial {
	public static MapperFeature autoDetectMapperFeatures[] = {MapperFeature.AUTO_DETECT_CREATORS,
			MapperFeature.AUTO_DETECT_FIELDS, MapperFeature.AUTO_DETECT_GETTERS,
			MapperFeature.AUTO_DETECT_IS_GETTERS};
	
	protected Object object;
	protected ObjectMapper mapper;
	protected ObjectWriter writter;
	protected Format format;
	protected String rootName;
	protected String itemName;

	public Serial(Object object, String format) {
		this(object, Format.valueOf(format.toUpperCase()), null, null);
	}

	public Serial(Object object, Format format) {
		this(object, format, null, null);
	}
	
	public Serial(Object object, String format, String rootName) {
		this(object, Format.valueOf(format.toUpperCase()), rootName, null);
	}

	public Serial(Object object, String format, String rootName, String itemName) {
		this(object, Format.valueOf(format.toUpperCase()), rootName, itemName);
	}

	public Serial(Object object, Format format, String rootName, String itemName) {
		this.object = object;
		this.format = format;
		this.rootName = rootName;
		this.itemName = itemName;
		
		MapperBuilder builder;

		switch (format) {
		case JSON:
			builder = JsonMapper.builder();
			break;
			
		case SMILE:
			builder = SmileMapper.builder();
			break;
			
		case XML:
			builder = XmlMapper.builder();
			break;
			
		default:
			throw new InvalidValueException(KeyWords.INVALID_SERIAL_FORMAT, format);
		}
		
		mapper = builder.disable(autoDetectMapperFeatures)
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
						SerializationFeature.FAIL_ON_EMPTY_BEANS).build();
		
		mapper.registerModule(new JavaTimeModule());
		
		SimpleModule module = new SimpleModule();

		if (Format.XML.equals(format) && object instanceof Object[]) {
			Object[] objects = (Object[]) object;
			module.addSerializer(objects.getClass(), new ArrayXMLSerializer(itemName));
		}

		ToStringSerializer serializer = new ToStringSerializer();
		module.addSerializer(HTML.class, serializer);
		module.addSerializer(HTMLFragment.class, serializer);
		module.addSerializer(XML.class, serializer);
		module.addSerializer(XMLFragment.class, serializer);
		module.addSerializer(JSON.class, serializer);
		module.addSerializer(Color.class, serializer);
		module.addSerializer(InternetAddress.class, serializer);
		module.addSerializer(URL.class, serializer);
		module.addSerializer(IndexMode.class, serializer);

		mapper.registerModule(module);

		if (Format.XML.equals(format) && rootName != null) {
			writter = mapper.writer().withRootName(rootName).withDefaultPrettyPrinter();
		} else {
			writter = mapper.writer().withDefaultPrettyPrinter();
		}

	}

	public String getString() {
		try {
			return writter.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new NXException(e);
		}
	}

	public byte[] getBinary() {
		try {
			return writter.writeValueAsBytes(object);
		} catch (JsonProcessingException e) {
			throw new NXException(e);
		}
	}

	public void write(OutputStream output) {
		try {
			writter.writeValue(output, object);
		} catch (IOException e) {
			throw new NXException(e);
		}
	}

	public Format getFormat() {
		return format;
	}

	@Override
	public String toString() {
		return getString();
	}
}