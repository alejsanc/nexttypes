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

package com.nexttypes.system;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexttypes.datatypes.ActionResult;
import com.nexttypes.datatypes.AlterFieldResult;
import com.nexttypes.datatypes.AlterIndexResult;
import com.nexttypes.datatypes.AlterResult;
import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Document;
import com.nexttypes.datatypes.FieldInfo;
import com.nexttypes.datatypes.FieldRange;
import com.nexttypes.datatypes.File;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.HTMLFragment;
import com.nexttypes.datatypes.Image;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.Names;
import com.nexttypes.datatypes.ObjectField;
import com.nexttypes.datatypes.Objects;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.Tuples;
import com.nexttypes.datatypes.Type;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.datatypes.TypeReference;
import com.nexttypes.datatypes.UpdateIdResponse;
import com.nexttypes.datatypes.XML;
import com.nexttypes.datatypes.XML.Element;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.ActionException;
import com.nexttypes.exceptions.ActionExecutionException;
import com.nexttypes.exceptions.ActionFieldException;
import com.nexttypes.exceptions.ActionNotFoundException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.nodes.Node;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;

public class Controller {
	protected LinkedHashMap<String, LinkedHashMap<String, TypeField>> actions;
	protected String actionsInfo;	
	protected Node nextNode;
	protected String type;
	protected Auth auth;
	protected Context context;
	protected TypeSettings typeSettings;
	protected Strings strings;

	public Controller(String type, Auth auth, Node nextNode) {
		this.nextNode = nextNode;
		this.type = type;
		this.auth = auth;
		
		context = nextNode.getContext();
		typeSettings = nextNode.getTypeSettings();
		strings = nextNode.getStrings();
		
	}
	
	public ActionResult executeAction(String id, String action, Object... parameters) {
		String[] objects = id == null ? null : new String[] {id};
		return executeAction(objects, action, parameters);
	}
	
	public ActionResult executeAction(String[] objects, String action, Object... parameters) {
		
		Boolean objectsInputNotNull = typeSettings.getActionBoolean(type, action,
				KeyWords.OBJECTS_INPUT_NOT_NULL);
		
		if (objectsInputNotNull && (objects == null || objects.length == 0)) {
			throw new ActionException(type, action, KeyWords.EMPTY_OBJECTS_LIST);
		}		
		
		ActionResult result = null;
		Method method = null;

		LinkedHashMap<String, TypeField> fields = getActionFields(action);

		if (fields == null) {
			throw new ActionNotFoundException(type, action);
		}

		int x = 0;
		for (Map.Entry<String, TypeField> entry : fields.entrySet()) {
			String field = entry.getKey();
			TypeField typeField = entry.getValue();

			if (typeField.isNotNull() && parameters[x] == null) {
				throw new ActionFieldException(type, action, field, KeyWords.EMPTY_FIELD);
			}

			if (parameters[x] != null) {
				checkActionFieldRange(action, field, parameters[x]);
				checkActionFileField(action, field, parameters[x]);
			}

			x++;
		}

		for (Method m : getClass().getMethods()) {
			Action annotation = m.getAnnotation(Action.class);
			if (annotation != null && annotation.value().equals(action)) {
				method = m;
				break;
			}
		}

		if (method == null) {
			throw new ActionNotFoundException(type, action);
		}

		try {
			result = (ActionResult) method.invoke(this, ArrayUtils.insert(0, parameters,
					(Object) objects));
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();

			if (cause instanceof NXException) {
				throw (NXException) cause;
			} else {
				throw new ActionExecutionException(type, action, cause);
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new ActionExecutionException(type, action, e);
		}

		return result;
	}
	
	protected void checkActionFileField(String action, String field, Object value) {
		if (value instanceof File) {
			String[] allowedContentTypes = typeSettings.getActionFieldStringArray(type, action, field,
					KeyWords.ALLOWED_CONTENT_TYPES);

			if (allowedContentTypes != null) {
				String contentType = ((File) value).getContentType();

				if (!ArrayUtils.contains(allowedContentTypes, contentType)) {
					throw new ActionFieldException(type, action, field,
							KeyWords.DISALLOWED_CONTENT_TYPE, contentType);
				}
			}
		}
	}
	
	public Type getType() {
		Type typeObject = nextNode.getType(type);
		typeObject.setActions(getTypeActions());
		return typeObject;
	}
	
	public LinkedHashMap<String, TypeField> getActionFields(String action) {
		return getTypeActions().get(action);
	}
	
	public TypeField getActionField(String action, String field) {
		return getActionFields(action).get(field);
	}
	
	public String getActionFieldType(String action, String field) {
		return getActionField(action, field).getType();
	}
	
	public LinkedHashMap<String, LinkedHashMap<String, TypeField>> getTypeActions() {
		if (actions == null && actionsInfo != null) {		
			try (InputStream stream = getClass().getResourceAsStream(actionsInfo)) {
				if (stream != null) {
					ObjectMapper mapper = new ObjectMapper();
					actions = mapper.readValue(stream, new com.fasterxml.jackson.core.type.TypeReference
						<LinkedHashMap<String, LinkedHashMap<String, TypeField>>>() {});
				}
			} catch (IOException e) {
				throw new NXException(e);
			}	
		}
		
		if (actions == null) {
			actions = new LinkedHashMap<>();
		}	

		return actions;
	}
	
	public void checkActionFieldRange(String action, String field, Object value) {
		
		String fieldType = getActionFieldType(action, field);

		if (PT.isTimeType(fieldType) || PT.isNumericType(fieldType)) {
			FieldRange range = getActionFieldRange(action, field);
			if (range != null && !range.isInRange(value)) {
				throw new ActionFieldException(type, action, field, KeyWords.OUT_OF_RANGE_VALUE,
						value);
			}
		}
	}
	
	public ObjectsStream exportObjects(String[] objects, LinkedHashMap<String, Order> order) {
		return nextNode.exportObjects(type, objects, order);
	}
	
	public ZonedDateTime create(Type type) {
		return nextNode.create(type);
	}
	
	public AlterResult alter(Type type) {
		return nextNode.alter(type);
	}

	public AlterResult alter(Type type, ZonedDateTime adate) {
		return nextNode.alter(type, adate);
	}

	public ZonedDateTime rename(String newName) {
		return nextNode.rename(type, newName);
	}
	
	public Boolean existsType() {
		return nextNode.existsType(type);
	}
	
	public Boolean existsObject(String id) {
		return nextNode.existsObject(type, id);
	}
	
	public FieldRange getActionFieldRange(String action, String field) {
		return getActionField(action, field).getRange();
	}
	
	public FieldRange getFieldRange(String field) {
		return nextNode.getFieldRange(type, field);
	}

	public ZonedDateTime addField(String field, TypeField typeField) {
		return nextNode.addField(type, field, typeField);
	}

	public ZonedDateTime addIndex(String index, TypeIndex typeIndex) {
		return nextNode.addIndex(type, index, typeIndex);
	}

	public AlterFieldResult alterField(String field, TypeField typeField) {
		return nextNode.alterField(type, field, typeField);
	}

	public AlterIndexResult alterIndex(String index, TypeIndex typeIndex) {
		return nextNode.alterIndex(type, index, typeIndex);
	}

	public ZonedDateTime renameField(String field, String newName) {
		return nextNode.renameField(type, field, newName);
	}

	public ZonedDateTime renameIndex(String index, String newName) {
		return nextNode.renameIndex(type, index, newName);
	}

	public ZonedDateTime dropField(String field) {
		return nextNode.dropField(type, field);
	}

	public ZonedDateTime dropIndex(String index) {
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

	public UpdateIdResponse updateId(String id, String newId) {
		return nextNode.updateId(type, id, newId);
	}

	public ZonedDateTime updateField(String id, String field, Object value) {
		return nextNode.updateField(type, id, field, value);
	}

	public ZonedDateTime updatePassword(String id, String field, String currentPassword,
			String newPassword, String newPasswordRepeat) {
		return nextNode.updatePassword(type, id, field, currentPassword, newPassword, newPasswordRepeat);
	}

	public boolean checkPassword(String id, String field, String password) {
		return nextNode.checkPassword(type, id, field, password);
	}

	public ZonedDateTime update(String id, byte[] data) {
		return nextNode.update(type, id, data);
	}

	public NXObject get(String id, String[] fields, String lang, boolean fulltext, boolean binary,
			boolean documentPreview, boolean password, boolean objectName, boolean referencesName) {
		return nextNode.get(type, id, fields, lang, fulltext, binary, documentPreview, password,
				objectName, referencesName);
	}

	public Objects select(String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return nextNode.select(type, fields, lang, filter, search, order, offset, limit);
	}

	public Objects select(String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return nextNode.select(type, fields, lang, filter, search, order, fulltext, binary,
				documentPreview, password, objectsName, referencesName, offset, limit);
	}

	public Objects select(String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return nextNode.select(type, fields, lang, filters, search, order, offset, limit);
	}

	public Objects select(String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return nextNode.select(type, fields, lang, filters, search, order, fulltext, binary,
				documentPreview, password, objectsName, referencesName, offset, limit);
	}

	public Tuples select(StringBuilder sql, ArrayList<Object> parameters, String filters,
			String search, String[] searchFields, String order, Long offset, Long limit) {
		return nextNode.select(type, sql, parameters, filters, search, searchFields, order,
				offset, limit);
	}

	public Tuple[] select(StringBuilder sql, ArrayList<Object> parameters, String filters, String order) {
		return nextNode.select(type, sql, parameters, filters, order);
	}

	public ObjectsStream selectStream(String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return nextNode.selectStream(type, fields, lang, filter, search, order, offset, limit);
	}

	public ObjectsStream selectStream(String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return nextNode.selectStream(type, fields, lang, filter, search, order, fulltext, binary,
				documentPreview, password, objectsName, referencesName, offset, limit);
	}

	public ObjectsStream selectStream(String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return nextNode.selectStream(type, fields, lang, filters, search, order, offset, limit);
	}

	public ObjectsStream selectStream(String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return nextNode.selectStream(type, fields, lang, filters, search, order, fulltext, binary, documentPreview,
				password, objectsName, referencesName, offset, limit);
	}

	public String getName(String id, String lang) {
		return nextNode.getName(type, id, lang);
	}
	
	public Names getNames(String lang) {
		return nextNode.getNames(type, lang);
	}

	public Names getNames(String lang, String search, Long offset,
			Long limit) {
		return nextNode.getNames(type, lang, search, offset, limit);
	}
	
	public Names getNames(String sql, Object[] parameters, String lang,
			String search, Long offset, Long limit) {
		return nextNode.getNames(type, sql, parameters, lang, search, offset, limit);
	}

	public Names getNames(StringBuilder sql, ArrayList<Object> parameters,
			String lang, String search, Long offset, Long limit) {
		return nextNode.getNames(type, sql, parameters, lang, search, offset, limit);
	}
	
	public Names getNames(String referencingType, String referencingAction,
			String referencingField, String lang) {
		return nextNode.getNames(type, referencingType, referencingAction, referencingField, lang);
	}
	
	public Names getNames(String referencingType, String referencingAction,
			String referencingField, String lang, String search, Long offset, Long limit) {
		return nextNode.getNames(type, referencingType, referencingAction, referencingField, lang,
				search, offset, limit);
	}

	public TypeField getTypeField(String field) {
		return nextNode.getTypeField(type, field);
	}

	public LinkedHashMap<String, TypeField> getTypeFields(String[] fields) {
		return nextNode.getTypeFields(type, fields);
	}

	public LinkedHashMap<String, TypeField> getTypeFields() {
		return nextNode.getTypeFields(type);
	}

	public TypeIndex getTypeIndex(String index) {
		return nextNode.getTypeIndex(type, index);
	}

	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String[] indexes) {
		return nextNode.getTypeIndexes(type, indexes);
	}

	public LinkedHashMap<String, TypeIndex> getTypeIndexes() {
		return nextNode.getTypeIndexes(type);
	}

	public String getFieldType(String field) {
		return nextNode.getFieldType(type, field);
	}

	public Tuple getFieldsSize(String id) {
		return nextNode.getFieldsSize(type, id);
	}

	public String getPasswordField(String id, String field) {
		return nextNode.getPasswordField(type, id, field);
	}

	public void delete(String... objects) {
		nextNode.delete(type, objects);
	}

	public Object getField(String id, String field) {
		return nextNode.getField(type, id, field);
	}

	public String getStringField(String id, String field) {
		return nextNode.getStringField(type, id, field);
	}

	public byte[] getBinaryField(String id, String field) {
		return nextNode.getBinaryField(type, id, field);
	}

	public Image getImageField(String id, String field) {
		return nextNode.getImageField(type, id, field);
	}

	public byte[] getImageContent(String id, String field) {
		return nextNode.getImageContent(type, id, field);
	}

	public byte[] getImageThumbnail(String id, String field) {
		return nextNode.getImageThumbnail(type, id, field);
	}

	public String getImageContentType(String id, String field) {
		return nextNode.getImageContentType(type, id, field);
	}

	public String getDocumentContentType(String id, String field) {
		return nextNode.getDocumentContentType(type, id, field);
	}

	public XML getXMLField(String id, String field) {
		return nextNode.getXMLField(type, id, field);
	}

	public Element getHTMLElement(String id, String field, String element) {
		return nextNode.getHTMLElement(type, id, field, element);
	}

	public Element getXMLElement(String id, String field, String element) {
		return nextNode.getXMLElement(type, id, field, element);
	}

	public HTMLFragment getHTMLField(String id, String field) {
		return nextNode.getHTMLField(type, id, field);
	}

	public Document getDocumentField(String id, String field) {
		return nextNode.getDocumentField(type, id, field);
	}

	public ObjectField getObjectField(String id, String field) {
		return nextNode.getObjectField(type, id, field);
	}

	public String getFieldContentType(String field) {
		return nextNode.getFieldContentType(type, field);
	}
	
	public Object getFieldDefault(String field) {
		return nextNode.getFieldDefault(type, field);
	}

	public String getCompositeFieldContentType(String id, String field) {
		return nextNode.getCompositeFieldContentType(type, id, field);
	}

	public String getFieldContentType(String id, String field) {
		return nextNode.getFieldContentType(type, id, field);
	}

	public LinkedHashMap<String, String> getFieldsContentType() {
		return nextNode.getFieldsContentType(type);
	}

	public LinkedHashMap<String, FieldInfo> getFieldsInfo(String id) {
		return nextNode.getFieldsInfo(type, id);
	}

	public ZonedDateTime getADate() {
		return nextNode.getADate(type);
	}

	public ZonedDateTime getUDate(String id) {
		return nextNode.getUDate(type, id);
	}

	public String getETag(String id) {
		return nextNode.getETag(type, id);
	}

	public TypeReference[] getUpReferences() {
		return nextNode.getUpReferences(type);
	}

	public TypeReference[] getDownReferences() {
		return nextNode.getDownReferences(type);
	}

	public Long count() {
		return nextNode.count(type);
	}

	public boolean hasObjects() {
		return nextNode.hasObjects(type);
	}

	public boolean hasNullValues(String field) {
		return nextNode.hasNullValues(type, field);
	}
}