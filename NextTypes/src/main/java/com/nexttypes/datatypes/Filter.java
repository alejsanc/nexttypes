/*
 * Copyright 2015-2026 Alejandro Sánchez <alex@nexttypes.com>
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

import com.nexttypes.enums.Comparison;

public class Filter {
	protected String field;
	protected Comparison comparison;
	protected Object value;
	protected boolean include;

	public Filter(String field, Comparison comparison, Object value, boolean include) {
		this.field = field;
		this.comparison = comparison;
		this.value = value;
		this.include = include;
	}

	public String getField() {
		return field;
	}

	public Comparison getComparison() {
		return comparison;
	}

	public Object getValue() {
		return value;
	}
	
	public boolean include() {
		return include;
	}
}