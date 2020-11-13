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

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.system.KeyWords;

public class ImportTypesResult {

	protected ArrayList<String> importedTypes = new ArrayList<>();
	protected ArrayList<String> ignoredTypes = new ArrayList<>();
	protected LinkedHashMap<String, AlterResult> alteredTypes = new LinkedHashMap<>();
	
	public void addImportedType(String type) {
		importedTypes.add(type);
	}

	public void addIgnoredType(String type) {
		ignoredTypes.add(type);
	}

	public void addAlteredType(String type, AlterResult result) {
		alteredTypes.put(type, result);
	}
	
	@JsonProperty(KeyWords.IMPORTED_TYPES)
	public ArrayList<String> getImportedTypes() {
		return importedTypes;
	}

	@JsonProperty(KeyWords.IGNORED_TYPES)
	public ArrayList<String> getIgnoredTypes() {
		return ignoredTypes;
	}

	@JsonProperty(KeyWords.ALTERED_TYPES)
	public LinkedHashMap<String, AlterResult> getAlteredTypes() {
		return alteredTypes;
	}
}