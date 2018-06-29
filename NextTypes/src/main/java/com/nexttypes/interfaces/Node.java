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

package com.nexttypes.interfaces;

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

public interface Node extends Module, AutoCloseable {

	public String[] getGroups(String user);

	public ZonedDateTime create(Type type);

	public ZonedDateTime addField(String type, String field, TypeField typeField);

	public ZonedDateTime addIndex(String type, String index, TypeIndex typeIndex);

	public AlterResult alter(Type type);

	public AlterResult alter(Type type, ZonedDateTime adate);

	public ZonedDateTime rename(String type, String newName);

	public AlterFieldResult alterField(String type, String field, TypeField typeField);

	public AlterIndexResult alterIndex(String type, String index, TypeIndex typeIndex);

	public ZonedDateTime renameField(String type, String field, String newName);

	public ZonedDateTime renameIndex(String type, String index, String newName);

	public ZonedDateTime insert(NXObject object);

	public ZonedDateTime update(NXObject object);

	public ZonedDateTime update(NXObject object, ZonedDateTime udate);

	public ZonedDateTime update(String type, String id, byte[] data);

	public ZonedDateTime updateId(String type, String id, String newId);

	public ZonedDateTime updateField(String type, String id, String field, Object value);

	public ZonedDateTime updatePassword(String type, String id, String field, String currentPassword,
			String newPassword, String newPasswordRepeat);

	public boolean checkPassword(String type, String id, String field, String password);

	public NXObject get(String type, String id, String[] fields, String lang, boolean fulltext,
			boolean binary, boolean documentPreview, boolean password);

	public Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit);

	public Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, Long offset, Long limit);

	public Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit);

	public Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, Long offset, Long limit);

	public Tuples select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters,
			String search, String[] searchFields, String order, Long offset, Long limit);

	public Tuple[] select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters,
			String order);

	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit);

	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter,
			String search, LinkedHashMap<String, Order> order, boolean fulltext, boolean binary,
			boolean documentPreview, boolean password, Long offset, Long limit);

	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit);

	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, boolean fulltext, boolean binary,
			boolean documentPreview, boolean password, Long offset, Long limit);

	public Type getType(String type);

	public LinkedHashMap<String, Type> getTypes(String[] types);

	public String[] getTypesName();

	public TypeInfo[] getTypesInfo();

	public Boolean existsType(String type);

	public Boolean existsObject(String type, String id);

	public String getName(String type, String id, String lang);

	public LinkedHashMap<String, String> getObjectsName(String type, String lang);

	public LinkedHashMap<String, ObjectInfo[]> getObjectsInfo(String[] types);

	public Reference[] getReferences();

	public TypeReference[] getDownReferences(String type);

	public TypeReference[] getUpReferences(String type);

	public Reference[] getUpReferences(String[] types);

	public TypeField getTypeField(String type, String field);

	public LinkedHashMap<String, TypeField> getTypeFields(String type, String... fields);

	public LinkedHashMap<String, TypeField> getTypeFields(String type);

	public TypeIndex getTypeIndex(String type, String index);

	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String type, String... indexes);

	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String type);

	public String getFieldType(String type, String field);

	public LinkedHashMap<String, TypeField> getActionFields(String type, String action);

	public LinkedHashMap<String, LinkedHashMap<String, TypeField>> getTypeActions(String type);

	public Tuple getFieldsSize(String type, String id);

	public String getFieldContentType(String type, String field);

	public String getFieldContentType(String type, String id, String field);

	public String getCompositeFieldContentType(String type, String id, String field);

	public LinkedHashMap<String, String> getFieldsContentType(String type);

	public LinkedHashMap<String, FieldInfo> getFieldsInfo(String type, String id);

	public void drop(String... types);

	public ZonedDateTime dropField(String type, String field);

	public ZonedDateTime dropIndex(String type, String index);

	public void delete(String type, String... objects);

	public Object getField(String type, String id, String field);

	public String getStringField(String type, String id, String field);

	public byte[] getBinaryField(String type, String id, String field);

	public Image getImageField(String type, String id, String field);

	public HTMLFragment getHTMLField(String type, String id, String field);

	public byte[] getImageContent(String type, String id, String field);

	public byte[] getImageThumbnail(String type, String id, String field);

	public String getImageContentType(String type, String id, String field);

	public String getDocumentContentType(String type, String id, String field);

	public XML getXMLField(String type, String id, String field);

	public Element getHTMLElement(String type, String id, String field, String element);

	public Element getXMLElement(String type, String id, String field, String element);

	public Document getDocumentField(String type, String id, String field);

	public String getPasswordField(String type, String id, String field);

	public ObjectField getObjectField(String type, String id, String field);

	public ZonedDateTime getADate(String type);

	public ZonedDateTime getUDate(String type, String id);

	public String getETag(String type, String id);

	public ActionResult executeAction(String type, String id, String action, Object... parameters);

	public ActionResult executeAction(String type, String[] objects, String action, Object... parameters);

	public Long count(String type);

	public Long count(String sql, Object... parameters);

	public boolean hasObjects(String type);

	public boolean hasNullValues(String type, String field);

	public int execute(String sql);

	public int execute(String sql, Object... parameters);

	public int execute(String sql, Integer expectedRows, Object... parameters);

	public int execute(String sql, boolean useSavepoint, Integer expectedRows, Object... parameters);

	public TypesStream exportTypes(String[] types, String lang, boolean includeObjects);

	public TypesStream exportTypes(String[] types, String lang, Filter filter, boolean includeObjects);

	public TypesStream exportTypes(String[] types, String lang, Filter[] filters, boolean includeObjects);

	public TypesStream backup(String lang, boolean full);

	public ObjectsStream exportObjects(String type, String[] objects, String lang,
			LinkedHashMap<String, Order> order);

	public ImportTypesResult importTypes(InputStream types, ImportAction existingTypesAction,
			ImportAction existingObjectsAction);

	public ImportTypesResult importTypes(TypesStream types, ImportAction existingTypesAction,
			ImportAction existingObjectsAction);

	public ImportObjectsResult importObjects(InputStream objects, ImportAction existingObjectsAction);

	public ImportObjectsResult importObjects(ObjectsStream objects, ImportAction existingObjectsAction);

	public Short getInt16(String sql);

	public Short getInt16(String sql, Object... parameters);

	public Integer getInt32(String sql);

	public Integer getInt32(String sql, Object... parameters);

	public Long getInt64(String sql);

	public Long getInt64(String sql, Object... parameters);

	public Float getFloat32(String sql);

	public Float getFloat32(String sql, Object... parameters);

	public Double getFloat64(String sql);

	public Double getFloat64(String sql, Object... parameters);

	public BigDecimal getNumeric(String sql);

	public BigDecimal getNumeric(String sql, Object... parameters);

	public String getString(String sql);

	public String getString(String sql, Object... parameters);

	public String getText(String sql);

	public String getText(String sql, Object... parameters);

	public LocalDate getDate(String sql);

	public LocalDate getDate(String sql, Object... parameters);

	public LocalTime getTime(String sql);

	public LocalTime getTime(String sql, Object... parameters);

	public LocalDateTime getDatetime(String sql);

	public LocalDateTime getDatetime(String sql, Object... parameters);

	public byte[] getBinary(String sql);

	public byte[] getBinary(String sql, Object... parameters);

	public HTMLFragment getHTML(String sql, String lang, String allowedTags);

	public HTMLFragment getHTML(String sql, String lang, String allowedTags, Object... parameters);

	public URI getURI(String sql);

	public URI getURI(String sql, Object... parameters);

	public InternetAddress getEmail(String sql);

	public InternetAddress getEmail(String sql, Object... parameters);

	public String getTel(String sql);

	public String getTel(String sql, Object... parameters);

	public Boolean getBoolean(String sql);

	public Boolean getBoolean(String sql, Object... parameters);

	public ZoneId getTimezone(String sql);

	public ZoneId getTimezone(String sql, Object... parameters);

	public Color getColor(String sql);

	public Color getColor(String sql, Object... parameters);

	public Image getImage(String sql);

	public Image getImage(String sql, Object... parameters);

	public Document getDocument(String sql);

	public Document getDocument(String sql, Object... parameters);

	public ZonedDateTime getUTCDatetime(String sql);

	public ZonedDateTime getUTCDatetime(String sql, Object... parameters);

	public Object getObject(String sql);

	public Object getObject(String sql, Object... parameters);

	public Short[] getInt16Array(String sql);

	public Short[] getInt16Array(String sql, Object... parameters);

	public Integer[] getInt32Array(String sql);

	public Integer[] getInt32Array(String sql, Object... parameters);

	public Long[] getInt64Array(String sql);

	public Long[] getInt64Array(String sql, Object... parameters);

	public Float[] getFloat32Array(String sql);

	public Float[] getFloat32Array(String sql, Object... parameters);

	public Double[] getFloat64Array(String sql);

	public Double[] getFloat64Array(String sql, Object... parameters);

	public BigDecimal[] getNumericArray(String sql);

	public BigDecimal[] getNumericArray(String sql, Object... parameters);

	public Boolean[] getBooleanArray(String sql);

	public Boolean[] getBooleanArray(String sql, Object... parameters);

	public String[] getStringArray(String sql);

	public String[] getStringArray(String sql, Object... parameters);

	public String[] getTextArray(String sql);

	public String[] getTextArray(String sql, Object... parameters);

	public LocalDate[] getDateArray(String sql);

	public LocalDate[] getDateArray(String sql, Object... parameters);

	public LocalTime[] getTimeArray(String sql);

	public LocalTime[] getTimeArray(String sql, Object... parameters);

	public LocalDateTime[] getDatetimeArray(String sql);

	public LocalDateTime[] getDatetimeArray(String sql, Object... parameters);

	public ZonedDateTime[] getUTCDatetimeArray(String sql);

	public ZonedDateTime[] getUTCDatetimeArray(String sql, Object... parameters);

	public byte[][] getBinaryArray(String sql);

	public byte[][] getBinaryArray(String sql, Object... parameters);

	public HTMLFragment[] getHTMLArray(String sql, String lang, String allowedTags);

	public HTMLFragment[] getHTMLArray(String sql, String lang, String allowedTags, Object... parameters);

	public URI[] getURIArray(String sql);

	public URI[] getURIArray(String sql, Object... parameters);

	public InternetAddress[] getEmailArray(String sql);

	public InternetAddress[] getEmailArray(String sql, Object... parameters);

	public String[] getTelArray(String sql);

	public String[] getTelArray(String sql, Object... parameters);

	public ZoneId[] getTimezoneArray(String sql);

	public ZoneId[] getTimezoneArray(String sql, Object... parameters);

	public Color[] getColorArray(String sql);

	public Color[] getColorArray(String sql, Object... parameters);

	public Image[] getImageArray(String sql);

	public Image[] getImageArray(String sql, Object... parameters);

	public Document[] getDocumentArray(String sql);

	public Document[] getDocumentArray(String sql, Object... parameters);

	public <T> T[] getArray(String sql, Class<T> type);

	public <T> T[] getArray(String sql, Class<T> type, Object... parameters);

	public Tuple getTuple(String sql);

	public Tuple getTuple(String sql, Object... parameters);

	public Tuple getMatrix(String sql, String[] axes);

	public Tuple getMatrix(String sql, String[] axes, Object... parameters);

	public Tuple[] query(String sql);

	public Tuple[] query(String sql, Object... parameters);

	public <T> T[] query(String sql, Class<T> type);

	public <T> T[] query(String sql, Class<T> type, Object... parameters);

	public void commit();

	public Savepoint setSavepoint();

	public void rollback();

	public void rollback(Savepoint savepoint);

	public void setDeferredConstraints(boolean status);

	public void close();
}