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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.nexttypes.system.KeyWords;

@JacksonXmlRootElement(localName = KeyWords.OBJECTS)
public class Objects extends Result {

	protected NXObject[] items;

	public Objects() {
		this(null, 0L, null, null, null, null, null);
	}
	
	public Objects(NXObject[] items, Long count, Long offset, Long limit, Long minLimit, Long maxLimit,
			Long limitIncrement) {

		super(count, offset, limit, minLimit, maxLimit, limitIncrement);

		this.items = items;
	}

	@JacksonXmlElementWrapper(localName = KeyWords.ITEMS)
	@JacksonXmlProperty(localName = KeyWords.OBJECT)
	@JsonProperty(KeyWords.ITEMS)
	public NXObject[] getItems() {
		return items;
	}
}