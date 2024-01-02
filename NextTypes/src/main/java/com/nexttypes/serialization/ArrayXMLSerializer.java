/*
 * Copyright 2015-2024 Alejandro Sรกnchez <alex@nexttypes.com>
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

import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class ArrayXMLSerializer extends JsonSerializer<Object[]> {
	protected String itemName;

	public ArrayXMLSerializer(String itemName) {
		this.itemName = itemName;
	}

	@Override
	public void serialize(Object[] objects, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		ToXmlGenerator xmlGenerator = (ToXmlGenerator) generator;

		xmlGenerator.startWrappedValue(null, new QName(itemName));

		xmlGenerator.writeStartArray();

		for (Object object : objects) {
			xmlGenerator.writeObject(object);
		}

		xmlGenerator.writeEndArray();

		xmlGenerator.finishWrappedValue(null, new QName(itemName));
	}
}
