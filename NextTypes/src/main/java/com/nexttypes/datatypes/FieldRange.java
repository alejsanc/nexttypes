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

package com.nexttypes.datatypes;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.nexttypes.system.KeyWords;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({ KeyWords.MIN, KeyWords.MAX })
public class FieldRange {
	protected Object min;
	protected Object max;
	
	public FieldRange(@JsonProperty(KeyWords.MIN) String min,
			@JsonProperty(KeyWords.MAX) String max) {
		this.min = min;
		this.max = max;
	}
	
	public FieldRange(String type, Integer precision, Integer scale) {
		parse(type, precision, scale);
	}
	
	protected void parse(String type, Integer precision, Integer scale) {
					
		switch(type) {
			
		case PT.DATE:
			min = Tuple.parseDate(min);
			max = Tuple.parseDate(max);
			break;
			
		case PT.TIME:
			min = Tuple.parseTime(min);
			max = Tuple.parseTime(max);
			break;
			
		case PT.DATETIME:
			min = Tuple.parseDateTime(min);
			max = Tuple.parseDateTime(max);
			break;
			
		case PT.INT16:
			min = min == null ? Short.MIN_VALUE : Tuple.parseInt16(min);
			max = max == null ? Short.MAX_VALUE : Tuple.parseInt16(max);
			break;
			
		case PT.INT32:
			min = min == null ? Integer.MIN_VALUE : Tuple.parseInt32(min);
			max = max == null ? Integer.MAX_VALUE : Tuple.parseInt32(max);
			break;
			
		case PT.INT64:
			min = min == null ? Long.MIN_VALUE : Tuple.parseInt64(min);
			max = max == null ? Long.MAX_VALUE : Tuple.parseInt64(max);
			break;
			
		case PT.FLOAT32:
			min = min == null ? Float.MIN_VALUE : Tuple.parseFloat32(min);
			max = max == null ? Float.MAX_VALUE : Tuple.parseFloat32(max);
			break;
			
		case PT.FLOAT64:
			min = min == null ? Double.MIN_VALUE : Tuple.parseFloat64(min);
			max = max == null ? Double.MAX_VALUE : Tuple.parseFloat64(max);
			break;
			
		case PT.NUMERIC:
			BigDecimal numericMax = PT.numericMaxValue(precision, scale);
			BigDecimal numericMin = numericMax.negate();
							
			min = min == null ? numericMin : Tuple.parseNumeric(min, numericMin, numericMax);
			max = max == null ? numericMax : Tuple.parseNumeric(max, numericMin, numericMax);
					
			break;
		}
	}
	
	@JsonProperty(KeyWords.MIN)
	public Object getMin() {
		return min;
	}
	
	@JsonProperty(KeyWords.MAX)
	public Object getMax() {
		return max;
	}
	
	public boolean isInRange(Object value) {
		return (min == null || PT.compare(value, min) >= 0)
				&& (max == null || PT.compare(value, max) <= 0);
	}
}
