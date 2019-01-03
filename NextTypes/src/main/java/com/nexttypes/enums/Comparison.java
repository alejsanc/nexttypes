/*
 * Copyright 2015-2019 Alejandro Sánchez <alex@nexttypes.com>
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

package com.nexttypes.enums;

public enum Comparison {
	EQUAL("equal"),
	NOT_EQUAL("not_equal"),
	GREATER("greater"),
	GREATER_OR_EQUAL("greater_or_equal"),
	LESS("less"),
	LESS_OR_EQUAL("less_or_equal"),
	LIKE("like");
	
	protected String comparison;
	
	private Comparison(String comparison) {
		this.comparison = comparison;
	}
	
	@Override
	public String toString() {
		return comparison;
	}
}