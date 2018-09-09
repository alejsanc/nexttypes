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

package com.nexttypes.system;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexttypes.datatypes.AlterFieldResult;
import com.nexttypes.datatypes.AlterIndexResult;
import com.nexttypes.datatypes.Document;
import com.nexttypes.datatypes.FieldInfo;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.HTMLFragment;
import com.nexttypes.datatypes.Image;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.ObjectField;
import com.nexttypes.datatypes.Objects;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.Tuples;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.datatypes.TypeReference;
import com.nexttypes.datatypes.XML;
import com.nexttypes.datatypes.XML.Element;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.interfaces.Node;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;

public class Controller {
	protected Node nextNode;
	protected String type;
	protected String[] objects;
	protected String user;
	protected String[] groups;
	protected Context context;
	protected TypeSettings typeSettings;
	protected Strings strings;

	public Controller(String type, String[] objects, String user, String[] groups, Node nextNode) {
		this.nextNode = nextNode;
		this.type = type;
		this.objects = objects;
		this.user = user;
		this.groups = groups;
		
		context = nextNode.getContext();
		typeSettings = nextNode.getTypeSettings();
		strings = nextNode.getStrings();
	}

	public LinkedHashMap<String, TypeField> getActionFields(String action) {
		return getActions().get(action);
	}

	public LinkedHashMap<String, LinkedHashMap<String, TypeField>> getActions() {
		LinkedHashMap<String, LinkedHashMap<String, TypeField>> actions = null;
		Class classObject = getClass();

		try (InputStream stream = classObject
				.getResourceAsStream(classObject.getSimpleName() + "." + Format.JSON.getExtension())) {

			if (stream != null) {
				ObjectMapper mapper = new ObjectMapper();
				actions = mapper.readValue(stream,
						new com.fasterxml.jackson.core.type.TypeReference<LinkedHashMap<String, LinkedHashMap<String, TypeField>>>() {
						});
			}
		} catch (IOException e) {
			throw new NXException(e);
		}

		if (actions == null) {
			actions = new LinkedHashMap<>();
		}

		return actions;
	}

	public ZonedDateTime addField(String type, String field, TypeField typeField) {
		return nextNode.addField(type, field, typeField);
	}

	public ZonedDateTime addIndex(String type, String index, TypeIndex typeIndex) {
		return nextNode.addIndex(type, index, typeIndex);
	}

	public AlterFieldResult alterField(String type, String field, TypeField typeField) {
		return nextNode.alterField(type, field, typeField);
	}

	public AlterIndexResult alterIndex(String type, String index, TypeIndex typeIndex) {
		return nextNode.alterIndex(type, index, typeIndex);
	}

	public ZonedDateTime renameField(String type, String field, String newName) {
		return nextNode.renameField(type, field, newName);
	}

	public ZonedDateTime renameIndex(String type, String index, String newName) {
		return nextNode.renameIndex(type, index, newName);
	}

	public ZonedDateTime dropField(String type, String field) {
		return nextNode.dropField(type, field);
	}

	public ZonedDateTime dropIndex(String type, String index) {
		return nextNode.dropIndex(type, index);
	}

	public ZonedDateTime insert(NXObject object) {
		return nextNode.insert(object);
	}

	public ZonedDateTime update(NXObject object) {
		return nextNode.update(object);
	}

	public ZonedDateTime update(NXObject object, ZonedDateTime udate) {
		return nextNode.update(object, udate);
	}

	public ZonedDateTime updateId(String type, String id, String newId) {
		return nextNode.updateId(type, id, newId);
	}

	public ZonedDateTime updateField(String type, String id, String field, Object value) {
		return nextNode.updateField(type, id, field, value);
	}

	public ZonedDateTime updatePassword(String type, String id, String field, String currentPassword,
			String newPassword, String newPasswordRepeat) {
		return nextNode.updatePassword(type, id, field, currentPassword, newPassword, newPasswordRepeat);
	}

	public boolean checkPassword(String type, String id, String field, String password) {
		return nextNode.checkPassword(type, id, field, password);
	}

	public ZonedDateTime update(String type, String id, byte[] data) {
		return nextNode.update(type, id, data);
	}

	public NXObject get(String type, String id, String[] fields, String lang, boolean fulltext, boolean binary,
			boolean documentPreview, boolean password, boolean objectName, boolean referencesName) {
		return nextNode.get(type, id, fields, lang, fulltext, binary, documentPreview, password,
				objectName, referencesName);
	}

	public Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return nextNode.select(type, fields, lang, filter, search, order, offset, limit);
	}

	public Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return nextNode.select(type, fields, lang, filter, search, order, fulltext, binary,
				documentPreview, password, objectsName, referencesName, offset, limit);
	}

	public Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return nextNode.select(type, fields, lang, filters, search, order, offset, limit);
	}

	public Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return nextNode.select(type, fields, lang, filters, search, order, fulltext, binary,
				documentPreview, password, objectsName, referencesName, offset, limit);
	}

	public Tuples select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters, String search,
			String[] searchFields, String order, Long offset, Long limit) {
		return nextNode.select(type, sql, parameters, filters, search, searchFields, order, offset, limit);
	}

	public Tuple[] select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters, String order) {
		return nextNode.select(type, sql, parameters, filters, order);
	}

	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return nextNode.selectStream(type, fields, lang, filter, search, order, offset, limit);
	}

	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return nextNode.selectStream(type, fields, lang, filter, search, order, fulltext, binary,
				documentPreview, password, objectsName, referencesName, offset, limit);
	}

	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return nextNode.selectStream(type, fields, lang, filters, search, order, offset, limit);
	}

	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return nextNode.selectStream(type, fields, lang, filters, search, order, fulltext, binary, documentPreview,
				password, objectsName, referencesName, offset, limit);
	}

	public String getName(String type, String id, String lang) {
		return nextNode.getName(type, id, lang);
	}

	public LinkedHashMap<String, String> getNames(String type, String lang) {
		return nextNode.getObjectsName(type, lang);
	}

	public TypeField getTypeField(String type, String field) {
		return nextNode.getTypeField(type, field);
	}

	public LinkedHashMap<String, TypeField> getTypeFields(String type, String[] fields) {
		return nextNode.getTypeFields(type, fields);
	}

	public LinkedHashMap<String, TypeField> getTypeFields(String type) {
		return nextNode.getTypeFields(type);
	}

	public TypeIndex getTypeIndex(String type, String index) {
		return nextNode.getTypeIndex(type, index);
	}

	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String type, String[] indexes) {
		return nextNode.getTypeIndexes(type, indexes);
	}

	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String type) {
		return nextNode.getTypeIndexes(type);
	}

	public String getFieldType(String type, String field) {
		return nextNode.getFieldType(type, field);
	}

	public Tuple getFieldsSize(String type, String id) {
		return nextNode.getFieldsSize(type, id);
	}

	public String getPasswordField(String type, String id, String field) {
		return nextNode.getPasswordField(type, id, field);
	}

	public LinkedHashMap<String, LinkedHashMap<String, TypeField>> getTypeActions(String type) {
		return nextNode.getTypeActions(type);
	}

	public void delete(String type, String... objects) {
		nextNode.delete(type, objects);
	}

	public Object getField(String type, String id, String field) {
		return nextNode.getField(type, id, field);
	}

	public String getStringField(String type, String id, String field) {
		return nextNode.getStringField(type, id, field);
	}

	public byte[] getBinaryField(String type, String id, String field) {
		return nextNode.getBinaryField(type, id, field);
	}

	public Image getImageField(String type, String id, String field) {
		return nextNode.getImageField(type, id, field);
	}

	public byte[] getImageContent(String type, String id, String field) {
		return nextNode.getImageContent(type, id, field);
	}

	public byte[] getImageThumbnail(String type, String id, String field) {
		return nextNode.getImageThumbnail(type, id, field);
	}

	public String getImageContentType(String type, String id, String field) {
		return nextNode.getImageContentType(type, id, field);
	}

	public String getDocumentContentType(String type, String id, String field) {
		return nextNode.getDocumentContentType(type, id, field);
	}

	public XML getXMLField(String type, String id, String field) {
		return nextNode.getXMLField(type, id, field);
	}

	public Element getHTMLElement(String type, String id, String field, String element) {
		return nextNode.getHTMLElement(type, id, field, element);
	}

	public Element getXMLElement(String type, String id, String field, String element) {
		return nextNode.getXMLElement(type, id, field, element);
	}

	public HTMLFragment getHTMLField(String type, String id, String field) {
		return nextNode.getHTMLField(type, id, field);
	}

	public Document getDocumentField(String type, String id, String field) {
		return nextNode.getDocumentField(type, id, field);
	}

	public ObjectField getObjectField(String type, String id, String field) {
		return nextNode.getObjectField(type, id, field);
	}

	public String getFieldContentType(String type, String field) {
		return nextNode.getFieldContentType(type, field);
	}

	public String getCompositeFieldContentType(String type, String id, String field) {
		return nextNode.getCompositeFieldContentType(type, id, field);
	}

	public String getFieldContentType(String type, String id, String field) {
		return nextNode.getFieldContentType(type, id, field);
	}

	public LinkedHashMap<String, String> getFieldsContentType(String type) {
		return nextNode.getFieldsContentType(type);
	}

	public LinkedHashMap<String, FieldInfo> getFieldsInfo(String type, String id) {
		return nextNode.getFieldsInfo(type, id);
	}

	public ZonedDateTime getADate(String type) {
		return nextNode.getADate(type);
	}

	public ZonedDateTime getUDate(String type, String id) {
		return nextNode.getUDate(type, id);
	}

	public String getETag(String type, String id) {
		return nextNode.getETag(type, id);
	}

	public TypeReference[] getUpReferences(String type) {
		return nextNode.getUpReferences(type);
	}

	public TypeReference[] getDownReferences(String type) {
		return nextNode.getDownReferences(type);
	}

	public Long count(String type) {
		return nextNode.count(type);
	}

	public boolean hasObjects(String type) {
		return nextNode.hasObjects(type);
	}

	public boolean hasNullValues(String type, String field) {
		return nextNode.hasNullValues(type, field);
	}
}