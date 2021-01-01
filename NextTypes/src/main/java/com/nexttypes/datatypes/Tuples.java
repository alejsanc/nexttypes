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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.system.KeyWords;

public class Tuples extends Result {
	protected Tuple[] items;
	
	public Tuples() {
		this(null, 0L, null, null, null, null, null);
	}

	public Tuples(Tuple[] items, Long count, Long offset, Long limit, Long minLimit, Long maxLimit,
			Long limitIncrement) {

		super(count, offset, limit, minLimit, maxLimit, limitIncrement);

		this.items = items;
	}

	@JsonProperty(KeyWords.ITEMS)
	public Tuple[] getItems() {
		return items;
	}
}