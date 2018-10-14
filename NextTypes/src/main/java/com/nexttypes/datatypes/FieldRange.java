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

import java.math.BigDecimal;

public class FieldRange {
	protected Object min;
	protected Object max;
	
	protected FieldRange(String min, String max, TypeField typeField) {
		String fieldType = typeField.getType();
				
		switch(fieldType) {
			
		case PT.DATE:
			this.min = Tuple.parseDate(min);
			this.max = Tuple.parseDate(max);
			break;
			
		case PT.TIME:
			this.min = Tuple.parseTime(min);
			this.max = Tuple.parseTime(max);
			break;
			
		case PT.DATETIME:
			this.min = Tuple.parseDateTime(min);
			this.max = Tuple.parseDateTime(max);
			break;
			
		case PT.INT16:
			this.min = min == null ? Short.MIN_VALUE : Tuple.parseInt16(min);
			this.max = max == null ? Short.MAX_VALUE : Tuple.parseInt16(max);
			break;
			
		case PT.INT32:
			this.min = min == null ? Integer.MIN_VALUE : Tuple.parseInt32(min);
			this.max = max == null ? Integer.MAX_VALUE : Tuple.parseInt32(max);
			break;
			
		case PT.INT64:
			this.min = min == null ? Long.MIN_VALUE : Tuple.parseInt64(min);
			this.max = max == null ? Long.MAX_VALUE : Tuple.parseInt64(max);
			break;
			
		case PT.FLOAT32:
			this.min = min == null ? Float.MIN_VALUE : Tuple.parseFloat32(min);
			this.max = max == null ? Float.MAX_VALUE : Tuple.parseFloat32(max);
			break;
			
		case PT.FLOAT64:
			this.min = min == null ? Double.MIN_VALUE : Tuple.parseFloat64(min);
			this.max = max == null ? Double.MAX_VALUE : Tuple.parseFloat64(max);
			break;
			
		case PT.NUMERIC:
			BigDecimal numericMax = PT.numericMaxValue(typeField);
			BigDecimal numericMin = numericMax.negate();
							
			this.min = min == null ? numericMin : Tuple.parseNumeric(min, numericMin, numericMax);
			this.max = max == null ? numericMax : Tuple.parseNumeric(max, numericMin, numericMax);
					
			break;
		}
	}
	
	public FieldRange(Object min, Object max) {
		this.min = min;
		this.max = max;
	}
	
	public Object getMin() {
		return min;
	}
	
	public Object getMax() {
		return max;
	}
	
	public boolean isInRange(Object value) {
		return (min == null || PT.compare(value, min) >= 0)
				&& (max == null || PT.compare(value, max) <= 0);
	}
}
