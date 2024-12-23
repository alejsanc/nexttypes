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

package com.nexttypes.serialization;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nexttypes.datatypes.Serial;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.KeyWords;

public class StreamDeserializer {
	protected JsonParser parser;

	public StreamDeserializer(JsonParser parser) {
		this.parser = parser;
	}

	public StreamDeserializer(InputStream input) {
		if (input == null) {
			throw new NXException(KeyWords.EMPTY_INPUT);
		}

		JsonFactory factory = new JsonFactory();
		try {
			parser = factory.createParser(input);
			JsonMapper mapper = JsonMapper.builder().disable(Serial.autoDetectMapperFeatures).build();
			mapper.registerModule(new JavaTimeModule());
			parser.setCodec(mapper);
		} catch (Exception e) {
			throw new NXException(KeyWords.INVALID_INPUT);
		}
	}
	
	protected void checkTag(String expectedTag) {
		try {
			String tag = parser.currentName();
			if (!expectedTag.equals(tag)) {
				throw new InvalidValueException(KeyWords.UNEXPECTED_TAG, tag);
			}
		} catch (IOException e) {
			throw new NXException(e);
		}
	}
}