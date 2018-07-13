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
	public static final String URI = "uri";
	public static final String EMAIL = "email";
	public static final String TEL = "tel";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String DATETIME = "datetime";
	public static final String TIMEZONE = "timezone";
	public static final String COLOR = "color";
	public static final String BINARY = "binary";
	public static final String IMAGE = "image";
	public static final String AUDIO = "audio";
	public static final String VIDEO = "video";
	public static final String DOCUMENT = "document";
	public static final String PASSWORD = "password";

	public static final String[] PRIMITIVE_TYPES = new String[] { INT16, INT32, INT64, FLOAT32, FLOAT64, NUMERIC,
			BOOLEAN, STRING, TEXT, HTML, JSON, XML, URI, EMAIL, TEL, DATE, TIME, DATETIME, TIMEZONE, COLOR, BINARY,
			IMAGE, AUDIO, VIDEO, DOCUMENT, PASSWORD };

	public static final String[] STRING_TYPES = new String[] { STRING, URI, EMAIL, TEL };
	
	public static final String[] TEXT_TYPES = new String[] { TEXT, HTML, JSON, XML };

	public static final String[] NUMERIC_TYPES = new String[] { INT16, INT32, INT64, FLOAT32, FLOAT64, NUMERIC };

	public static final String[] BINARY_TYPES = new String[] { BINARY, IMAGE, DOCUMENT, AUDIO, VIDEO };

	public static final String[] COMPLEX_TYPES = new String[] { IMAGE, DOCUMENT, AUDIO, VIDEO };
	
	public static final String[] FILTER_TYPES = new String[] { INT16, INT32, INT64, FLOAT32, FLOAT64,
			NUMERIC, BOOLEAN, STRING, TEXT, HTML, JSON, XML, URI, EMAIL, TEL, DATE, TIME, DATETIME, TIMEZONE, COLOR };
	
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
	
	public static boolean isComplexType(String type) {
		return ArrayUtils.contains(COMPLEX_TYPES, type);
	}
	
	public static boolean isFilterType(String type) {
		return ArrayUtils.contains(FILTER_TYPES, type);
	}
}
