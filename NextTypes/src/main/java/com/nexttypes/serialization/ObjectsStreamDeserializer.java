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

package com.nexttypes.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nexttypes.datatypes.Audio;
import com.nexttypes.datatypes.Document;
import com.nexttypes.datatypes.File;
import com.nexttypes.datatypes.Image;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.ObjectReference;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.Video;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.exceptions.TypeNotFoundException;
import com.nexttypes.interfaces.Node;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.system.Constants;

public class ObjectsStreamDeserializer extends StreamDeserializer implements ObjectsStream {
	protected Long count;
	protected NXObject item;
	protected LinkedHashMap<String, TypeField> typeFields;
	protected TypeSettings typeSettings;
	protected Node nextNode;
	protected String lang;
	protected boolean sharedParser;

	public ObjectsStreamDeserializer(InputStream input, String lang, boolean sharedParser, Node nextNode,
			TypeSettings typeSettings) {
		super(input);
		setParameters(lang, sharedParser, nextNode, typeSettings);
	}

	public ObjectsStreamDeserializer(JsonParser parser, String lang, boolean sharedParser, Node nextNode,
			TypeSettings typeSettings, Strings strings) {
		super(parser);
		setParameters(lang, sharedParser, nextNode, typeSettings);
	}

	protected void setParameters(String lang, boolean sharedParser, Node nextNode, TypeSettings typeSettings) {
		this.lang = lang;
		this.sharedParser = sharedParser;
		this.nextNode = nextNode;
		this.typeSettings = typeSettings;
	}

	@Override
	public void close() {
		try {
			if (!sharedParser) {
				parser.close();
			}
		} catch (IOException e) {
			throw new NXException(e);
		}
	}

	@Override
	public void exec() {
		try {
			parser.nextToken();
			parser.nextToken();
			parser.nextToken();

			count = parser.getLongValue();

			parser.nextToken();
			parser.nextToken();
		} catch (IOException e) {
			throw new NXException(e);
		}
	}

	@Override
	public boolean next() {
		try {
			parser.nextToken();

			if (parser.getCurrentToken().equals(JsonToken.START_OBJECT)) {

				String type = null, id = null, name = null;
				ZonedDateTime cdate = null, udate = null;
				Boolean backup = null;
				LinkedHashMap<String, Object> fields = null;

				while (parser.nextToken() != JsonToken.END_OBJECT) {
					parser.nextToken();

					switch (parser.getCurrentName()) {
					case Constants.TYPE:
						type = parser.getText();
						if (typeFields == null) {
							if (nextNode.existsType(type)) {
								typeFields = nextNode.getTypeFields(type);
							} else {
								throw new TypeNotFoundException(type);
							}
						}
						break;
					case Constants.ID:
						id = parser.getText();
						break;
					case Constants.NAME:
						name = parser.getText();
						break;
					case Constants.CDATE:
						cdate = Tuple.parseUTCDatetime(parser.getText());
						break;
					case Constants.UDATE:
						udate = Tuple.parseUTCDatetime(parser.getText());
						break;
					case Constants.BACKUP:
						backup = Tuple.parseBoolean(parser.getText());
						break;
					case Constants.FIELDS:
						fields = parseFields(type);
						break;

					}
				}

				item = new NXObject(type, id, name, cdate, udate, backup, fields);
				return true;

			} else {
				parser.nextToken();
				return false;
			}
		} catch (IOException e) {
			throw new NXException(e);
		}
	}

	protected LinkedHashMap<String, Object> parseFields(String type) {
		try {
			LinkedHashMap<String, Object> fields = new LinkedHashMap<>();

			while (parser.nextToken() != JsonToken.END_OBJECT) {
				parser.nextToken();

				String fieldName = parser.getCurrentName();
				TypeField field = typeFields.get(fieldName);
				String fieldType = field.getType();
				Object value = null;

				if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {

					switch (fieldType) {
					case PT.STRING:
					case PT.TEXT:
					case PT.TEL:
					case PT.PASSWORD:
						value = parser.getText();
						break;
					case PT.TIMEZONE:
						value = Tuple.parseTimezone(parser.getText());
						break;
					case PT.HTML:
						value = Tuple.parseHTML(parser.getText(), lang,
								typeSettings.getFieldString(type, fieldName, Constants.HTML_ALLOWED_TAGS));
						break;
					case PT.XML:
						value = Tuple.parseXML(parser.getText(), lang,
								typeSettings.getFieldString(type, fieldName, Constants.XML_ALLOWED_TAGS));
						break;
					case PT.JSON:
						value = Tuple.parseJSON(parser.getText());
						break;
					case PT.INT16:
						value = Tuple.parseInt16(parser.getText());
						break;
					case PT.INT32:
						value = Tuple.parseInt32(parser.getText());
						break;
					case PT.INT64:
						value = Tuple.parseInt64(parser.getText());
						break;
					case PT.FLOAT32:
						value = Tuple.parseFloat32(parser.getText());
						break;
					case PT.FLOAT64:
						value = Tuple.parseFloat64(parser.getText());
						break;
					case PT.NUMERIC:
						value = Tuple.parseNumeric(parser.getText());
						break;
					case PT.BOOLEAN:
						value = Tuple.parseBoolean(parser.getText());
						break;
					case PT.DATE:
						value = Tuple.parseDate(parser.getText());
						break;
					case PT.TIME:
						value = Tuple.parseTime(parser.getText());
						break;
					case PT.DATETIME:
						value = Tuple.parseDatetime(parser.getText());
						break;
					case PT.COLOR:
						value = Tuple.parseColor(parser.getText());
						break;
					case PT.URI:
						value = Tuple.parseURI(parser.getText());
						break;
					case PT.EMAIL:
						value = Tuple.parseEmail(parser.getText());
						break;
					case PT.BINARY:
						value = parser.getBinaryValue();
						break;
					case PT.FILE:
						value = parser.readValueAs(new TypeReference<File>() {});
						break;
					case PT.IMAGE:
						value = parser.readValueAs(new TypeReference<Image>() {});
						break;
					case PT.DOCUMENT:
						value = parser.readValueAs(new TypeReference<Document>() {});
						break;
					case PT.AUDIO:
						value = parser.readValueAs(new TypeReference<Audio>() {});
						break;
					case PT.VIDEO:
						value = parser.readValueAs(new TypeReference<Video>() {});
						break;
					default:
						value = parseObjectReference();
					}
				}

				fields.put(fieldName, value);
			}

			return fields;
		} catch (IOException e) {
			throw new NXException(e);
		}
	}

	protected ObjectReference parseObjectReference() throws JsonParseException, IOException {
		String referenceId = null;
		String referenceName = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			parser.nextToken();

			switch (parser.getCurrentName()) {
			case Constants.ID:
				referenceId = parser.getText();
				break;
			case Constants.NAME:
				referenceName = parser.getText();
				break;
			}
		}

		return new ObjectReference(referenceId, referenceName);
	}

	@Override
	public NXObject getItem() {
		return item;
	}

	@Override
	public Long getCount() {
		return count;
	}
}