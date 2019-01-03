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

import org.apache.commons.lang3.ArrayUtils;

public class PT {
	public static final String INT16 = "int16";
	public static final String INT32 = "int32";
	public static final String INT64 = "int64";
	public static final String FLOAT32 = "float32";
	public static final String FLOAT64 = "float64";
	public static final String NUMERIC = "numeric";
	public static final String BOOLEAN = "boolean";
	public static final String STRING = "string";
	public static final String TEXT = "text";
	public static final String HTML = "html";
	public static final String JSON = "json";
	public static final String XML = "xml";
	public static final String URL = "url";
	public static final String EMAIL = "email";
	public static final String TEL = "tel";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String DATETIME = "datetime";
	public static final String TIMEZONE = "timezone";
	public static final String COLOR = "color";
	public static final String BINARY = "binary";
	public static final String FILE = "file";
	public static final String IMAGE = "image";
	public static final String DOCUMENT = "document";
	public static final String AUDIO = "audio";
	public static final String VIDEO = "video";
	public static final String PASSWORD = "password";

	public static final String[] PRIMITIVE_TYPES = new String[] { INT16, INT32, INT64, FLOAT32, FLOAT64,
			NUMERIC, BOOLEAN, STRING, TEXT, HTML, JSON, XML, URL, EMAIL, TEL, DATE, TIME, DATETIME,
			TIMEZONE, COLOR, BINARY, FILE, IMAGE, AUDIO, VIDEO, DOCUMENT, PASSWORD };

	public static final String[] STRING_TYPES = new String[] { STRING, URL, EMAIL, TEL };
	
	public static final String[] TEXT_TYPES = new String[] { TEXT, HTML, JSON, XML };

	public static final String[] NUMERIC_TYPES = new String[] { INT16, INT32, INT64, FLOAT32, FLOAT64,
			NUMERIC };

	public static final String[] BINARY_TYPES = new String[] { BINARY, FILE, IMAGE, DOCUMENT, AUDIO,
			VIDEO };

	public static final String[] FILE_TYPES = new String[] { FILE, IMAGE, DOCUMENT, AUDIO, VIDEO };
	
	public static final String[] TIME_TYPES = new String[] { DATE, TIME, DATETIME };
	
	public static final String[] FILTER_TYPES = new String[] { INT16, INT32, INT64, FLOAT32, FLOAT64,
			NUMERIC, BOOLEAN, STRING, TEXT, HTML, JSON, XML, URL, EMAIL, TEL, DATE, TIME, DATETIME,
			TIMEZONE, COLOR };
	
	public static boolean isPrimitiveType(String type) {
		return ArrayUtils.contains(PRIMITIVE_TYPES, type);
	}
	
	public static boolean isStringType(String type) {
		return ArrayUtils.contains(STRING_TYPES, type);
	}
	
	public static boolean isTextType(String type) {
		return ArrayUtils.contains(TEXT_TYPES, type);
	}
	
	public static boolean isNumericType(String type) {
		return ArrayUtils.contains(NUMERIC_TYPES, type);
	}
	
	public static boolean isBinaryType(String type) {
		return ArrayUtils.contains(BINARY_TYPES, type);
	}
	
	public static boolean isFileType(String type) {
		return ArrayUtils.contains(FILE_TYPES, type);
	}
	
	public static boolean isTimeType(String type) {
		return ArrayUtils.contains(TIME_TYPES, type);
	}
	
	public static boolean isFilterType(String type) {
		return ArrayUtils.contains(FILTER_TYPES, type);
	}
	
	public static BigDecimal numericMaxValue(TypeField typeField) {
		return numericMaxValue(typeField.getPrecision(), typeField.getScale());
	}
	
	public static BigDecimal numericMaxValue(Integer precision, Integer scale) {
						
		StringBuilder max = new StringBuilder();
		int left = precision - scale;
							
		if (left > 0) {
			for (int x = 0; x < left; x++) {
				max.append("9");
			}
		} else {
			max.append("0");
		}
		
		if (scale > 0) {
			max.append(".");
							
			for (int x = 0; x < scale; x++) {
				max.append("9");
			}
		}
		
		return new BigDecimal(max.toString());
	}
	
	public static Integer compare(Object value1, Object value2) {
		Integer result = null;
		
		if (value1 instanceof Comparable) {
			result = ((Comparable) value1).compareTo(value2);
		} else if (value1 instanceof Short) {
			result = ((Short) value1).compareTo((Short) value2);
		} else if (value1 instanceof Integer) {
			result = ((Integer) value1).compareTo((Integer) value2);
		} else if (value1 instanceof Long) {
			result = ((Long) value1).compareTo((Long) value2);
		} else if (value1 instanceof Float) {
			result = ((Float) value1).compareTo((Float) value2);
		} else if (value1 instanceof Double) {
			result = ((Double) value1).compareTo((Double) value2);
		}
		
		return result;
	}
}