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
import java.util.TreeMap;

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
import com.nexttypes.datatypes.Matrix;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.Names;
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
import com.nexttypes.enums.Order;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.interfaces.TypesStream;
import com.nexttypes.system.Context;
import com.nexttypes.system.Module;

public abstract class Node extends Module {

	public static void init(Context context) {};
	
	public abstract String getVersion();
	
	public abstract String[] getGroups(String user);

	public abstract ZonedDateTime create(Type type);

	public abstract ZonedDateTime addField(String type, String field, TypeField typeField);

	public abstract ZonedDateTime addIndex(String type, String index, TypeIndex typeIndex);

	public abstract AlterResult alter(Type type);

	public abstract AlterResult alter(Type type, ZonedDateTime adate);

	public abstract ZonedDateTime rename(String type, String newName);

	public abstract AlterFieldResult alterField(String type, String field, TypeField typeField);

	public abstract AlterIndexResult alterIndex(String type, String index, TypeIndex typeIndex);

	public abstract ZonedDateTime renameField(String type, String field, String newName);

	public abstract ZonedDateTime renameIndex(String type, String index, String newName);

	public abstract ZonedDateTime insert(NXObject object);

	public abstract ZonedDateTime update(NXObject object);

	public abstract ZonedDateTime update(NXObject object, ZonedDateTime udate);

	public abstract ZonedDateTime update(String type, String id, byte[] data);

	public abstract ZonedDateTime updateId(String type, String id, String newId);

	public abstract ZonedDateTime updateField(String type, String id, String field, Object value);

	public abstract ZonedDateTime updatePassword(String type, String id, String field, String currentPassword,
			String newPassword, String newPasswordRepeat);

	public abstract boolean checkPassword(String type, String id, String field, String password);

	public abstract NXObject get(String type, String id, String[] fields, String lang, boolean fulltext,
			boolean binary, boolean documentPreview, boolean password, boolean objectName, 
			boolean referencesName);

	public abstract Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit);

	public abstract Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit);

	public abstract Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit);

	public abstract Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit);

	public abstract Tuples select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters,
			String search, String[] searchFields, String order, Long offset, Long limit);

	public abstract Tuple[] select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters,
			String order);

	public abstract ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit);

	public abstract ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter,
			String search, LinkedHashMap<String, Order> order, boolean fulltext, boolean binary,
			boolean documentPreview, boolean password, boolean objectsName, boolean referencesName,
			Long offset, Long limit);

	public abstract ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit);

	public abstract ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, boolean fulltext, boolean binary,
			boolean documentPreview, boolean password, boolean objectsName, boolean referencesName,
			Long offset, Long limit);

	public abstract Type getType(String type);

	public abstract LinkedHashMap<String, Type> getTypes(String[] types);

	public abstract String[] getTypesName();

	public abstract TypeInfo[] getTypesInfo();
	
	public abstract TreeMap<String, TypeInfo> getTypesInfoOrderByName();

	public abstract Boolean existsType(String type);

	public abstract Boolean existsObject(String type, String id);

	public abstract String getName(String type, String id, String lang);

	public abstract Names getNames(String type, String lang);
	
	public abstract Names getNames(String type, String lang, String search, Long offset, 
			Long limit);
	
	public abstract Names getNames(String type, String sql, Object[] parameters, String lang,
			String search, Long offset, Long limit);
	
	public abstract Names getNames(String type, StringBuilder sql, 
			ArrayList<Object> parameters, String lang, String search, Long offset, Long limit);
	
	public abstract Names getNames(String referencedType, String referencingType, 
			String referencingAction, String referencingField, String lang);
	
	public abstract Names getNames(String referencedType, String referencingType,
			String referencingAction, String referencingField, String lang, String search,
			Long offset, Long limit);

	public abstract LinkedHashMap<String, ObjectInfo[]> getObjectsInfo(String[] types);

	public abstract Reference[] getReferences();
	
	public abstract TreeMap<String, TreeMap<String, TreeMap<String, Reference>>>
		getReferencesOrderByNames();

	public abstract TypeReference[] getDownReferences(String type);

	public abstract TypeReference[] getUpReferences(String type);

	public abstract Reference[] getUpReferences(String[] types);

	public abstract TypeField getTypeField(String type, String field);
	
	public abstract LinkedHashMap<String, TypeField> getTypeFields(String type);

	public abstract LinkedHashMap<String, TypeField> getTypeFields(String type, String... fields);

	public abstract TypeIndex getTypeIndex(String type, String index);
	
	public abstract LinkedHashMap<String, TypeIndex> getTypeIndexes(String type);

	public abstract LinkedHashMap<String, TypeIndex> getTypeIndexes(String type, String... indexes);

	public abstract String getFieldType(String type, String field);
	
	public abstract String getActionFieldType(String type, String action, String field);
	
	public abstract TypeField getActionField(String type, String action, String field);

	public abstract LinkedHashMap<String, TypeField> getActionFields(String type, String action);

	public abstract LinkedHashMap<String, LinkedHashMap<String, TypeField>> getTypeActions(String type);

	public abstract Tuple getFieldsSize(String type, String id);

	public abstract String getFieldContentType(String type, String field);

	public abstract String getFieldContentType(String type, String id, String field);
	
	public abstract Object getFieldDefault(String type, String field);
	
	public abstract FieldRange getFieldRange(String type, String field);
	
	public abstract FieldRange getActionFieldRange(String type, String action, String field);
	
	public abstract String getCompositeFieldContentType(String type, String id, String field);

	public abstract LinkedHashMap<String, String> getFieldsContentType(String type);

	public abstract LinkedHashMap<String, FieldInfo> getFieldsInfo(String type, String id);
	
	public abstract void drop(String... types);

	public abstract ZonedDateTime dropField(String type, String field);

	public abstract ZonedDateTime dropIndex(String type, String index);

	public abstract void delete(String type, String... objects);

	public abstract Object getField(String type, String id, String field);

	public abstract String getStringField(String type, String id, String field);

	public abstract byte[] getBinaryField(String type, String id, String field);

	public abstract Image getImageField(String type, String id, String field);

	public abstract HTMLFragment getHTMLField(String type, String id, String field);

	public abstract byte[] getImageContent(String type, String id, String field);

	public abstract byte[] getImageThumbnail(String type, String id, String field);

	public abstract String getImageContentType(String type, String id, String field);

	public abstract String getDocumentContentType(String type, String id, String field);

	public abstract XML getXMLField(String type, String id, String field);

	public abstract Element getHTMLElement(String type, String id, String field, String element);

	public abstract Element getXMLElement(String type, String id, String field, String element);

	public abstract Document getDocumentField(String type, String id, String field);

	public abstract String getPasswordField(String type, String id, String field);

	public abstract ObjectField getObjectField(String type, String id, String field);

	public abstract ZonedDateTime getADate(String type);

	public abstract ZonedDateTime getUDate(String type, String id);

	public abstract String getETag(String type, String id);

	public abstract ActionResult executeAction(String type, String id, String action, Object... parameters);

	public abstract ActionResult executeAction(String type, String[] objects, String action, Object... parameters);

	public abstract Long count(String type);

	public abstract Long count(String sql, Object... parameters);

	public abstract boolean hasObjects(String type);

	public abstract boolean hasNullValues(String type, String field);

	public abstract int execute(String sql, Object... parameters);

	public abstract int execute(String sql, Integer expectedRows, Object... parameters);

	public abstract int execute(String sql, boolean useSavepoint, Integer expectedRows, Object... parameters);

	public abstract TypesStream exportTypes(String[] types, boolean includeObjects);

	public abstract TypesStream exportTypes(String[] types, Filter filter, boolean includeObjects);

	public abstract TypesStream exportTypes(String[] types, Filter[] filters, boolean includeObjects);

	public abstract TypesStream backup(boolean full);

	public abstract ObjectsStream exportObjects(String type, String[] objects, LinkedHashMap<String, Order> order);

	public abstract ImportTypesResult importTypes(InputStream types, ImportAction existingTypesAction,
			ImportAction existingObjectsAction);

	public abstract ImportTypesResult importTypes(TypesStream types, ImportAction existingTypesAction,
			ImportAction existingObjectsAction);

	public abstract ImportObjectsResult importObjects(InputStream objects, ImportAction existingObjectsAction);

	public abstract ImportObjectsResult importObjects(ObjectsStream objects, ImportAction existingObjectsAction);

	public abstract Short getInt16(String sql, Object... parameters);

	public abstract Integer getInt32(String sql, Object... parameters);

	public abstract Long getInt64(String sql, Object... parameters);

	public abstract Float getFloat32(String sql, Object... parameters);

	public abstract Double getFloat64(String sql, Object... parameters);

	public abstract BigDecimal getNumeric(String sql, Object... parameters);

	public abstract String getString(String sql, Object... parameters);

	public abstract String getText(String sql, Object... parameters);

	public abstract LocalDate getDate(String sql, Object... parameters);

	public abstract LocalTime getTime(String sql, Object... parameters);

	public abstract LocalDateTime getDateTime(String sql, Object... parameters);

	public abstract byte[] getBinary(String sql, Object... parameters);

	public abstract HTMLFragment getHTML(String sql, String allowedTags, Object... parameters);

	public abstract URL getURL(String sql, Object... parameters);

	public abstract InternetAddress getEmail(String sql, Object... parameters);

	public abstract String getTel(String sql, Object... parameters);

	public abstract Boolean getBoolean(String sql, Object... parameters);

	public abstract ZoneId getTimeZone(String sql, Object... parameters);

	public abstract Color getColor(String sql, Object... parameters);

	public abstract Image getImage(String sql, Object... parameters);

	public abstract Document getDocument(String sql, Object... parameters);

	public abstract ZonedDateTime getUTCDateTime(String sql, Object... parameters);

	public abstract Object getObject(String sql, Object... parameters);

	public abstract Short[] getInt16Array(String sql, Object... parameters);

	public abstract Integer[] getInt32Array(String sql, Object... parameters);

	public abstract Long[] getInt64Array(String sql, Object... parameters);

	public abstract Float[] getFloat32Array(String sql, Object... parameters);

	public abstract Double[] getFloat64Array(String sql, Object... parameters);

	public abstract BigDecimal[] getNumericArray(String sql, Object... parameters);

	public abstract Boolean[] getBooleanArray(String sql, Object... parameters);

	public abstract String[] getStringArray(String sql, Object... parameters);

	public abstract String[] getTextArray(String sql, Object... parameters);

	public abstract LocalDate[] getDateArray(String sql, Object... parameters);

	public abstract LocalTime[] getTimeArray(String sql, Object... parameters);

	public abstract LocalDateTime[] getDateTimeArray(String sql, Object... parameters);

	public abstract ZonedDateTime[] getUTCDateTimeArray(String sql, Object... parameters);

	public abstract byte[][] getBinaryArray(String sql, Object... parameters);

	public abstract HTMLFragment[] getHTMLArray(String sql, String allowedTags, Object... parameters);

	public abstract URL[] getURLArray(String sql, Object... parameters);

	public abstract InternetAddress[] getEmailArray(String sql, Object... parameters);

	public abstract String[] getTelArray(String sql, Object... parameters);

	public abstract ZoneId[] getTimeZoneArray(String sql, Object... parameters);

	public abstract Color[] getColorArray(String sql, Object... parameters);

	public abstract Image[] getImageArray(String sql, Object... parameters);

	public abstract Document[] getDocumentArray(String sql, Object... parameters);

	public abstract <T> T[] getArray(String sql, Class<T> type, Object... parameters);

	public abstract Tuple getTuple(String sql, Object... parameters);

	public abstract Matrix getMatrix(String sql, String[] axes, Object... parameters);

	public abstract Tuple[] query(String sql, Object... parameters);

	public abstract <T> T[] query(String sql, Class<T> type, Object... parameters);

	public abstract void commit();

	public abstract Savepoint setSavepoint();

	public abstract void rollback();

	public abstract void rollback(Savepoint savepoint);

	public abstract void setDeferredConstraints(boolean status);

	public abstract void close();
}