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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.system.KeyWords;

@JsonPropertyOrder({ KeyWords.ID, KeyWords.NAME })
public class ObjectReference {
	protected String id;
	protected String name;

	public ObjectReference(String id, String name) {
		this.id = id;
		this.name = name == null ? id : name;
	}

	@JsonProperty(KeyWords.ID)
	public String getId() {
		return id;
	}

	@JsonProperty(KeyWords.NAME)
	public String getName() {
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

}
