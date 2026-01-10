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

package com.nexttypes.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.system.KeyWords;

public class ObjectsStreamSerializer extends JsonSerializer<ObjectsStream> {
	@Override
	public void serialize(ObjectsStream objects, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		try (ObjectsStream o = objects) {
			objects.exec();

			generator.writeStartObject();
			generator.writeStringField(KeyWords.FORMAT, objects.getFormat());
			generator.writeStringField(KeyWords.VERSION, objects.getVersion());
			generator.writeStringField(KeyWords.TYPE, objects.getType());
			generator.writeNumberField(KeyWords.COUNT, objects.getCount());
			generator.writeFieldName(KeyWords.ITEMS);
			generator.writeStartArray();

			while (objects.next()) {
				generator.writeObject(objects.getItem());
			}

			generator.writeEndArray();
			generator.writeEndObject();
		}
	}
}