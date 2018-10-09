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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import javax.mail.internet.InternetAddress;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.Utils;

public class JSON extends PGobject {
	private static final long serialVersionUID = 1L;

	protected static final String JSONB = "jsonb";

	protected JsonNode node;
	protected ObjectMapper mapper;
	protected ObjectWriter writter;

	public JSON() {
		type = JSONB;

		mapper = new ObjectMapper();
		writter = mapper.writer().withDefaultPrettyPrinter();
	}

	public JSON(byte[] value) {
		this(Utils.toString(value));
	}

	public JSON(String value) {
		this();
		setValue(value);
	}

	public JSONObject getDocumentObject() {
		return node != null ? new JSONObject((ObjectNode) node) : null;
	}

	public JSONArray getDocumentArray() {
		return node != null ? new JSONArray((ArrayNode) node) : null;
	}
	
	public JSONObject setDocumentObject(JSONObject object) {
		node = object.getNode();
		return object;
	}
	
	public JSONArray setDocumentArray(JSONArray array) {
		node = array.getNode();
		return array;
	}

	public JSONObject createDocumentObject() {
		JSONObject object = createObject();
		node = object.getNode();
		return object;
	}
	
	public JSONArray createDocumentArray() {
		JSONArray array = createArray();
		node = array.getNode();
		return array;
	}

	public JSONObject createObject() {
		return new JSONObject(mapper.createObjectNode());
	}

	public JSONArray createArray() {
		return new JSONArray(mapper.createArrayNode());
	}

	public class JSONNode {
		protected JsonNode node;

		protected JSONNode(JsonNode node) {
			this.node = node;
		}

		protected JsonNode getNode() {
			return node;
		}

		protected Object get(Object fieldIndex) {
			Object value = null;

			JsonNode fieldNode = null;

			if (fieldIndex instanceof Integer) {
				fieldNode = node.get((Integer) fieldIndex);
			} else {
				fieldNode = node.get((String) fieldIndex);
			}

			if (fieldNode != null) {
				JsonNodeType fieldNodeType = fieldNode.getNodeType();

				switch (fieldNodeType) {
				case NUMBER:
					value = fieldNode.numberValue();
					break;

				case STRING:
					value = fieldNode.textValue();
					break;

				case BOOLEAN:
					value = fieldNode.booleanValue();
					break;

				case BINARY:
					try {
						value = fieldNode.binaryValue();
					} catch (IOException e) {
						throw new NXException(e);
					}
					break;

				case OBJECT:
					value = new JSONObject((ObjectNode) fieldNode);
					break;

				case ARRAY:
					value = new JSONArray((ArrayNode) fieldNode);
					break;
				}
			}

			return value;
		}

		protected Short getInt16(Object fieldIndex) {
			return Tuple.parseInt16(get(fieldIndex));
		}

		protected Integer getInt32(Object fieldIndex) {
			return Tuple.parseInt32(get(fieldIndex));
		}

		protected Long getInt64(Object fieldIndex) {
			return Tuple.parseInt64(get(fieldIndex));
		}

		protected Float getFloat32(Object fieldIndex) {
			return Tuple.parseFloat32(get(fieldIndex));
		}

		protected Double getFloat64(Object fieldIndex) {
			return Tuple.parseFloat64(get(fieldIndex));
		}

		protected BigDecimal getNumeric(Object fieldIndex) {
			return Tuple.parseNumeric(get(fieldIndex));
		}

		protected Boolean getBoolean(Object fieldIndex) {
			return Tuple.parseBoolean(get(fieldIndex));
		}

		protected String getString(Object fieldIndex) {
			return Tuple.parseString(get(fieldIndex));
		}

		protected String getText(Object fieldIndex) {
			return Tuple.parseText(get(fieldIndex));
		}

		protected HTMLFragment getHTML(Object fieldIndex, String lang, String allowedTags) {
			return Tuple.parseHTML(get(fieldIndex), lang, allowedTags);
		}

		protected JSON getJSON(Object fieldIndex) {
			return Tuple.parseJSON(get(fieldIndex));
		}

		protected XML getXML(Object fieldIndex, String lang, String allowedTags) {
			return Tuple.parseXML(get(fieldIndex), lang, allowedTags);
		}

		protected URL getURL(Object fieldIndex) {
			return Tuple.parseURL(get(fieldIndex));
		}

		protected InternetAddress getEmail(Object fieldIndex) {
			return Tuple.parseEmail(get(fieldIndex));
		}

		protected String getTel(Object fieldIndex) {
			return Tuple.parseTel(get(fieldIndex));
		}

		protected LocalDate getDate(Object fieldIndex) {
			return Tuple.parseDate(get(fieldIndex));
		}

		protected LocalTime getTime(Object fieldIndex) {
			return Tuple.parseTime(get(fieldIndex));
		}

		protected LocalDateTime getDateTime(Object fieldIndex) {
			return Tuple.parseDateTime(get(fieldIndex));
		}

		protected ZoneId getTimeZone(Object fieldIndex) {
			return Tuple.parseTimeZone(get(fieldIndex));
		}

		protected Color getColor(Object fieldIndex) {
			return Tuple.parseColor(get(fieldIndex));
		}

		protected byte[] getBinary(Object fieldIndex) {
			return Tuple.parseBinary(get(fieldIndex));
		}

		protected File getFile(Object fieldIndex) {
			return Tuple.parseFile(get(fieldIndex));
		}

		protected Image getImage(Object fieldIndex) {
			return Tuple.parseImage(get(fieldIndex));
		}

		protected Document getDocument(Object fieldIndex) {
			return Tuple.parseDocument(get(fieldIndex));
		}

		protected Audio getAudio(Object fieldIndex) {
			return Tuple.parseAudio(get(fieldIndex));
		}

		protected Video getVideo(Object fieldIndex) {
			return Tuple.parseVideo(get(fieldIndex));
		}

		protected String getPassword(Object fieldIndex) {
			return Tuple.parsePassword(get(fieldIndex));
		}

		protected JSONObject getObject(Object fieldIndex) {
			return (JSONObject) get(fieldIndex);
		}

		protected JSONArray getArray(Object fieldIndex) {
			return (JSONArray) get(fieldIndex);
		}

		@Override
		public String toString() {
			return nodeToString(node);
		}
	}

	public class JSONObject extends JSONNode {
		protected ObjectNode object;

		protected JSONObject(ObjectNode node) {
			super(node);
			this.object = node;
		}

		public Object get(String field) {
			return super.get(field);
		}

		public JSONObject putObject(String field) {
			return new JSONObject(object.putObject(field));
		}

		public JSONArray putArray(String field) {
			return new JSONArray(object.putArray(field));
		}

		public JSONObject put(String field, Object value) {
			if (value == null) {
				object.putNull(field);
			} else if (value instanceof String) {
				object.put(field, (String) value);
			} else if (value instanceof Short) {
				object.put(field, (Short) value);
			} else if (value instanceof Integer) {
				object.put(field, (Integer) value);
			} else if (value instanceof Long) {
				object.put(field, (Long) value);
			} else if (value instanceof Float) {
				object.put(field, (Float) value);
			} else if (value instanceof Double) {
				object.put(field, (Double) value);
			} else if (value instanceof BigInteger) {
				object.put(field, (BigInteger) value);
			} else if (value instanceof BigDecimal) {
				object.put(field, (BigDecimal) value);
			} else if (value instanceof Boolean) {
				object.put(field, (Boolean) value);
			} else if (value instanceof JSONObject) {
				object.set(field, ((JSONObject) value).getNode());
			} else if (value instanceof JSONArray) {
				object.set(field, ((JSONArray) value).getNode()); 
			} else {
				object.put(field, value.toString());
			}

			return this;
		}

		public Short getInt16(String field) {
			return super.getInt16(field);
		}

		public Integer getInt32(String field) {
			return super.getInt32(field);
		}

		public Long getInt64(String field) {
			return super.getInt64(field);
		}

		public Float getFloat32(String field) {
			return super.getFloat32(field);
		}

		public Double getFloat64(String field) {
			return super.getFloat64(field);
		}

		public BigDecimal getNumeric(String field) {
			return super.getNumeric(field);
		}

		public Boolean getBoolean(String field) {
			return super.getBoolean(field);
		}

		public String getString(String field) {
			return super.getString(field);
		}

		public String getText(String field) {
			return super.getText(field);
		}

		public HTMLFragment getHTML(String field, String lang, String allowedTags) {
			return super.getHTML(field, lang, allowedTags);
		}

		public JSON getJSON(String field) {
			return super.getJSON(field);
		}

		public XML getXML(String field, String lang, String allowedTags) {
			return super.getXML(field, lang, allowedTags);
		}

		public URL getURL(String field) {
			return super.getURL(field);
		}

		public InternetAddress getEmail(String field) {
			return super.getEmail(field);
		}

		public String getTel(String field) {
			return super.getTel(field);
		}

		public LocalDate getDate(String field) {
			return super.getDate(field);
		}

		public LocalTime getTime(String field) {
			return super.getTime(field);
		}

		public LocalDateTime getDateTime(String field) {
			return super.getDateTime(field);
		}

		public ZoneId getTimeZone(String field) {
			return super.getTimeZone(field);
		}

		public Color getColor(String field) {
			return super.getColor(field);
		}

		public byte[] getBinary(String field) {
			return super.getBinary(field);
		}

		public File getFile(String field) {
			return super.getFile(field);
		}

		public Image getImage(String field) {
			return super.getImage(field);
		}

		public Document getDocument(String field) {
			return super.getDocument(field);
		}

		public Audio getAudio(String field) {
			return super.getAudio(field);
		}

		public Video getVideo(String field) {
			return super.getVideo(field);
		}

		public String getPassword(String field) {
			return super.getPassword(field);
		}

		public JSONObject getObject(String field) {
			return (JSONObject) super.get(field);
		}

		public JSONArray getArray(String field) {
			return (JSONArray) super.get(field);
		}
	}

	public class JSONArray extends JSONNode {
		protected ArrayNode array;

		protected JSONArray(ArrayNode node) {
			super(node);
			this.array = node;
		}

		public Object get(String field) {
			return super.get(field);
		}

		public JSONObject addObject() {
			return new JSONObject(array.addObject());
		}

		public JSONArray addArray() {
			return new JSONArray(array.addArray());
		}

		public JSONObject insertObject(int index) {
			return new JSONObject(array.insertObject(index));
		}

		public JSONArray insertArray(int index) {
			return new JSONArray(array.insertArray(index));
		}

		public JSONArray add(Object value) {
			if (value == null) {
				array.addNull();
			} else if (value instanceof String) {
				array.add((String) value);
			} else if (value instanceof Short) {
				array.add((Integer) value);
			} else if (value instanceof Long) {
				array.add((Long) value);
			} else if (value instanceof Float) {
				array.add((Float) value);
			} else if (value instanceof Double) {
				array.add((Double) value);
			} else if (value instanceof BigInteger) {
				array.add((BigInteger) value);
			} else if (value instanceof BigDecimal) {
				array.add((BigDecimal) value);
			} else if (value instanceof Boolean) {
				array.add((Boolean) value);
			} else if (value instanceof JSONObject) {
				array.add(((JSONObject) value).getNode());
			} else if (value instanceof JSONArray) {
				array.add(((JSONArray) value).getNode());
			} else {
				array.add(value.toString());
			}

			return this;
		}

		public JSONArray insert(int index, Object value) {
			if (value == null) {
				array.insertNull(index);
			} else if (value instanceof String) {
				array.insert(index, (String) value);
			} else if (value instanceof Short) {
				array.insert(index, (Integer) value);
			} else if (value instanceof Long) {
				array.insert(index, (Long) value);
			} else if (value instanceof Float) {
				array.insert(index, (Float) value);
			} else if (value instanceof Double) {
				array.insert(index, (Double) value);
			} else if (value instanceof BigInteger) {
				array.insert(index, (BigInteger) value);
			} else if (value instanceof BigDecimal) {
				array.insert(index, (BigDecimal) value);
			} else if (value instanceof Boolean) {
				array.insert(index, (Boolean) value);
			} else if (value instanceof JSONObject) {
				array.insert(index, ((JSONObject) value).getNode());
			} else if (value instanceof JSONArray) {
				array.insert(index, ((JSONArray) value).getNode());
			} else {
				array.insert(index, value.toString());
			}

			return this;
		}

		public Short getInt16(int index) {
			return super.getInt16(index);
		}

		public Integer getInt32(int index) {
			return super.getInt32(index);
		}

		public Long getInt64(int index) {
			return super.getInt64(index);
		}

		public Float getFloat32(int index) {
			return super.getFloat32(index);
		}

		public Double getFloat64(int index) {
			return super.getFloat64(index);
		}

		public BigDecimal getNumeric(int index) {
			return super.getNumeric(index);
		}

		public Boolean getBoolean(int index) {
			return super.getBoolean(index);
		}

		public String getString(int index) {
			return super.getString(index);
		}

		public String getText(int index) {
			return super.getText(index);
		}

		public HTMLFragment getHTML(int index, String lang, String allowedTags) {
			return super.getHTML(index, lang, allowedTags);
		}

		public JSON getJSON(int index) {
			return super.getJSON(index);
		}

		public XML getXML(int index, String lang, String allowedTags) {
			return super.getXML(index, lang, allowedTags);
		}

		public URL getURL(int index) {
			return super.getURL(index);
		}

		public InternetAddress getEmail(int index) {
			return super.getEmail(index);
		}

		public String getTel(int index) {
			return super.getTel(index);
		}

		public LocalDate getDate(int index) {
			return super.getDate(index);
		}

		public LocalTime getTime(int index) {
			return super.getTime(index);
		}

		public LocalDateTime getDateTime(int index) {
			return super.getDateTime(index);
		}

		public ZoneId getTimeZone(int index) {
			return super.getTimeZone(index);
		}

		public Color getColor(int index) {
			return super.getColor(index);
		}

		public byte[] getBinary(int index) {
			return super.getBinary(index);
		}

		public File getFile(int index) {
			return super.getFile(index);
		}

		public Image getImage(int index) {
			return super.getImage(index);
		}

		public Document getDocument(int index) {
			return super.getDocument(index);
		}

		public Audio getAudio(int index) {
			return super.getAudio(index);
		}

		public Video getVideo(int index) {
			return super.getVideo(index);
		}

		public String getPassword(int index) {
			return super.getPassword(index);
		}

		public JSONObject getObject(int index) {
			return (JSONObject) super.get(index);
		}

		public JSONArray getArray(int index) {
			return (JSONArray) super.get(index);
		}

	}

	@Override
	public void setValue(String value) {
		try {
			node = mapper.readTree(value);
		} catch (IOException e) {
			throw new NXException(e);
		}
	}

	@Override
	public String getValue() {
		return toString();
	}

	protected String nodeToString(JsonNode node) {
		try {
			return writter.writeValueAsString(node);
		} catch (JsonProcessingException e) {
			throw new NXException(e);
		}
	}

	@Override
	public String toString() {
		return nodeToString(node);
	}
}