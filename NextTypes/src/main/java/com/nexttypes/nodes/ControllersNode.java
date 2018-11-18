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

package com.nexttypes.nodes;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Savepoint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import com.nexttypes.datatypes.ActionResult;
import com.nexttypes.datatypes.AlterFieldResult;
import com.nexttypes.datatypes.AlterIndexResult;
import com.nexttypes.datatypes.AlterResult;
import com.nexttypes.datatypes.Color;
import com.nexttypes.datatypes.Document;
import com.nexttypes.datatypes.FieldInfo;
import com.nexttypes.datatypes.FieldRange;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.HTMLFragment;
import com.nexttypes.datatypes.Image;
import com.nexttypes.datatypes.ImportObjectsResult;
import com.nexttypes.datatypes.ImportTypesResult;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.ObjectField;
import com.nexttypes.datatypes.ObjectInfo;
import com.nexttypes.datatypes.Objects;
import com.nexttypes.datatypes.Reference;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.Tuples;
import com.nexttypes.datatypes.Type;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.datatypes.TypeInfo;
import com.nexttypes.datatypes.TypeReference;
import com.nexttypes.datatypes.URL;
import com.nexttypes.datatypes.XML;
import com.nexttypes.datatypes.XML.Element;
import com.nexttypes.enums.ImportAction;
import com.nexttypes.enums.NodeMode;
import com.nexttypes.enums.Order;
import com.nexttypes.interfaces.Node;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.interfaces.TypesStream;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Context;
import com.nexttypes.system.Controller;
import com.nexttypes.system.Loader;

public class ControllersNode implements Node {
	protected String user;
	protected String[] groups;
	protected Settings settings;
	protected TypeSettings typeSettings;
	protected Node nextNode;

	public ControllersNode(HTTPRequest request, NodeMode mode) {
		this(request.getUser(), request.getGroups(), mode, request.getLang(), request.getRemoteAddress(),
				request.getContext(), true);
	}

	public ControllersNode(String user, String[] groups, NodeMode mode, String lang, String remoteAddress,
			Context context, boolean useConnectionPool) {
		this.user = user;
		this.groups = groups;
		settings = context.getSettings(Settings.CONTROLLERS_SETTINGS);
		nextNode = Loader.loadNode(settings.getString(Constants.NEXT_NODE), user, groups, mode, lang, remoteAddress,
				context, useConnectionPool);
		typeSettings = context.getTypeSettings(groups);
	}
	
	protected Controller getController(String type) {
		String className = typeSettings.gts(type, Constants.CONTROLLER);

		Controller controller = null;

		ProxyNode proxyNode = new ProxyNode(type, nextNode, this);

		if (className != null) {
			controller = Loader.loadController(className, type, user, groups, proxyNode);
		} else {
			controller = new Controller(type, user, groups, proxyNode);
		}

		return controller;
	}

	protected void setTypeActions(LinkedHashMap<String, Type> types) {
		for (Map.Entry<String, Type> entry : types.entrySet()) {
			entry.getValue().setActions(getTypeActions(entry.getKey()));
		}
	}	
	
	@Override
	public LinkedHashMap<String, TypeField> getActionFields(String type, String action) {
		return getController(type).getActionFields(action);
	}
	
	@Override
	public FieldRange getActionFieldRange(String type, String action, String field) {
		return getController(type).getActionFieldRange(action, field);
	}
	
	@Override
	public TypeField getActionField(String type, String action, String field) {
		return getController(type).getActionField(action, field);
	}
	
	@Override
	public String getActionFieldType(String type, String action, String field) {
		return getController(type).getActionFieldType(action, field);
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, TypeField>> getTypeActions(String type) {
		return getController(type).getTypeActions();
	}

	@Override
	public ActionResult executeAction(String type, String id, String action, Object... parameters) {
		return getController(type).executeAction(id, action, parameters);
	}

	@Override
	public ActionResult executeAction(String type, String[] objects, String action, Object... parameters) {
		return getController(type).executeAction(objects, action, parameters);
	}

	@Override
	public Type getType(String type) {
		return getController(type).getType();
	}

	@Override
	public LinkedHashMap<String, Type> getTypes(String[] types) {
		LinkedHashMap<String, Type> typeObjects = nextNode.getTypes(types);
		setTypeActions(typeObjects);
		return typeObjects;
	}

	@Override
	public TypesStream exportTypes(String[] types, boolean includeObjects) {
		TypesStream export = nextNode.exportTypes(types, includeObjects);
		setTypeActions(export.getTypes());
		return export;
	}

	@Override
	public TypesStream exportTypes(String[] types, Filter filter, boolean includeObjects) {
		TypesStream export = nextNode.exportTypes(types, filter, includeObjects);
		setTypeActions(export.getTypes());
		return export;
	}

	@Override
	public TypesStream exportTypes(String[] types, Filter[] filters, boolean includeObjects) {
		TypesStream export = nextNode.exportTypes(types, filters, includeObjects);
		setTypeActions(export.getTypes());
		return export;
	}

	@Override
	public TypesStream backup(boolean full) {
		TypesStream backup = nextNode.backup(full);
		setTypeActions(backup.getTypes());
		return backup;
	}

	@Override
	public ObjectsStream exportObjects(String type, String[] objects, LinkedHashMap<String, Order> order) {
		return getController(type).exportObjects(objects, order);
	}

	@Override
	public ImportTypesResult importTypes(InputStream types, ImportAction existingTypesAction,
			ImportAction existingObjectsAction) {
		return nextNode.importTypes(types, existingTypesAction, existingObjectsAction);
	}

	@Override
	public ImportTypesResult importTypes(TypesStream types, ImportAction existingTypesAction,
			ImportAction existingObjectsAction) {
		return nextNode.importTypes(types, existingTypesAction, existingObjectsAction);
	}

	@Override
	public ImportObjectsResult importObjects(InputStream objects, ImportAction existingObjectsAction) {
		return nextNode.importObjects(objects, existingObjectsAction);
	}

	@Override
	public ImportObjectsResult importObjects(ObjectsStream objects, ImportAction existingObjectsAction) {
		return nextNode.importObjects(objects, existingObjectsAction);
	}

	@Override
	public ZonedDateTime create(Type type) {
		return getController(type.getName()).create(type);
	}

	@Override
	public ZonedDateTime addField(String type, String field, TypeField typeField) {
		return getController(type).addField(field, typeField);
	}

	@Override
	public ZonedDateTime addIndex(String type, String index, TypeIndex typeIndex) {
		return getController(type).addIndex(index, typeIndex);
	}

	@Override
	public AlterResult alter(Type type) {
		return getController(type.getName()).alter(type);
	}

	@Override
	public AlterResult alter(Type type, ZonedDateTime adate) {
		return getController(type.getName()).alter(type, adate);
	}

	@Override
	public ZonedDateTime rename(String type, String newName) {
		return getController(type).rename(newName);
	}

	@Override
	public AlterFieldResult alterField(String type, String field, TypeField typeField) {
		return getController(type).alterField(field, typeField);
	}

	@Override
	public AlterIndexResult alterIndex(String type, String index, TypeIndex typeIndex) {
		return getController(type).alterIndex(index, typeIndex);
	}

	@Override
	public ZonedDateTime renameField(String type, String field, String newName) {
		return getController(type).renameField(field, newName);
	}

	@Override
	public ZonedDateTime renameIndex(String type, String index, String newName) {
		return getController(type).renameIndex(index, newName);
	}

	@Override
	public ZonedDateTime insert(NXObject object) {
		return getController(object.getType()).insert(object);
	}

	@Override
	public ZonedDateTime update(NXObject object) {
		return getController(object.getType()).update(object);
	}

	@Override
	public ZonedDateTime update(NXObject object, ZonedDateTime udate) {
		return getController(object.getType()).update(object, udate);
	}

	@Override
	public ZonedDateTime updateId(String type, String id, String newId) {
		return getController(type).updateId(id, newId);
	}

	@Override
	public ZonedDateTime updateField(String type, String id, String field, Object value) {
		return getController(type).updateField(id, field, value);
	}

	@Override
	public ZonedDateTime updatePassword(String type, String id, String field, String currentPassword,
			String newPassword, String newPasswordRepeat) {
		return getController(type).updatePassword(id, field, currentPassword, newPassword, newPasswordRepeat);
	}

	@Override
	public boolean checkPassword(String type, String id, String field, String password) {
		return getController(type).checkPassword(id, field, password);
	}

	@Override
	public ZonedDateTime update(String type, String id, byte[] data) {
		return getController(type).update(id, data);
	}

	@Override
	public NXObject get(String type, String id, String[] fields, String lang, boolean fulltext, boolean binary,
			boolean documentPreview, boolean password, boolean objectName, boolean referencesName) {
		return getController(type).get(id, fields, lang, fulltext, binary, documentPreview,
				password, objectName, referencesName);
	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return getController(type).select(fields, lang, filter, search, order, offset, limit);

	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return getController(type).select(fields, lang, filter, search, order, fulltext, binary, documentPreview,
				password, objectsName, referencesName, offset, limit);
	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return getController(type).select(fields, lang, filters, search, order, offset, limit);

	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return getController(type).select(fields, lang, filters, search, order, fulltext, binary, documentPreview,
				password, objectsName, referencesName, offset, limit);
	}

	@Override
	public Tuples select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters,
			String search, String[] searchFields, String[] groupFields, String order, Long offset,
			Long limit) {
		return getController(type).select(sql, parameters, filters, search, searchFields,
				groupFields, order, offset, limit);
	}

	@Override
	public Tuple[] select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters, String order) {
		return getController(type).select(sql, parameters, filters, order);
	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return getController(type).selectStream(fields, lang, filter, search, order, offset, limit);

	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return getController(type).selectStream(fields, lang, filter, search, order, fulltext, binary,
				documentPreview, password, objectsName, referencesName, offset, limit);
	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return getController(type).selectStream(fields, lang, filters, search, order, offset, limit);

	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {
		return getController(type).selectStream(fields, lang, filters, search, order, fulltext, binary,
				documentPreview, password, objectsName, referencesName, offset, limit);
	}

	@Override
	public String[] getTypesName() {
		return nextNode.getTypesName();
	}

	@Override
	public TypeInfo[] getTypesInfo() {
		return nextNode.getTypesInfo();
	}

	@Override
	public String getName(String type, String id, String lang) {
		return getController(type).getName(id, lang);
	}

	@Override
	public LinkedHashMap<String, String> getObjectsName(String type, String lang) {
		return getController(type).getNames(lang);
	}

	@Override
	public LinkedHashMap<String, ObjectInfo[]> getObjectsInfo(String[] types) {
		return nextNode.getObjectsInfo(types);
	}

	@Override
	public TypeField getTypeField(String type, String field) {
		return getController(type).getTypeField(field);
	}

	@Override
	public LinkedHashMap<String, TypeField> getTypeFields(String type, String... fields) {
		return getController(type).getTypeFields(fields);
	}

	@Override
	public LinkedHashMap<String, TypeField> getTypeFields(String type) {
		return getController(type).getTypeFields();
	}

	@Override
	public TypeIndex getTypeIndex(String type, String index) {
		return getController(type).getTypeIndex(index);
	}

	@Override
	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String type, String... indexes) {
		return getController(type).getTypeIndexes(indexes);
	}

	@Override
	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String type) {
		return getController(type).getTypeIndexes();
	}

	@Override
	public String getFieldType(String type, String field) {
		return getController(type).getFieldType(field);
	}

	@Override
	public Tuple getFieldsSize(String type, String id) {
		return getController(type).getFieldsSize(id);
	}

	@Override
	public void drop(String... types) {
		nextNode.drop(types);
	}

	@Override
	public ZonedDateTime dropField(String type, String field) {
		return getController(type).dropField(field);
	}

	@Override
	public ZonedDateTime dropIndex(String type, String index) {
		return getController(type).dropIndex(index);
	}

	@Override
	public void delete(String type, String... objects) {
		getController(type).delete(objects);
	}

	@Override
	public Long count(String type) {
		return getController(type).count();
	}

	@Override
	public boolean hasObjects(String type) {
		return getController(type).hasObjects();
	}

	@Override
	public boolean hasNullValues(String type, String field) {
		return getController(type).hasNullValues(field);
	}

	@Override
	public Long count(String sql, Object... parameters) {
		return nextNode.count(sql, parameters);
	}

	@Override
	public Object getField(String type, String id, String field) {
		return getController(type).getField(id, field);
	}

	@Override
	public String getStringField(String type, String id, String field) {
		return getController(type).getStringField(id, field);
	}

	@Override
	public byte[] getBinaryField(String type, String id, String field) {
		return getController(type).getBinaryField(id, field);
	}

	@Override
	public Image getImageField(String type, String id, String field) {
		return getController(type).getImageField(id, field);
	}

	@Override
	public byte[] getImageContent(String type, String id, String field) {
		return getController(type).getImageContent(id, field);
	}

	@Override
	public byte[] getImageThumbnail(String type, String id, String field) {
		return getController(type).getImageThumbnail(id, field);
	}

	@Override
	public String getImageContentType(String type, String id, String field) {
		return getController(type).getImageContentType(id, field);
	}

	@Override
	public String getDocumentContentType(String type, String id, String field) {
		return getController(type).getDocumentContentType(id, field);
	}

	@Override
	public XML getXMLField(String type, String id, String field) {
		return getController(type).getXMLField(id, field);
	}

	@Override
	public Element getHTMLElement(String type, String id, String field, String element) {
		return getController(type).getHTMLElement(id, field, element);
	}

	@Override
	public Element getXMLElement(String type, String id, String field, String element) {
		return getController(type).getXMLElement(id, field, element);
	}

	@Override
	public HTMLFragment getHTMLField(String type, String id, String field) {
		return getController(type).getHTMLField(id, field);
	}

	@Override
	public Document getDocumentField(String type, String id, String field) {
		return getController(type).getDocumentField(id, field);
	}

	@Override
	public ObjectField getObjectField(String type, String id, String field) {
		return getController(type).getObjectField(id, field);
	}

	@Override
	public String getPasswordField(String type, String id, String field) {
		return getController(type).getPasswordField(id, field);
	}

	@Override
	public String getFieldContentType(String type, String field) {
		return getController(type).getFieldContentType(type, field);
	}

	@Override
	public String getFieldContentType(String type, String id, String field) {
		return getController(type).getFieldContentType(id, field);
	}
	
	@Override
	public Object getFieldDefault(String type, String field) {
		return getController(type).getFieldDefault(field);
	}

	@Override
	public String getCompositeFieldContentType(String type, String id, String field) {
		return getController(type).getCompositeFieldContentType(id, field);
	}

	@Override
	public LinkedHashMap<String, String> getFieldsContentType(String type) {
		return getController(type).getFieldsContentType();
	}

	@Override
	public LinkedHashMap<String, FieldInfo> getFieldsInfo(String type, String id) {
		return getController(type).getFieldsInfo(id);
	}

	@Override
	public ZonedDateTime getADate(String type) {
		return getController(type).getADate();
	}

	@Override
	public ZonedDateTime getUDate(String type, String id) {
		return getController(type).getUDate(id);
	}

	@Override
	public String getETag(String type, String id) {
		return getController(type).getETag(id);
	}
	
	@Override
	public FieldRange getFieldRange(String type, String field) {
		return getController(type).getFieldRange(field);
	}

	@Override
	public int execute(String sql) {
		return nextNode.execute(sql);
	}

	@Override
	public int execute(String sql, Object... parameters) {
		return nextNode.execute(sql, parameters);
	}

	@Override
	public int execute(String sql, Integer expectedRows, Object... parameters) {
		return nextNode.execute(sql, expectedRows, parameters);
	}

	@Override
	public int execute(String sql, boolean useSavepoint, Integer expectedRows, Object... parameters) {
		return nextNode.execute(sql, useSavepoint, expectedRows, parameters);
	}

	@Override
	public Savepoint setSavepoint() {
		return nextNode.setSavepoint();
	}

	@Override
	public void rollback() {
		nextNode.rollback();
	}

	@Override
	public void rollback(Savepoint savepoint) {
		nextNode.rollback(savepoint);
	}

	@Override
	public void commit() {
		nextNode.commit();
	}

	@Override
	public void close() {
		nextNode.close();
	}

	@Override
	public Reference[] getReferences() {
		return nextNode.getReferences();
	}

	@Override
	public TypeReference[] getUpReferences(String type) {
		return getController(type).getUpReferences();
	}

	@Override
	public Reference[] getUpReferences(String[] types) {
		return nextNode.getUpReferences(types);
	}

	@Override
	public TypeReference[] getDownReferences(String type) {
		return getController(type).getDownReferences();
	}

	@Override
	public Short getInt16(String sql) {
		return nextNode.getInt16(sql);
	}

	@Override
	public Short getInt16(String sql, Object... parameters) {
		return nextNode.getInt16(sql, parameters);
	}

	@Override
	public Integer getInt32(String sql) {
		return nextNode.getInt32(sql);
	}

	@Override
	public Integer getInt32(String sql, Object... parameters) {
		return nextNode.getInt32(sql, parameters);
	}

	@Override
	public Long getInt64(String sql) {
		return nextNode.getInt64(sql);
	}

	@Override
	public Long getInt64(String sql, Object... parameters) {
		return nextNode.getInt64(sql, parameters);
	}

	@Override
	public Float getFloat32(String sql) {
		return nextNode.getFloat32(sql);
	}

	@Override
	public Float getFloat32(String sql, Object... parameters) {
		return nextNode.getFloat32(sql, parameters);
	}

	@Override
	public Double getFloat64(String sql) {
		return nextNode.getFloat64(sql);
	}

	@Override
	public Double getFloat64(String sql, Object... parameters) {
		return nextNode.getFloat64(sql, parameters);
	}

	@Override
	public BigDecimal getNumeric(String sql) {
		return nextNode.getNumeric(sql);
	}

	@Override
	public BigDecimal getNumeric(String sql, Object... parameters) {
		return nextNode.getNumeric(sql, parameters);
	}

	@Override
	public String getString(String sql) {
		return nextNode.getString(sql);
	}

	@Override
	public String getString(String sql, Object... parameters) {
		return nextNode.getString(sql, parameters);
	}

	@Override
	public String getText(String sql) {
		return nextNode.getText(sql);
	}

	@Override
	public String getText(String sql, Object... parameters) {
		return nextNode.getText(sql, parameters);
	}

	@Override
	public LocalDate getDate(String sql) {
		return nextNode.getDate(sql);
	}

	@Override
	public LocalDate getDate(String sql, Object... parameters) {
		return nextNode.getDate(sql, parameters);
	}

	@Override
	public LocalTime getTime(String sql) {
		return nextNode.getTime(sql);
	}

	@Override
	public LocalTime getTime(String sql, Object... parameters) {
		return nextNode.getTime(sql, parameters);
	}

	@Override
	public LocalDateTime getDateTime(String sql) {
		return nextNode.getDateTime(sql);
	}

	@Override
	public LocalDateTime getDateTime(String sql, Object... parameters) {
		return nextNode.getDateTime(sql, parameters);
	}

	@Override
	public byte[] getBinary(String sql) {
		return nextNode.getBinary(sql);
	}

	@Override
	public byte[] getBinary(String sql, Object... parameters) {
		return nextNode.getBinary(sql, parameters);
	}

	@Override
	public HTMLFragment getHTML(String sql, String allowedTags) {
		return nextNode.getHTML(sql, allowedTags);
	}

	@Override
	public HTMLFragment getHTML(String sql, String allowedTags, Object... parameters) {
		return nextNode.getHTML(sql, allowedTags, parameters);
	}

	@Override
	public URL getURL(String sql) {
		return nextNode.getURL(sql);
	}

	@Override
	public URL getURL(String sql, Object... parameters) {
		return nextNode.getURL(sql, parameters);
	}

	@Override
	public InternetAddress getEmail(String sql) {
		return nextNode.getEmail(sql);
	}

	@Override
	public InternetAddress getEmail(String sql, Object... parameters) {
		return nextNode.getEmail(sql, parameters);
	}

	@Override
	public String getTel(String sql) {
		return nextNode.getTel(sql);
	}

	@Override
	public String getTel(String sql, Object... parameters) {
		return nextNode.getTel(sql, parameters);
	}

	@Override
	public ZoneId getTimeZone(String sql) {
		return nextNode.getTimeZone(sql);
	}

	@Override
	public ZoneId getTimeZone(String sql, Object... parameters) {
		return nextNode.getTimeZone(sql, parameters);
	}

	@Override
	public Color getColor(String sql) {
		return nextNode.getColor(sql);
	}

	@Override
	public Color getColor(String sql, Object... parameters) {
		return nextNode.getColor(sql, parameters);
	}

	@Override
	public Image getImage(String sql) {
		return nextNode.getImage(sql);
	}

	@Override
	public Image getImage(String sql, Object... parameters) {
		return nextNode.getImage(sql, parameters);
	}

	@Override
	public Document getDocument(String sql) {
		return nextNode.getDocument(sql);
	}

	@Override
	public Document getDocument(String sql, Object... parameters) {
		return nextNode.getDocument(sql, parameters);
	}

	@Override
	public ZonedDateTime getUTCDateTime(String sql) {
		return nextNode.getUTCDateTime(sql);
	}

	@Override
	public ZonedDateTime getUTCDateTime(String sql, Object... parameters) {
		return nextNode.getUTCDateTime(sql, parameters);
	}

	@Override
	public Short[] getInt16Array(String sql) {
		return nextNode.getInt16Array(sql);
	}

	@Override
	public Short[] getInt16Array(String sql, Object... parameters) {
		return nextNode.getInt16Array(sql, parameters);
	}

	@Override
	public Integer[] getInt32Array(String sql) {
		return nextNode.getInt32Array(sql);
	}

	@Override
	public Integer[] getInt32Array(String sql, Object... parameters) {
		return nextNode.getInt32Array(sql, parameters);
	}

	@Override
	public Long[] getInt64Array(String sql) {
		return nextNode.getInt64Array(sql);
	}

	@Override
	public Long[] getInt64Array(String sql, Object... parameters) {
		return nextNode.getInt64Array(sql, parameters);
	}

	@Override
	public Float[] getFloat32Array(String sql) {
		return nextNode.getFloat32Array(sql);
	}

	@Override
	public Float[] getFloat32Array(String sql, Object... parameters) {
		return nextNode.getFloat32Array(sql, parameters);
	}

	@Override
	public Double[] getFloat64Array(String sql) {
		return nextNode.getFloat64Array(sql);
	}

	@Override
	public Double[] getFloat64Array(String sql, Object... parameters) {
		return nextNode.getFloat64Array(sql, parameters);
	}

	@Override
	public BigDecimal[] getNumericArray(String sql) {
		return nextNode.getNumericArray(sql);
	}

	@Override
	public BigDecimal[] getNumericArray(String sql, Object... parameters) {
		return nextNode.getNumericArray(sql, parameters);
	}

	@Override
	public Boolean[] getBooleanArray(String sql) {
		return nextNode.getBooleanArray(sql);
	}

	@Override
	public Boolean[] getBooleanArray(String sql, Object... parameters) {
		return nextNode.getBooleanArray(sql, parameters);
	}

	@Override
	public String[] getStringArray(String sql) {
		return nextNode.getStringArray(sql);
	}

	@Override
	public String[] getStringArray(String sql, Object... parameters) {
		return nextNode.getStringArray(sql, parameters);
	}

	@Override
	public String[] getTextArray(String sql) {
		return nextNode.getTextArray(sql);
	}

	@Override
	public String[] getTextArray(String sql, Object... parameters) {
		return nextNode.getTextArray(sql, parameters);
	}

	@Override
	public LocalDate[] getDateArray(String sql) {
		return nextNode.getDateArray(sql);
	}

	@Override
	public LocalDate[] getDateArray(String sql, Object... parameters) {
		return nextNode.getDateArray(sql, parameters);
	}

	@Override
	public LocalTime[] getTimeArray(String sql) {
		return nextNode.getTimeArray(sql);
	}

	@Override
	public LocalTime[] getTimeArray(String sql, Object... parameters) {
		return nextNode.getTimeArray(sql, parameters);
	}

	@Override
	public LocalDateTime[] getDateTimeArray(String sql) {
		return nextNode.getDateTimeArray(sql);
	}

	@Override
	public LocalDateTime[] getDateTimeArray(String sql, Object... parameters) {
		return nextNode.getDateTimeArray(sql, parameters);
	}

	@Override
	public ZonedDateTime[] getUTCDateTimeArray(String sql) {
		return nextNode.getUTCDateTimeArray(sql);
	}

	@Override
	public ZonedDateTime[] getUTCDateTimeArray(String sql, Object... parameters) {
		return nextNode.getUTCDateTimeArray(sql, parameters);
	}

	@Override
	public byte[][] getBinaryArray(String sql) {
		return nextNode.getBinaryArray(sql);
	}

	@Override
	public byte[][] getBinaryArray(String sql, Object... parameters) {
		return nextNode.getBinaryArray(sql, parameters);
	}

	@Override
	public HTMLFragment[] getHTMLArray(String sql, String allowedTags) {
		return nextNode.getHTMLArray(sql, allowedTags);
	}

	@Override
	public HTMLFragment[] getHTMLArray(String sql, String allowedTags, Object... parameters) {
		return nextNode.getHTMLArray(sql, allowedTags, parameters);
	}

	@Override
	public URL[] getURLArray(String sql) {
		return nextNode.getURLArray(sql);
	}

	@Override
	public URL[] getURLArray(String sql, Object... parameters) {
		return nextNode.getURLArray(sql, parameters);
	}

	@Override
	public InternetAddress[] getEmailArray(String sql) {
		return nextNode.getEmailArray(sql);
	}

	@Override
	public InternetAddress[] getEmailArray(String sql, Object... parameters) {
		return nextNode.getEmailArray(sql, parameters);
	}

	@Override
	public String[] getTelArray(String sql) {
		return nextNode.getTelArray(sql);
	}

	@Override
	public String[] getTelArray(String sql, Object... parameters) {
		return nextNode.getTelArray(sql, parameters);
	}

	@Override
	public ZoneId[] getTimeZoneArray(String sql) {
		return nextNode.getTimeZoneArray(sql);
	}

	@Override
	public ZoneId[] getTimeZoneArray(String sql, Object... parameters) {
		return nextNode.getTimeZoneArray(sql, parameters);
	}

	@Override
	public Color[] getColorArray(String sql) {
		return nextNode.getColorArray(sql);
	}

	@Override
	public Color[] getColorArray(String sql, Object... parameters) {
		return nextNode.getColorArray(sql, parameters);
	}

	@Override
	public Image[] getImageArray(String sql) {
		return nextNode.getImageArray(sql);
	}

	@Override
	public Image[] getImageArray(String sql, Object... parameters) {
		return nextNode.getImageArray(sql, parameters);
	}

	@Override
	public Document[] getDocumentArray(String sql) {
		return nextNode.getDocumentArray(sql);
	}

	@Override
	public Document[] getDocumentArray(String sql, Object... parameters) {
		return nextNode.getDocumentArray(sql, parameters);
	}

	@Override
	public <T> T[] getArray(String sql, Class<T> type) {
		return nextNode.getArray(sql, type);
	}

	@Override
	public <T> T[] getArray(String sql, Class<T> type, Object... parameters) {
		return nextNode.getArray(sql, type, parameters);
	}

	@Override
	public Object getObject(String sql) {
		return nextNode.getObject(sql);
	}

	@Override
	public Object getObject(String sql, Object... parameters) {
		return nextNode.getObject(sql, parameters);
	}

	@Override
	public Tuple getMatrix(String sql, String[] axes) {
		return nextNode.getMatrix(sql, axes);
	}

	@Override
	public Tuple getMatrix(String sql, String[] axes, Object... parameters) {
		return nextNode.getMatrix(sql, axes, parameters);
	}

	@Override
	public Tuple getTuple(String sql) {
		return nextNode.getTuple(sql);
	}

	@Override
	public Tuple getTuple(String sql, Object... parameters) {
		return nextNode.getTuple(sql, parameters);
	}

	@Override
	public Tuple[] query(String sql) {
		return nextNode.query(sql);
	}

	@Override
	public Tuple[] query(String sql, Object... parameters) {
		return nextNode.query(sql, parameters);
	}

	@Override
	public <T> T[] query(String sql, Class<T> type) {
		return nextNode.query(sql, type);
	}

	@Override
	public <T> T[] query(String sql, Class<T> type, Object... parameters) {
		return nextNode.query(sql, type, parameters);
	}

	@Override
	public String getUser() {
		return nextNode.getUser();
	}

	@Override
	public String[] getGroups() {
		return nextNode.getGroups();
	}

	@Override
	public void setUser(String user) {
		nextNode.setUser(user);
	}

	@Override
	public void setGroups(String[] groups) {
		nextNode.setGroups(groups);
	}

	@Override
	public Boolean existsType(String type) {
		return getController(type).existsType();
	}

	@Override
	public Boolean existsObject(String type, String id) {
		return getController(type).existsObject(id);
	}

	@Override
	public Boolean getBoolean(String sql) {
		return nextNode.getBoolean(sql);
	}

	@Override
	public Boolean getBoolean(String sql, Object... parameters) {
		return nextNode.getBoolean(sql, parameters);
	}

	@Override
	public void setDeferredConstraints(boolean status) {
		nextNode.setDeferredConstraints(status);
	}

	@Override
	public Context getContext() {
		return nextNode.getContext();
	}

	@Override
	public Strings getStrings() {
		return nextNode.getStrings();
	}

	@Override
	public TypeSettings getTypeSettings() {
		return nextNode.getTypeSettings();
	}

	@Override
	public String[] getGroups(String user) {
		return nextNode.getGroups(user);
	}
}