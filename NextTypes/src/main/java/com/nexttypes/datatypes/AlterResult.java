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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.system.KeyWords;

public class AlterResult {

	protected ZonedDateTime adate;
	protected String message;
	protected LinkedHashMap<String, AlterFieldResult> alteredFields = new LinkedHashMap<>();
	protected ArrayList<String> addedFields = new ArrayList<>();
	protected ArrayList<String> droppedFields = new ArrayList<>();
	protected ArrayList<String> renamedFields = new ArrayList<>();
	protected LinkedHashMap<String, AlterIndexResult> alteredIndexes = new LinkedHashMap<>();
	protected ArrayList<String> addedIndexes = new ArrayList<>();
	protected ArrayList<String> droppedIndexes = new ArrayList<>();
	protected ArrayList<String> renamedIndexes = new ArrayList<>();

	public void setADate(ZonedDateTime adate) {
		this.adate = adate;
	}

	@JsonProperty(KeyWords.ADATE)
	public ZonedDateTime getADate() {
		return adate;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@JsonProperty(KeyWords.ALTERED)
	public boolean isAltered() {
		return alteredFields.size() > 0 || addedFields.size() > 0 || droppedFields.size() > 0
				|| renamedFields.size() > 0 || alteredIndexes.size() > 0 || addedIndexes.size() > 0
				|| droppedIndexes.size() > 0 || renamedIndexes.size() > 0;
	}

	@JsonProperty(KeyWords.MESSAGE)
	public String getMessage() {
		return message;
	}

	public void addAlteredField(String field, AlterFieldResult result) {
		alteredFields.put(field, result);
	}

	public void addAddedField(String field) {
		addedFields.add(field);
	}

	public void addDroppedField(String field) {
		droppedFields.add(field);
	}

	public void addRenamedField(String field) {
		renamedFields.add(field);
	}

	public void addAlteredIndex(String index, AlterIndexResult result) {
		alteredIndexes.put(index, result);
	}

	public void addAddedIndex(String index) {
		addedIndexes.add(index);
	}

	public void addDroppedIndex(String index) {
		droppedIndexes.add(index);
	}

	public void addRenamedIndex(String index) {
		renamedIndexes.add(index);
	}

	@JsonProperty(KeyWords.ALTERED_FIELDS)
	public LinkedHashMap<String, AlterFieldResult> getAlteredFields() {
		return alteredFields;
	}

	@JsonProperty(KeyWords.ADDED_FIELDS)
	public String[] getAddedFields() {
		return addedFields.toArray(new String[] {});
	}

	@JsonProperty(KeyWords.DROPPED_FIELDS)
	public String[] getDroppedFields() {
		return droppedFields.toArray(new String[] {});
	}

	@JsonProperty(KeyWords.RENAMED_FIELDS)
	public String[] getRenamedFields() {
		return renamedFields.toArray(new String[] {});
	}

	@JsonProperty(KeyWords.ALTERED_INDEXES)
	public LinkedHashMap<String, AlterIndexResult> getAlteredIndexes() {
		return alteredIndexes;
	}

	@JsonProperty(KeyWords.ADDED_INDEXES)
	public String[] getAddedIndexes() {
		return addedIndexes.toArray(new String[] {});
	}

	@JsonProperty(KeyWords.DROPPED_INDEXES)
	public String[] getDroppedIndexes() {
		return droppedIndexes.toArray(new String[] {});
	}

	@JsonProperty(KeyWords.RENAMED_INDEXES)
	public String[] getRenamedIndexes() {
		return renamedIndexes.toArray(new String[] {});
	}
}