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

package com.nexttypes.interfaces;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.datatypes.Type;
import com.nexttypes.system.KeyWords;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonPropertyOrder({ KeyWords.FORMAT, KeyWords.VERSION, KeyWords.DATE, KeyWords.TYPES, KeyWords.OBJECTS })
public interface TypesStream extends Stream {
	
	@JsonProperty(KeyWords.DATE)
	public ZonedDateTime getDate();

	@JsonProperty(KeyWords.TYPES)
	public LinkedHashMap<String, Type> getTypes();

	@JsonProperty(KeyWords.OBJECTS)
	public LinkedHashMap<String, ObjectsStream> getObjects();

	public ObjectsStream getObjectsStream();
}
