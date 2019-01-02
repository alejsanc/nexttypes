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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.postgresql.jdbc.PgSQLXML;
import org.postgresql.util.PGobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.datatypes.JSON.JSONObject;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.Constants;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Utils;

public class Tuple {

	protected LinkedHashMap<String, Object> fields = new LinkedHashMap<>();

	public Object get(String field) {
		return fields.get(field);
	}

	public void put(String field, Object value) {
		fields.put(field, value);
	}

	public void putIfNull(String field, Object value) {
		Object tupleValue = get(field);
		if (tupleValue == null && value != null) {
			put(field, value);
		}
	}

	public boolean containsKey(String key) {
		return fields.containsKey(key);
	}

	@JsonProperty(KeyWords.FIELDS)
	public LinkedHashMap<String, Object> getFields() {
		return fields;
	}

	public void putFields(LinkedHashMap<String, Object> fields) {
		this.fields = fields;
	}

	protected static String bytesToText(Object value) {
		try {
			value = new String((byte[]) value, Constants.UTF_8_CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new NXException(e);
		}
		return (String) value;
	}

	protected static String bytesToString(Object value) {
		return bytesToText(value).replace("\n", "");
	}

	public String getString(String field) {
		return parseString(get(field));
	}

	public static String parseString(Object value) {
		if (value instanceof Timestamp) {
			value = parseDateTime(value).toString();
		} else if (value instanceof Date) {
			value = parseDate(value).toString();
		} else if (value instanceof Time) {
			value = parseTime(value).toString();
		} else if (value instanceof byte[]) {
			value = bytesToString(value);
		} 	

		return (String) value;
	}

	public Object getArray(String field) {
		return parseArray(get(field));
	}

	public static Object parseArray(Object value) {
		if (value instanceof Array) {
			try {
				value = ((Array) value).getArray();
			} catch (SQLException e) {
				throw new NXException(e);
			}
		}

		return value;
	}

	public String[] getStringArray(String field) {
		return (String[]) getArray(field);
	}

	public String getHTMLText(String field) {
		String value = getString(field);
		return value != null ? XML.getText(value) : null;
	}

	public HTMLFragment getHTML(String field) {
		return (HTMLFragment) get(field);
	}

	public HTMLFragment getHTML(String field, String lang, String allowedTags) {
		return parseHTML(get(field), lang, allowedTags);
	}

	public static HTMLFragment parseHTML(Object value, String lang, String allowedTags) {
		if (value instanceof String) {
			value = new HTMLFragment((String) value, lang, allowedTags);
		} else if (value instanceof byte[]) {
			value = new HTMLFragment(bytesToText(value), lang, allowedTags);
		}
		return (HTMLFragment) value;
	}

	public XML getXML(String field, String lang, String allowedTags) {
		return parseXML(get(field), lang, allowedTags);
	}

	public static XML parseXML(Object value, String lang, String allowedTags) {
		if (value instanceof String) {
			value = new XML((String) value, lang, allowedTags);
		} else if (value instanceof byte[]) {
			value = new XML(bytesToText(value), lang, allowedTags);
		} else if (value instanceof PgSQLXML) {
			try {
				value = new XML(((PgSQLXML) value).getBinaryStream(), lang, allowedTags);
			} catch (SQLException e) {
				throw new NXException(e);
			}
		}
		return (XML) value;
	}

	public JSON getJSON(String field) {
		return parseJSON(get(field));
	}

	public static JSON parseJSON(Object value) {
		if (value instanceof String) {
			value = new JSON((String) value);
		} else if (value instanceof PGobject) {
			value = new JSON(value.toString());
		}
		return (JSON) value;
	}

	public Short getInt16(String field) {
		return parseInt16(get(field));
	}

	public static Short parseInt16(Object value) {
		if (value instanceof BigDecimal) {
			value = ((BigDecimal) value).shortValueExact();
		} else if (value instanceof String) {
			value = Short.parseShort((String) value);
		} else if (value instanceof byte[]) {
			value = Short.parseShort(bytesToString(value));
		}
		return (Short) value;
	}

	public Integer getInt32(String field) {
		return parseInt32(get(field));
	}

	public static Integer parseInt32(Object value) {
		if (value instanceof BigDecimal) {
			value = ((BigDecimal) value).intValueExact();
		} else if (value instanceof Short) {
			value = ((Short) value).intValue();
		} else if (value instanceof String) {
			value = Integer.parseInt((String) value);
		} else if (value instanceof byte[]) {
			value = Integer.parseInt(bytesToString(value));
		}
		return (Integer) value;
	}

	public Long getInt64(String field) {
		return parseInt64(get(field));
	}

	public static Long parseInt64(Object value) {
		if (value instanceof BigDecimal) {
			value = ((BigDecimal) value).longValueExact();
		} else if (value instanceof Integer) {
			value = ((Integer) value).longValue();
		} else if (value instanceof Short) {
			value = ((Short) value).longValue();
		} else if (value instanceof String) {
			value = Long.parseLong((String) value);
		} else if (value instanceof byte[]) {
			value = Long.parseLong(bytesToString(value));
		}
		return (Long) value;
	}

	public Float getFloat32(String field) {
		return parseFloat32(get(field));
	}

	public static Float parseFloat32(Object value) {
		if (value instanceof String) {
			value = Float.parseFloat((String) value);
		} else if (value instanceof byte[]) {
			value = Float.parseFloat(bytesToString(value));
		}
		return (Float) value;
	}

	public Double getFloat64(String field) {
		return parseFloat64(get(field));
	}

	public static Double parseFloat64(Object value) {
		if (value instanceof Float) {
			value = ((Float) value).doubleValue();
		} else if (value instanceof String) {
			value = Double.parseDouble((String) value);
		} else if (value instanceof byte[]) {
			value = Double.parseDouble(bytesToString(value));
		}
		return (Double) value;
	}

	public BigDecimal getNumeric(String field) {
		return parseNumeric(get(field));
	}
	
	public BigDecimal getNumeric(String field, TypeField typeField) {
		return parseNumeric(get(field), typeField);
	}
	
	public BigDecimal getNumeric(String field, BigDecimal min, BigDecimal max) {
		return parseNumeric(get(field), min, max);
	}

	public static BigDecimal parseNumeric(Object value) {
		if (value instanceof Short) {
			value = new BigDecimal((Short) value);
		} else if (value instanceof Integer) {
			value = new BigDecimal((Integer) value);
		} else if (value instanceof Long) {
			value = new BigDecimal((Long) value);
		} else if (value instanceof Float) {
			value = new BigDecimal((Float) value);
		} else if (value instanceof Double) {
			value = new BigDecimal((Double) value);
		} else if (value instanceof String) {
			value = new BigDecimal((String) value);
		} else if (value instanceof byte[]) {
			value = new BigDecimal(bytesToString(value));
		}
		return (BigDecimal) value;
	}
	
	public static BigDecimal parseNumeric(Object value, TypeField typeField) {
		BigDecimal max = PT.numericMaxValue(typeField);
		return parseNumeric(value, max.negate(), max);
	}
	
	public static BigDecimal parseNumeric(Object value, BigDecimal min, BigDecimal max) {
		BigDecimal numeric = parseNumeric(value);
		
		if (numeric != null && (numeric.compareTo(min) == -1 || numeric.compareTo(max) == 1)) {
			throw new InvalidValueException(KeyWords.INVALID_NUMERIC, numeric);
		}
		
		return numeric;
	}
	
	public Boolean getBoolean(String field) {
		return parseBoolean(get(field));
	}

	public static Boolean parseBoolean(Object value) {
		if (value instanceof String) {
			value = Boolean.parseBoolean((String) value);
		} else if (value instanceof Long) {
			if ((Long) value == 0) {
				value = false;
			} else if ((Long) value == 1) {
				value = true;
			} else {
				throw new InvalidValueException(KeyWords.INVALID_BOOLEAN, value);
			}
		} else if (value instanceof byte[]) {
			value = Boolean.parseBoolean(bytesToString(value));
		}

		return (Boolean) value;
	}

	public URL getURL(String field) {
		return parseURL(get(field));
	}

	public static URL parseURL(Object value) {

		if (value instanceof String) {
			value = new URL((String) value);
		} else if (value instanceof byte[]) {
			value = new URL(bytesToString(value));
		}

		return (URL) value;
	}

	public InternetAddress getEmail(String field) {
		return parseEmail(get(field));
	}

	public static InternetAddress parseEmail(Object value) {
		try {
			if (value instanceof String) {
				value = new InternetAddress((String) value);
				((InternetAddress) value).validate();
			} else if (value instanceof byte[]) {
				value = new InternetAddress(bytesToString(value));
				((InternetAddress) value).validate();
			}
		} catch (AddressException e) {
			throw new InvalidValueException(KeyWords.INVALID_EMAIL, value);
		}
		return (InternetAddress) value;
	}

	public LocalDate getDate(String field) {
		return parseDate(get(field));
	}

	public static LocalDate parseDate(Object value) {
		if (value instanceof Date) {
			value = ((Date) value).toLocalDate();
		} else if (value instanceof Timestamp) {
			value = ((Timestamp) value).toLocalDateTime().toLocalDate();
		} else if (value instanceof String) {
			value = LocalDate.parse((String) value);
		} else if (value instanceof byte[]) {
			value = LocalDate.parse(bytesToString(value));
		}
		return (LocalDate) value;
	}

	public LocalTime getTime(String field) {
		return parseTime(get(field));

	}

	public static LocalTime parseTime(Object value) {
		if (value instanceof Time) {
			value = ((Time) value).toLocalTime();
		} else if (value instanceof Timestamp) {
			value = ((Timestamp) value).toLocalDateTime().toLocalTime();
		} else if (value instanceof String) {
			value = LocalTime.parse((String) value);
		} else if (value instanceof byte[]) {
			value = LocalTime.parse(bytesToString(value));
		}
		return (LocalTime) value;
	}

	public LocalDateTime getDateTime(String field) {
		return parseDateTime(get(field));
	}

	public static LocalDateTime parseDateTime(Object value) {
		if (value instanceof Timestamp) {
			value = ((Timestamp) value).toLocalDateTime();
		} else if (value instanceof String) {
			value = LocalDateTime.parse((String) value);
		} else if (value instanceof byte[]) {
			value = LocalDateTime.parse(bytesToString(value));
		}
		return (LocalDateTime) value;
	}

	public ZonedDateTime getUTCDateTime(String field) {
		return parseUTCDateTime(get(field));
	}

	public static ZonedDateTime parseUTCDateTime(Object value) {
		if (value instanceof Timestamp) {
			value = ZonedDateTime.of(((Timestamp) value).toLocalDateTime(), ZoneOffset.UTC);
		} else if (value instanceof String) {
			value = ZonedDateTime.parse((String) value);
			ZoneOffset offset = ((ZonedDateTime) value).getOffset();

			if (!offset.equals(ZoneOffset.UTC)) {
				throw new InvalidValueException(KeyWords.INVALID_TIMEZONE, offset.getId());
			}
		}
		return (ZonedDateTime) value;
	}

	public ZoneId getTimeZone(String field) {
		return parseTimeZone(get(field));
	}

	public static ZoneId parseTimeZone(Object value) {
		if (value instanceof String) {
			value = ZoneId.of((String) value);
		} else if (value instanceof byte[]) {
			value = ZoneId.of(bytesToString(value));
		}
		return (ZoneId) value;
	}

	public Color getColor(String field) {
		return parseColor(get(field));
	}

	public static Color parseColor(Object value) {
		if (value instanceof String) {
			value = new Color((String) value);
		} else if (value instanceof byte[]) {
			value = new Color(bytesToString(value));
		}
		return (Color) value;
	}

	public byte[] getBinary(String field) {
		return parseBinary(get(field));
	}

	public static byte[] parseBinary(Object value) {
		if (value instanceof File) {
			value = ((File) value).getContent();
		} 
		return (byte[]) value;
	}
	
	public File getFile(String field) {
		return parseFile(get(field));
	}
	
	public static File parseFile(Object value) {
		if (value instanceof byte[]) {
			value = new File((byte[]) value);
		} else if(value instanceof JSONObject) {
			value = new File((JSONObject) value);
		}
		return (File) value;
	}

	public Image getImage(String field) {
		return parseImage(get(field));
	}

	public static Image parseImage(Object value) {
		if (value instanceof byte[]) {
			value = new Image((byte[]) value);
		} else if (value instanceof File) {
			value = new Image((File) value);
		} else if (value instanceof JSONObject) {
			value = new Image((JSONObject) value);
		}
		return (Image) value;
	}

	public Audio getAudio(String field) {
		return parseAudio(get(field));
	}

	public static Audio parseAudio(Object value) {
		if (value instanceof byte[]) {
			value = new Audio((byte[]) value);
		} else if (value instanceof File) {
			value = new Audio((File) value);
		} else if (value instanceof JSONObject) {
			value = new Audio((JSONObject) value);
		}
		return (Audio) value;
	}

	public Video getVideo(String field) {
		return parseVideo(get(field));
	}

	public static Video parseVideo(Object value) {
		if (value instanceof byte[]) {
			value = new Video((byte[]) value);
		} else if (value instanceof File) {
			value = new Video((File) value);
		} else if (value instanceof JSONObject) {
			value = new Video((JSONObject) value);
		}
		return (Video) value;
	}

	public Document getDocument(String field) {
		return parseDocument(get(field));
	}

	public static Document parseDocument(Object value) {
		if (value instanceof byte[]) {
			value = new Document((byte[]) value);
		} else if (value instanceof File) {
			value = new Document((File) value);
		} else if (value instanceof JSONObject) {
			value = new Document((JSONObject) value);
		}
		return (Document) value;
	}

	public String getTel(String field) {
		return parseTel(get(field));
	}

	public static String parseTel(Object value) {
		if (value instanceof byte[]) {
			value = bytesToString(value);
		}
		return (String) value;
	}

	public String getText(String field) {
		return parseText(get(field));
	}

	static public String parseText(Object value) {
		if (value instanceof byte[]) {
			value = bytesToText(value);
		}
		return (String) value;
	}

	public String getETag(String field) {
		return parseETag(get(field));
	}

	static public String parseETag(Object value) {
		value = parseUTCDateTime(value);

		if (value instanceof ZonedDateTime) {
			value = Utils.etag((ZonedDateTime) value);
		}

		return (String) value;
	}

	public String getPassword(String field) {
		return parsePassword(get(field));
	}

	static public String parsePassword(Object value) {
		if (value instanceof byte[]) {
			value = bytesToString(value);
		}

		return (String) value;
	}

	public Tuple getTuple(String field) {
		return parseTuple(get(field));
	}

	public static Tuple parseTuple(Object value) {
		if (value instanceof String) {
			Tuple tuple = new Tuple();

			for (String entry : Utils.split((String) value)) {
				String[] keyValue = Utils.split(entry, ":");
				tuple.put(keyValue[0], keyValue[1]);
			}

			value = tuple;
		}

		return (Tuple) value;
	}
}
