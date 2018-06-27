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

import javax.mail.internet.InternetAddress;

import com.nexttypes.datatypes.ActionResult;
import com.nexttypes.datatypes.AlterFieldResult;
import com.nexttypes.datatypes.AlterIndexResult;
import com.nexttypes.datatypes.AlterResult;
import com.nexttypes.datatypes.Color;
import com.nexttypes.datatypes.Document;
import com.nexttypes.datatypes.FieldInfo;
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
import com.nexttypes.datatypes.URI;
import com.nexttypes.datatypes.XML;
import com.nexttypes.datatypes.XML.Element;
import com.nexttypes.enums.ImportAction;
import com.nexttypes.enums.Order;
import com.nexttypes.interfaces.Node;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.interfaces.TypesStream;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.system.Context;

public class ProxyNode implements Node {
	protected String type;
	protected Node nextNode;
	protected Node controllersNode;

	public ProxyNode(String type, Node nextNode, Node controllersNode) {
		this.type = type;
		this.nextNode = nextNode;
		this.controllersNode = controllersNode;
	}

	public Node getNextNode(String type) {
		Node node = null;

		if (type.equals(this.type)) {
			node = nextNode;
		} else {
			node = controllersNode;
		}

		return node;
	}

	@Override
	public ZonedDateTime create(Type type) {
		return getNextNode(type.getName()).create(type);
	}

	@Override
	public ZonedDateTime addField(String type, String field, TypeField typeField) {
		return getNextNode(type).addField(type, field, typeField);
	}

	@Override
	public ZonedDateTime addIndex(String type, String index, TypeIndex typeIndex) {
		return getNextNode(type).addIndex(type, index, typeIndex);
	}

	@Override
	public AlterResult alter(Type type) {
		return getNextNode(type.getName()).alter(type);
	}

	@Override
	public AlterResult alter(Type type, ZonedDateTime adate) {
		return getNextNode(type.getName()).alter(type, adate);
	}

	@Override
	public ZonedDateTime rename(String type, String newName) {
		return getNextNode(type).rename(type, newName);
	}

	@Override
	public AlterFieldResult alterField(String type, String field, TypeField typeField) {
		return getNextNode(type).alterField(type, field, typeField);
	}

	@Override
	public AlterIndexResult alterIndex(String type, String index, TypeIndex typeIndex) {
		return getNextNode(type).alterIndex(type, index, typeIndex);
	}

	@Override
	public ZonedDateTime renameField(String type, String field, String newName) {
		return getNextNode(type).renameField(type, field, newName);
	}

	@Override
	public ZonedDateTime renameIndex(String type, String index, String newName) {
		return getNextNode(type).renameIndex(type, index, newName);
	}

	@Override
	public ZonedDateTime insert(NXObject object) {
		return getNextNode(object.getType()).insert(object);
	}

	@Override
	public ZonedDateTime update(NXObject object) {
		return getNextNode(object.getType()).update(object);
	}

	@Override
	public ZonedDateTime update(NXObject object, ZonedDateTime udate) {
		return getNextNode(object.getType()).update(object, udate);
	}

	@Override
	public ZonedDateTime update(String type, String id, byte[] data) {
		return getNextNode(type).update(type, id, data);
	}

	@Override
	public NXObject get(String type, String id, String[] fields, String lang, boolean fulltext, boolean binary,
			boolean documentPreview, boolean password) {
		return getNextNode(type).get(type, id, fields, lang, fulltext, binary, documentPreview, password);
	}

	@Override
	public ZonedDateTime updateId(String type, String id, String newId) {
		return getNextNode(type).updateId(type, id, newId);
	}

	@Override
	public ZonedDateTime updateField(String type, String id, String field, Object value) {
		return getNextNode(type).updateField(type, id, field, value);
	}

	@Override
	public ZonedDateTime updatePassword(String type, String id, String field, String currentPassword,
			String newPassword, String newPasswordRepeat) {
		return getNextNode(type).updatePassword(type, id, field, currentPassword, newPassword, newPasswordRepeat);
	}

	@Override
	public boolean checkPassword(String type, String id, String field, String password) {
		return getNextNode(type).checkPassword(type, id, field, password);
	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return getNextNode(type).select(type, fields, lang, filter, search, order, offset, limit);
	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, Long offset, Long limit) {
		return getNextNode(type).select(type, fields, lang, filter, search, order, fulltext, binary, documentPreview,
				password, offset, limit);
	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return getNextNode(type).select(type, fields, lang, filters, search, order, offset, limit);
	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, Long offset, Long limit) {
		return getNextNode(type).select(type, fields, lang, filters, search, order, fulltext, binary, documentPreview,
				password, offset, limit);
	}

	@Override
	public Tuples select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters, String search,
			String[] searchFields, String order, Long offset, Long limit) {
		return getNextNode(type).select(type, sql, parameters, filters, search, searchFields, order, offset, limit);
	}

	@Override
	public Tuple[] select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters, String order) {
		return getNextNode(type).select(type, sql, parameters, filters, order);
	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return getNextNode(type).selectStream(type, fields, lang, filter, search, order, offset, limit);
	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, Long offset, Long limit) {
		return getNextNode(type).selectStream(type, fields, lang, filter, search, order, fulltext, binary,
				documentPreview, password, offset, limit);
	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return getNextNode(type).selectStream(type, fields, lang, filters, search, order, offset, limit);
	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, Long offset, Long limit) {
		return getNextNode(type).selectStream(type, fields, lang, filters, search, order, fulltext, binary,
				documentPreview, password, offset, limit);
	}

	@Override
	public Type getType(String type) {
		return getNextNode(type).getType(type);
	}

	@Override
	public LinkedHashMap<String, Type> getTypes(String[] types) {
		return nextNode.getTypes(types);
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
	public Boolean existsType(String type) {
		return nextNode.existsType(type);
	}

	@Override
	public Boolean existsObject(String type, String id) {
		return nextNode.existsObject(type, id);
	}

	@Override
	public String getName(String type, String id, String lang) {
		return getNextNode(type).getName(type, id, lang);
	}

	@Override
	public LinkedHashMap<String, String> getObjectsName(String type, String lang) {
		return getNextNode(type).getObjectsName(type, lang);
	}

	@Override
	public LinkedHashMap<String, ObjectInfo[]> getObjectsInfo(String[] types) {
		return nextNode.getObjectsInfo(types);
	}

	@Override
	public Reference[] getReferences() {
		return nextNode.getReferences();
	}

	@Override
	public TypeReference[] getDownReferences(String type) {
		return getNextNode(type).getDownReferences(type);
	}

	@Override
	public TypeReference[] getUpReferences(String type) {
		return getNextNode(type).getUpReferences(type);
	}

	@Override
	public Reference[] getUpReferences(String[] types) {
		return nextNode.getUpReferences(types);
	}

	@Override
	public TypeField getTypeField(String type, String field) {
		return getNextNode(type).getTypeField(type, field);
	}

	@Override
	public LinkedHashMap<String, TypeField> getTypeFields(String type, String... fields) {
		return getNextNode(type).getTypeFields(type, fields);
	}

	@Override
	public LinkedHashMap<String, TypeField> getTypeFields(String type) {
		return getNextNode(type).getTypeFields(type);
	}

	@Override
	public TypeIndex getTypeIndex(String type, String index) {
		return getNextNode(type).getTypeIndex(type, index);
	}

	@Override
	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String type, String... indexes) {
		return getNextNode(type).getTypeIndexes(type, indexes);
	}

	@Override
	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String type) {
		return getNextNode(type).getTypeIndexes(type);
	}

	@Override
	public String getFieldType(String type, String field) {
		return getNextNode(type).getFieldType(type, field);
	}

	@Override
	public Tuple getFieldsSize(String type, String id) {
		return getNextNode(type).getFieldsSize(type, id);
	}

	@Override
	public LinkedHashMap<String, TypeField> getActionFields(String type, String action) {
		return getNextNode(type).getActionFields(type, action);
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, TypeField>> getTypeActions(String type) {
		return getNextNode(type).getTypeActions(type);
	}

	@Override
	public void drop(String... types) {
		nextNode.drop(types);
	}

	@Override
	public ZonedDateTime dropField(String type, String field) {
		return getNextNode(type).dropField(type, field);
	}

	@Override
	public ZonedDateTime dropIndex(String type, String index) {
		return getNextNode(type).dropIndex(type, index);
	}

	@Override
	public void delete(String type, String... objects) {
		getNextNode(type).delete(type, objects);
	}

	@Override
	public Object getField(String type, String id, String field) {
		return getNextNode(type).getField(type, id, field);
	}

	@Override
	public String getStringField(String type, String id, String field) {
		return getNextNode(type).getStringField(type, id, field);
	}

	@Override
	public byte[] getBinaryField(String type, String id, String field) {
		return getNextNode(type).getBinaryField(type, id, field);
	}

	@Override
	public Image getImageField(String type, String id, String field) {
		return getNextNode(type).getImageField(type, id, field);
	}

	@Override
	public byte[] getImageContent(String type, String id, String field) {
		return getNextNode(type).getImageContent(type, id, field);
	}

	@Override
	public byte[] getImageThumbnail(String type, String id, String field) {
		return getNextNode(type).getImageThumbnail(type, id, field);
	}

	@Override
	public String getImageContentType(String type, String id, String field) {
		return getNextNode(type).getImageContentType(type, id, field);
	}

	@Override
	public String getDocumentContentType(String type, String id, String field) {
		return getNextNode(type).getDocumentContentType(type, id, field);
	}

	@Override
	public XML getXMLField(String type, String id, String field) {
		return getNextNode(type).getXMLField(type, id, field);
	}

	@Override
	public Element getHTMLElement(String type, String id, String field, String element) {
		return getNextNode(type).getHTMLElement(type, id, field, element);
	}

	@Override
	public Element getXMLElement(String type, String id, String field, String element) {
		return getNextNode(type).getXMLElement(type, id, field, element);
	}

	@Override
	public HTMLFragment getHTMLField(String type, String id, String field) {
		return getNextNode(type).getHTMLField(type, id, field);
	}

	@Override
	public Document getDocumentField(String type, String id, String field) {
		return getNextNode(type).getDocumentField(type, id, field);
	}

	@Override
	public String getPasswordField(String type, String id, String field) {
		return getNextNode(type).getPasswordField(type, id, field);
	}

	@Override
	public ObjectField getObjectField(String type, String id, String field) {
		return getNextNode(type).getObjectField(type, id, field);
	}

	@Override
	public String getFieldContentType(String type, String field) {
		return getNextNode(type).getFieldContentType(type, field);
	}

	@Override
	public String getCompositeFieldContentType(String type, String id, String field) {
		return getNextNode(type).getCompositeFieldContentType(type, id, field);
	}

	@Override
	public String getFieldContentType(String type, String id, String field) {
		return getNextNode(type).getFieldContentType(type, id, field);
	}

	@Override
	public LinkedHashMap<String, String> getFieldsContentType(String type) {
		return getNextNode(type).getFieldsContentType(type);
	}

	@Override
	public LinkedHashMap<String, FieldInfo> getFieldsInfo(String type, String id) {
		return getNextNode(type).getFieldsInfo(type, id);
	}

	@Override
	public ZonedDateTime getADate(String type) {
		return getNextNode(type).getADate(type);
	}

	@Override
	public ZonedDateTime getUDate(String type, String id) {
		return getNextNode(type).getUDate(type, id);
	}

	@Override
	public String getETag(String type, String id) {
		return getNextNode(type).getETag(type, id);
	}

	@Override
	public ActionResult executeAction(String type, String id, String action, Object... parameters) {
		return getNextNode(type).executeAction(type, id, action, parameters);
	}

	@Override
	public ActionResult executeAction(String type, String[] objects, String action, Object... parameters) {
		return getNextNode(type).executeAction(type, objects, action, parameters);
	}

	@Override
	public Long count(String type) {
		return getNextNode(type).count(type);
	}

	@Override
	public boolean hasObjects(String type) {
		return getNextNode(type).hasObjects(type);
	}

	@Override
	public boolean hasNullValues(String type, String field) {
		return getNextNode(type).hasNullValues(type, field);
	}

	@Override
	public Long count(String sql, Object... parameters) {
		return nextNode.count(sql, parameters);
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
	public TypesStream exportTypes(String[] types, String lang, boolean includeObjects) {
		return nextNode.exportTypes(types, lang, includeObjects);
	}

	@Override
	public TypesStream exportTypes(String[] types, String lang, Filter filter, boolean includeObjects) {
		return nextNode.exportTypes(types, lang, filter, includeObjects);
	}

	@Override
	public TypesStream exportTypes(String[] types, String lang, Filter[] filters, boolean includeObjects) {
		return nextNode.exportTypes(types, lang, filters, includeObjects);
	}

	@Override
	public TypesStream backup(String lang, boolean full) {
		return nextNode.backup(lang, full);
	}

	@Override
	public ObjectsStream exportObjects(String type, String[] objects, String lang, LinkedHashMap<String, Order> order) {
		return nextNode.exportObjects(type, objects, lang, order);
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
	public LocalDateTime getDatetime(String sql) {
		return nextNode.getDatetime(sql);
	}

	@Override
	public LocalDateTime getDatetime(String sql, Object... parameters) {
		return nextNode.getDatetime(sql, parameters);
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
	public HTMLFragment getHTML(String sql, String lang, String allowedTags) {
		return nextNode.getHTML(sql, lang, allowedTags);
	}

	@Override
	public HTMLFragment getHTML(String sql, String lang, String allowedTags, Object... parameters) {
		return nextNode.getHTML(sql, lang, allowedTags, parameters);
	}

	@Override
	public URI getURI(String sql) {
		return nextNode.getURI(sql);
	}

	@Override
	public URI getURI(String sql, Object... parameters) {
		return nextNode.getURI(sql, parameters);
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
	public Boolean getBoolean(String sql) {
		return nextNode.getBoolean(sql);
	}

	@Override
	public Boolean getBoolean(String sql, Object... parameters) {
		return nextNode.getBoolean(sql, parameters);
	}

	@Override
	public ZoneId getTimezone(String sql) {
		return nextNode.getTimezone(sql);
	}

	@Override
	public ZoneId getTimezone(String sql, Object... parameters) {
		return nextNode.getTimezone(sql, parameters);
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
	public ZonedDateTime getUTCDatetime(String sql) {
		return nextNode.getUTCDatetime(sql);
	}

	@Override
	public ZonedDateTime getUTCDatetime(String sql, Object... parameters) {
		return nextNode.getUTCDatetime(sql, parameters);
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
	public LocalDateTime[] getDatetimeArray(String sql) {
		return nextNode.getDatetimeArray(sql);
	}

	@Override
	public LocalDateTime[] getDatetimeArray(String sql, Object... parameters) {
		return nextNode.getDatetimeArray(sql, parameters);
	}

	@Override
	public ZonedDateTime[] getUTCDatetimeArray(String sql) {
		return nextNode.getUTCDatetimeArray(sql);
	}

	@Override
	public ZonedDateTime[] getUTCDatetimeArray(String sql, Object... parameters) {
		return nextNode.getUTCDatetimeArray(sql, parameters);
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
	public HTMLFragment[] getHTMLArray(String sql, String lang, String allowedTags) {
		return nextNode.getHTMLArray(sql, lang, allowedTags);
	}

	@Override
	public HTMLFragment[] getHTMLArray(String sql, String lang, String allowedTags, Object... parameters) {
		return nextNode.getHTMLArray(sql, lang, allowedTags, parameters);
	}

	@Override
	public URI[] getURIArray(String sql) {
		return nextNode.getURIArray(sql);
	}

	@Override
	public URI[] getURIArray(String sql, Object... parameters) {
		return nextNode.getURIArray(sql, parameters);
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
	public ZoneId[] getTimezoneArray(String sql) {
		return nextNode.getTimezoneArray(sql);
	}

	@Override
	public ZoneId[] getTimezoneArray(String sql, Object... parameters) {
		return nextNode.getTimezoneArray(sql, parameters);
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
	public Tuple getTuple(String sql) {
		return nextNode.getTuple(sql);
	}

	@Override
	public Tuple getTuple(String sql, Object... parameters) {
		return nextNode.getTuple(sql, parameters);
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
	public void commit() {
		nextNode.commit();
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
	public void close() {
		nextNode.close();
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