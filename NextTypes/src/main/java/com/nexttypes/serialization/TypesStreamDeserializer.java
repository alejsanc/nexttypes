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

package com.nexttypes.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.Type;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.exceptions.NotImplementedException;
import com.nexttypes.interfaces.Node;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.interfaces.TypesStream;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.system.KeyWords;

public class TypesStreamDeserializer extends StreamDeserializer implements TypesStream {
	protected String version;
	protected LinkedHashMap<String, Type> types;
	protected ZonedDateTime date;
	protected String lang;
	protected Node nextNode;
	protected TypeSettings typeSettings;
	protected Strings strings;

	public TypesStreamDeserializer(InputStream input, String lang, Node nextNode, TypeSettings typeSettings,
			Strings strings) {
		super(input);
		this.lang = lang;
		this.nextNode = nextNode;
		this.typeSettings = typeSettings;
		this.strings = strings;
	}
	
	@Override
	public String getFormat() {
		return NEXTTYPES_TYPES;
	}
	
	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void exec() {
		try {
			parser.nextToken();
			parser.nextToken();
			parser.nextToken();
			
			checkTag(KeyWords.FORMAT);
			
			String format = parser.getText();
			if (!NEXTTYPES_TYPES.equals(format)) {
				throw new InvalidValueException(KeyWords.INVALID_STREAM_FORMAT, format);
			}
			
			parser.nextToken();
			parser.nextToken();
			
			checkTag(KeyWords.VERSION);
			
			version = parser.getText();
			
			parser.nextToken();
			parser.nextToken();
			
			checkTag(KeyWords.DATE);

			date = Tuple.parseUTCDateTime(parser.getText());

			parser.nextToken();
			parser.nextToken();
			
			checkTag(KeyWords.TYPES);

			types = parser.readValueAs(new TypeReference<LinkedHashMap<String, Type>>() {});

			parser.nextToken();
			parser.nextToken();
			
			checkTag(KeyWords.OBJECTS);

		} catch (IOException e) {
			throw new NXException(e);
		}
	}

	@Override
	public boolean next() {
		try {
			if (parser.nextToken() == JsonToken.FIELD_NAME) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			throw new NXException(e);
		}
	}

	@Override
	public ObjectsStream getObjectsStream() {
		return new ObjectsStreamDeserializer(parser, lang, true, nextNode, typeSettings, strings);
	}

	@Override
	public void close() {
		try {
			parser.close();
		} catch (IOException e) {
			throw new NXException(e);
		}
	}

	@Override
	public LinkedHashMap<String, Type> getTypes() {
		return types;
	}

	@Override
	public LinkedHashMap<String, ObjectsStream> getObjects() {
		throw new NotImplementedException();
	}

	@Override
	public ZonedDateTime getDate() {
		return date;
	}
}