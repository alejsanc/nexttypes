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

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.system.Constants;

public class ImportTypesResult {

	protected ArrayList<String> importedTypes = new ArrayList<>();
	protected ArrayList<String> ignoredTypes = new ArrayList<>();
	protected LinkedHashMap<String, AlterResult> alteredTypes = new LinkedHashMap<>();
	protected LinkedHashMap<String, ArrayList<String>> importedObjects = new LinkedHashMap<>();
	protected LinkedHashMap<String, ArrayList<String>> ignoredObjects = new LinkedHashMap<>();
	protected LinkedHashMap<String, ArrayList<String>> updatedObjects = new LinkedHashMap<>();

	public void addImportedType(String type) {
		importedTypes.add(type);
	}

	public void addIgnoredType(String type) {
		ignoredTypes.add(type);
	}

	public void addAlteredType(String type, AlterResult result) {
		alteredTypes.put(type, result);
	}

	public void addImportedObject(String type, String id) {
		ArrayList<String> objects = importedObjects.get(type);
		if (objects == null) {
			objects = new ArrayList<>();
			importedObjects.put(type, objects);
		}
		objects.add(id);
	}

	public void addIgnoredObject(String type, String id) {
		ArrayList<String> objects = ignoredObjects.get(type);
		if (objects == null) {
			objects = new ArrayList<>();
			ignoredObjects.put(type, objects);
		}
		objects.add(id);
	}

	public void addUpdatedObject(String type, String id) {
		ArrayList<String> objects = updatedObjects.get(type);
		if (objects == null) {
			objects = new ArrayList<>();
			updatedObjects.put(type, objects);
		}
		objects.add(id);
	}

	public void addResult(ImportObjectsResult result) {
		importedObjects.putAll(result.getImportedObjects());
		ignoredObjects.putAll(result.getIgnoredObjects());
		updatedObjects.putAll(result.getUpdatedObjects());
	}

	@JsonProperty(Constants.IMPORTED_TYPES)
	public ArrayList<String> getImportedTypes() {
		return importedTypes;
	}

	@JsonProperty(Constants.IGNORED_TYPES)
	public ArrayList<String> getIgnoredTypes() {
		return ignoredTypes;
	}

	@JsonProperty(Constants.ALTERED_TYPES)
	public LinkedHashMap<String, AlterResult> getAlteredTypes() {
		return alteredTypes;
	}

	@JsonProperty(Constants.IMPORTED_OBJECTS)
	public LinkedHashMap<String, ArrayList<String>> getImportedObjects() {
		return importedObjects;
	}

	@JsonProperty(Constants.IGNORED_OBJECTS)
	public LinkedHashMap<String, ArrayList<String>> getIgnoredObjects() {
		return ignoredObjects;
	}

	@JsonProperty(Constants.UPDATED_OBJECTS)
	public LinkedHashMap<String, ArrayList<String>> getUpdatedObjects() {
		return updatedObjects;
	}
}