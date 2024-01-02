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

import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.system.KeyWords;

public class ImportObjectsResult {

	protected LinkedHashMap<String, Long> importedObjects = new LinkedHashMap<>();
	protected LinkedHashMap<String, Long> ignoredObjects = new LinkedHashMap<>();
	protected LinkedHashMap<String, Long> updatedObjects = new LinkedHashMap<>();

	public void addImportedObject(String type) {
		Long objects = importedObjects.get(type);
		if (objects == null) {
			objects = 0L;
		}
		importedObjects.put(type, ++objects);
	}

	public void addIgnoredObject(String type) {
		Long objects = ignoredObjects.get(type);
		if (objects == null) {
			objects = 0L;
		}
		ignoredObjects.put(type, ++objects);
	}

	public void addUpdatedObject(String type) {
		Long objects = updatedObjects.get(type);
		if (objects == null) {
			objects = 0L;
		}
		updatedObjects.put(type, ++objects);
	}

	@JsonProperty(KeyWords.IMPORTED_OBJECTS)
	public LinkedHashMap<String, Long> getImportedObjects() {
		return importedObjects;
	}

	@JsonProperty(KeyWords.IGNORED_OBJECTS)
	public LinkedHashMap<String, Long> getIgnoredObjects() {
		return ignoredObjects;
	}

	@JsonProperty(KeyWords.UPDATED_OBJECTS)
	public LinkedHashMap<String, Long> getUpdatedObjects() {
		return updatedObjects;
	}
}