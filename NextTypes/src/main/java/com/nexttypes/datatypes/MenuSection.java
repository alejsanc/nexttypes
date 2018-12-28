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
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.system.KeyWords;

public class MenuSection {

	protected String title;
	protected ArrayList<Anchor> anchors = new ArrayList<>();

	@JsonCreator
	public MenuSection(@JsonProperty(KeyWords.TITLE) String title, @JsonProperty(KeyWords.ANCHORS) Anchor[] anchors) {
		this.title = title;
		this.anchors.addAll(Arrays.asList(anchors));
	}

	public String getTitle() {
		return title;
	}

	public Anchor[] getAnchors() {
		return anchors.toArray(new Anchor[] {});
	}

	public void addAnchor(Anchor anchor) {
		this.anchors.add(anchor);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setAnchors(Anchor[] anchors) {
		this.anchors.clear();
		this.anchors.addAll(Arrays.asList(anchors));
	}
}