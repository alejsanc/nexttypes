/*
 * Copyright 2015-2024 Alejandro Sánchez <alex@nexttypes.com>
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

package com.nexttypes.views;

import java.util.LinkedHashMap;
import org.apache.commons.lang3.ArrayUtils;

import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.ActionReference;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.Names;
import com.nexttypes.datatypes.Objects;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.Reference;
import com.nexttypes.datatypes.Serial;
import com.nexttypes.datatypes.Type;
import com.nexttypes.datatypes.TypeInfo;
import com.nexttypes.enums.Comparison;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.ObjectNotFoundException;
import com.nexttypes.protocol.http.HTTPHeader;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.protocol.http.HTTPStatus;
import com.nexttypes.security.Security;
import com.nexttypes.settings.Settings;
import com.nexttypes.system.KeyWords;

public class SerialView extends View {

	public SerialView(HTTPRequest request) {
		super(request, Settings.SERIAL_SETTINGS);
	}

	@Override
	public Content getTypesName(String lang, String view) {

		String[] types = nextNode.getTypesName();
		return content(types, view, KeyWords.TYPES, KeyWords.TYPE);
	}

	@Override
	public Content getTypesInfo(String lang, String view) {

		TypeInfo[] types = nextNode.getTypesInfo();

		return content(types, view, KeyWords.TYPES, KeyWords.TYPE);
	}

	@Override
	public Content getType(String type, String lang, String view) {

		Type object = nextNode.getType(type);

		return content(object, view);
	}

	@Override
	public Content get(String type, String id, String lang, String view, String etag) {

		NXObject object = nextNode.get(type, id, null, lang, true, true, false, false, true, true);

		if (object == null) {
			return notFound(type, lang, view, new ObjectNotFoundException(type, id));
		}

		Content content = content(object, view);
		content.setHeader(HTTPHeader.ETAG, object.getETag());

		return content;
	}
	
	@Override
	public Content getField(String type, String id, String field, String view, String etag) {
		Object objectField = null;
		
		String fieldType = nextNode.getFieldType(type, field);
		
		if (PT.PASSWORD.equals(fieldType)) {
			objectField = Security.HIDDEN_PASSWORD;
		} else {
			objectField = nextNode.getField(type, id, field);
		}		
		
		Content content = content(objectField, view, KeyWords.FIELD);
		content.setHeader(HTTPHeader.ETAG, nextNode.getETag(type, id));

		return content;
	}
	
	@Override
	public Content getNames(String type, String lang, String view, ActionReference aref,
			String search, Long offset) {
		
		String referencingType = null;
		String referencingAction = null;
		String referencingField = null;
		Long limit = null;
		
		if (aref != null) {
		
			referencingType = aref.getReferencingType();
			referencingAction = aref.getReferencingAction();
			referencingField = aref.getReferencingField();
		}
		
		if (referencingType != null) {
			limit = typeSettings.getActionFieldInt64(referencingType, referencingAction,
					referencingField, KeyWords.OBJECT_INPUT_LIMIT);
		} else {
			limit = typeSettings.getActionInt64(type, referencingAction, KeyWords.OBJECT_INPUT_LIMIT);
		}
		
		Names names = nextNode.getNames(type, referencingType,
				referencingAction, referencingField, lang, search, offset, limit);
		
		return content(names, view);
	}

	@Override
	public Content getReferences(String lang, String view) {

		Reference[] references = nextNode.getReferences();

		return content(references, view, KeyWords.REFERENCES, KeyWords.REFERENCE);
	}

	@Override
	public Content select(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		if (ref != null) {
			Filter refFilter = new Filter(ref.getReferencingField(), Comparison.EQUAL,
					ref.getReferencedId(), false);
			
			if (filters != null) {
				filters = (Filter[]) ArrayUtils.add(filters, refFilter);
			} else {
				filters = new Filter[] { refFilter };
			}
		}

		Objects objects = nextNode.select(type, null, lang, filters, search, order, true, true,
				false, false, true, true, offset, limit);

		return content(objects, view);
	}

	public Content content(Object object, String format) {
		return content(object, HTTPStatus.OK, format, null, null);
	}
	
	public Content content(Object object, String format, String rootName) {
		return content(object, HTTPStatus.OK, format, rootName, null);
	}

	public Content content(Object object, String format, String rootName, String itemName) {
		return content(object, HTTPStatus.OK, format, rootName, itemName);
	}

	public Content content(Object object, HTTPStatus status, String format, String rootName, String itemName) {
		Format formatObject = Format.valueOf(format.toUpperCase());
		
		Serial serial = new Serial(object, formatObject, rootName, itemName);
		
		Object value = Format.SMILE.equals(formatObject) ? serial.getBinary() : serial.getString();

		return new Content(value, formatObject, status);
	}
}