/*
 * Copyright 2015-2021 Alejandro Sánchez <alex@nexttypes.com>
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Anchor {

	protected String text;
	protected String href;

	@JsonCreator
	public Anchor(@JsonProperty(HTML.TEXT) String text, @JsonProperty(HTML.HREF) String href) {
		this.text = text;
		this.href = href;
	}

	public String getText() {
		return text;
	}

	public String getHref() {
		return href;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setHref(String href) {
		this.href = href;
	}
}
