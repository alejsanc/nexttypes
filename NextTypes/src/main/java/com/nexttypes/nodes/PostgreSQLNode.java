/*
 * Copyright 2015-2020 Alejandro Sánchez <alex@nexttypes.com>
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
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.PGConnection;
import org.postgresql.jdbc.PgArray;
import org.postgresql.jdbc.PgSQLXML;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

import com.nexttypes.datatypes.ActionResult;
import com.nexttypes.datatypes.AlterFieldResult;
import com.nexttypes.datatypes.AlterIndexResult;
import com.nexttypes.datatypes.AlterResult;
import com.nexttypes.datatypes.Audio;
import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Color;
import com.nexttypes.datatypes.Document;
import com.nexttypes.datatypes.DocumentPreview;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.FieldInfo;
import com.nexttypes.datatypes.FieldRange;
import com.nexttypes.datatypes.File;
import com.nexttypes.datatypes.HTMLFragment;
import com.nexttypes.datatypes.IdFilter;
import com.nexttypes.datatypes.Image;
import com.nexttypes.datatypes.ImportObjectsResult;
import com.nexttypes.datatypes.ImportTypesResult;
import com.nexttypes.datatypes.JSON;
import com.nexttypes.datatypes.Matrix;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.Names;
import com.nexttypes.datatypes.ObjectField;
import com.nexttypes.datatypes.ObjectInfo;
import com.nexttypes.datatypes.ObjectReference;
import com.nexttypes.datatypes.Objects;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.Reference;
import com.nexttypes.datatypes.Serial;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.Tuples;
import com.nexttypes.datatypes.Type;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.datatypes.TypeInfo;
import com.nexttypes.datatypes.TypeReference;
import com.nexttypes.datatypes.URL;
import com.nexttypes.datatypes.UpdateIdResult;
import com.nexttypes.datatypes.Video;
import com.nexttypes.datatypes.XML;
import com.nexttypes.datatypes.XML.Element;
import com.nexttypes.enums.Comparison;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.ImportAction;
import com.nexttypes.enums.IndexMode;
import com.nexttypes.enums.NodeMode;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.FieldException;
import com.nexttypes.exceptions.FieldNotFoundException;
import com.nexttypes.exceptions.FulltextIndexNotFoundException;
import com.nexttypes.exceptions.IndexException;
import com.nexttypes.exceptions.IndexNotFoundException;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.exceptions.NotImplementedException;
import com.nexttypes.exceptions.ObjectException;
import com.nexttypes.exceptions.ObjectNotFoundException;
import com.nexttypes.exceptions.StringException;
import com.nexttypes.exceptions.TypeException;
import com.nexttypes.exceptions.TypeNotFoundException;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.interfaces.TuplesStream;
import com.nexttypes.interfaces.TypesStream;
import com.nexttypes.logging.Logger;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.security.Checks;
import com.nexttypes.security.Security;
import com.nexttypes.serialization.ObjectsStreamDeserializer;
import com.nexttypes.serialization.TypesStreamDeserializer;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Context;
import com.nexttypes.system.Context.TypesCache;
import com.nexttypes.system.DBConnection;
import com.nexttypes.system.Utils;

public class PostgreSQLNode extends Node {
	public static final String POSTGRESQL = "postgresql";
	public static final String DRIVER = "org.postgresql.Driver";

	protected static final String FILE_TYPE =
			"create type file as ("
					+ "content bytea,"
					+ "content_type character varying(255)"
			+ ")";
	
	protected static final String IMAGE_TYPE =
			"create type image as ("
					+ "content bytea,"
					+ "thumbnail bytea,"
					+ "content_type character varying(255)"
			+ ")";

	protected static final String DOCUMENT_TYPE =
			"create type document as ("
					+ "content bytea,"
					+ "text text,"
					+ "content_type character varying(255)"
			+ ")";

	protected static final String AUDIO_TYPE =
			"create type audio as ("
					+ "content bytea,"
					+ "content_type character varying(255)"
			+ ")";

	protected static final String VIDEO_TYPE =
			"create type video as ("
					+ "content bytea,"
					+ "content_type character varying(255)"
			+ ")";

	protected static final String FULLTEXT_SEARCH_FIELD_SEPARATOR = " || ' ' || ";

	protected static final String UUID_FUNCTION = "uuid_generate_v4()";

	protected static final String GROUPS_QUERY = "select \"group\" from group_user where \"user\" = ?";

	protected static final String GET_TYPES_NAME_QUERY = "select table_name from information_schema.tables"
			+ " where table_type = 'BASE TABLE' and table_schema = 'public' order by table_name";

	protected static final String GET_TYPES_SIZE_QUERY =

			"select"
					+ " table_name as name,"
					+ " pg_total_relation_size(table_name) as size"

			+ " from"
					+ " information_schema.tables"

			+ " where"
					+ " table_type = 'BASE TABLE'"
					+ " and table_schema = 'public'"

			+ " order by table_name";

	protected static final String EXISTS_TYPE_QUERY = "select exists (select 1 from information_schema.tables"
			+ " where table_schema = 'public' and table_name = ?)";

	protected static final String REFERENCES = " from" + " pg_constraint c"
			+ " join pg_class referenced on c.confrelid = referenced.oid"
			+ " join pg_class referencing on c.conrelid = referencing.oid"
			+ " join pg_attribute a on c.conkey[1] = a.attnum and referencing.oid = a.attrelid";

	protected static final String GET_REFERENCES_QUERY =
			"select"
					+ " referenced.relname as referenced_type,"
					+ " referencing.relname as referencing_type,"
					+ " a.attname as referencing_field"
					+ REFERENCES;

	protected static final String GET_UP_REFERENCES_QUERY =
			"select"
					+ " referenced.relname as type,"
					+ " a.attname as field" + REFERENCES

			+ " where"
				+ " referencing.relname = ?";

	protected static final String GET_DOWN_REFERENCES_QUERY = 
			"select"
					+ " referencing.relname as type,"
					+ " a.attname as field"
					+ REFERENCES
			+ " where"
					+ " referenced.relname = ?";

	protected static final String GET_TYPE_FIELDS_QUERY =
			"select"
					+ " de.description as type,"
					+ " co.column_name as name,"
					+ " co.character_maximum_length as length,"
					+ " co.numeric_precision as precision,"
					+ " co.numeric_scale as scale,"
					+ " case"
						+ " when co.is_nullable = 'NO' then true"
						+ " else false"
					+ " end as not_null"

			+ " from"
				+ " information_schema.columns co join pg_class cl on co.table_name = cl.relname"
				+ " join pg_description de on (cl.oid = de.objoid and co.ordinal_position = de.objsubid)"

			+ " where"
				+ " co.table_name = ? and co.column_name not in('id','cdate','udate','backup')";

	protected static final String GET_TYPE_INDEXES_QUERY =
			"select"
					+ " ic.relname as name,"
					+ " case"
						+ " when i.indkey = '0' then 'fulltext'"
						+ " when i.indisunique is true then 'unique'"
						+ " else 'index'"
					+ " end as mode,"
					+ " array(select attname from pg_attribute a where a.attrelid = tc.oid and a.attnum = any("
						+ " case"
							+ " when i.indkey = '0' then"
							+ " cast(array(select array_to_string(regexp_matches(i.indexprs, ':varattno (.)', 'g'),'')) as int[])"
							+ " else i.indkey"
						+ " end" + " ))::text[] as fields"

			+ " from"
				+ " pg_index i"
				+ " join pg_class ic on i.indexrelid = ic.oid"
				+ " join pg_class tc on i.indrelid = tc.oid"
				+ " join pg_namespace ns on tc.relnamespace = ns.oid"

			+ " where"
				+ " i.indisprimary is false"
				+ " and ns.nspname = 'public'"
				+ " and tc.relname = ?"

			+ " order by name";

	protected static final String GET_TYPE_DATES_QUERY = "select unnest(string_to_array(obj_description(?::regclass, 'pg_class'), '|'))";

	protected DBConnection.DBConnectionPool connectionPool;
	protected TypesCache cache;
	protected boolean cacheEnabled = true;

	protected Auth auth;
	protected String lang;
	
	protected Connection connection;
	protected PGConnection pgConnection;
	protected Settings settings;
	protected TypeSettings typeSettings;
	protected Strings strings;
	protected String remoteAddress;
	protected Context context;
	protected Logger logger;
	
	public PostgreSQLNode(Context context) {
		
		settings = context.getSettings(Settings.POSTGRESQL_SETTINGS);
		lang = settings.getString(KeyWords.DEFAULT_LANG);
		connectionPool = DBConnection.getConnectionPool(settings, POSTGRESQL, DRIVER);
		context.putDBConnectionPool(settings.getString(KeyWords.POOL), connectionPool);
		
		try (PostgreSQLNode node = new PostgreSQLNode(new Auth(Auth.ADMIN, Auth.ADMINISTRATORS),
				NodeMode.ADMIN, lang, URL.LOCALHOST, context, true)) {

			if (node.getTypesName().length == 0) {
				node.importTypes(node.getClass()
						.getResourceAsStream("/com/nexttypes/system/system-types.json"), 
						ImportAction.ABORT, ImportAction.ABORT);

				node.execute(FILE_TYPE);
				node.execute(IMAGE_TYPE);
				node.execute(DOCUMENT_TYPE);
				node.execute(AUDIO_TYPE);
				node.execute(VIDEO_TYPE);
			}
			
			node.commit();
		}
	}

	public PostgreSQLNode(HTTPRequest request, NodeMode mode) {
		this(request.getAuth(), mode, request.getLang(), request.getRemoteAddress(),
				request.getContext(), true);
	}

	public PostgreSQLNode(Auth auth, NodeMode mode, String lang, String remoteAddress,
			Context context, boolean useConnectionPool) {
		this.auth = auth;
		this.remoteAddress = remoteAddress;
		this.context = context;

		settings = context.getSettings(Settings.POSTGRESQL_SETTINGS);
		typeSettings = context.getTypeSettings(auth);
		
		if (lang == null) {
			lang = settings.getString(KeyWords.DEFAULT_LANG);
		} 

		this.lang = lang;
				
		strings = context.getStrings(lang);

		if (useConnectionPool) {
			connectionPool = context.getDatabaseConnectionPool(settings.getString(KeyWords.POOL));
			connection = connectionPool.getConnection(mode);
		} else {
			connection = DBConnection.getConnection(settings, POSTGRESQL, mode);
		}

		try {
			pgConnection = connection.unwrap(PGConnection.class);
			pgConnection.setPrepareThreshold(0);
			pgConnection.addDataType(PT.FILE,  File.class);
			pgConnection.addDataType(PT.IMAGE, Image.class);
			pgConnection.addDataType(PT.DOCUMENT, Document.class);
			pgConnection.addDataType(PT.AUDIO, Audio.class);
			pgConnection.addDataType(PT.VIDEO, Video.class);
		} catch (SQLException e) {
			throwException(e);
		}

		cache = context.getTypesCache();

		logger = context.getLogger();
	}
	
	@Override
	public String getVersion() {
		return Constants.VERSION;
	}

	@Override
	public String[] getGroups(String user) {
		return getStringArray(GROUPS_QUERY, user);
	}

	@Override
	public LinkedHashMap<String, FieldInfo> getFieldsInfo(String type, String id) {
		LinkedHashMap<String, FieldInfo> fields = new LinkedHashMap<>();

		Tuple fieldsSize = getFieldsSize(type, id);
		LinkedHashMap<String, String> fieldsContentType = getFieldsContentType(type);

		for (Map.Entry<String, String> entry : fieldsContentType.entrySet()) {
			String field = entry.getKey();
			String contentType = entry.getValue();

			if (contentType == null) {
				String fieldType = getFieldType(type, field);

				switch (fieldType) {
				case PT.FILE:
				case PT.IMAGE:
				case PT.DOCUMENT:
				case PT.AUDIO:
				case PT.VIDEO:
					contentType = getCompositeFieldContentType(type, id, field);
				}
			}

			fields.put(field, new FieldInfo(fieldsSize.getInt32(field), contentType));
		}

		return fields;
	}

	@Override
	public Tuple getFieldsSize(String type, String id) {
		LinkedHashMap<String, TypeField> fields = getTypeFields(type);
		StringBuilder sql = new StringBuilder("select ");
		ArrayList<Object> parameters = new ArrayList<>();

		for (Map.Entry<String, TypeField> entry : fields.entrySet()) {
			String field = entry.getKey();
			TypeField typeField = entry.getValue();
			String fieldType = typeField.getType();

			switch (fieldType) {
			case PT.BOOLEAN:
				sql.append("1 as \"" + field + "\",");
				break;
			case PT.INT16:
				sql.append("2 as \"" + field + "\",");
				break;
			case PT.INT32:
			case PT.FLOAT32:
			case PT.DATE:
				sql.append("4 as \"" + field + "\",");
				break;
			case PT.INT64:
			case PT.FLOAT64:
			case PT.TIME:
			case PT.DATETIME:
				sql.append("8 as \"" + field + "\",");
				break;
			case PT.FILE:
			case PT.IMAGE:
			case PT.DOCUMENT:
			case PT.AUDIO:
			case PT.VIDEO:
				sql.append("octet_length((\"" + field + "\").content) as \"" + field + "\",");
				break;
			case PT.NUMERIC:
			case PT.XML:
			case PT.JSON:
				sql.append("octet_length(\"" + field + "\"::text) as \"" + field + "\",");
				break;
			default:
				sql.append("octet_length(\"" + field + "\") as \"" + field + "\",");
			}
		}

		sql.deleteCharAt(sql.length() - 1).append(" from \"" + type + "\" where id=?");
		parameters.add(id);

		return getTuple(sql, parameters);
	}

	@Override
	public ZonedDateTime create(Type type) {
		return create(type, true);
	}

	protected ZonedDateTime create(Type type, boolean single) {
		cacheEnabled = false;

		String typeName = type.getName();

		checkType(typeName);

		if (single && existsType(typeName)) {
			throw new TypeException(typeName, KeyWords.TYPE_ALREADY_EXISTS);
		}

		StringBuilder sql = new StringBuilder("create table \"" + typeName + "\"" + " (id character varying("
				+ Type.MAX_ID_LENGTH + ") not null primary key,"
				+ " cdate timestamp not null, udate timestamp not null," + " backup boolean not null");

		for (Map.Entry<String, TypeField> entry : type.getFields().entrySet()) {
			String field = entry.getKey();

			checkField(typeName, field);

			TypeField typeField = entry.getValue();
			String fieldType = typeField.getType();
			String sqlParameters = sqlParameters(typeField.getParameters());

			sql.append(", \"" + field + "\" ");

			sql.append(sqlType(fieldType, sqlParameters));

			if (typeField.isNotNull()) {
				sql.append(" not null");

			}

			if (single && !PT.isPrimitiveType(fieldType)) {
				sql.append(", constraint " + typeName + "_" + field + " foreign key (\"" + field + "\")"
						+ " references \"" + fieldType + "\"(id) on update cascade deferrable initially immediate");
			}
		}

		sql.append(")");

		execute(sql);

		String read_user = settings.getString(NodeMode.READ + "_" + KeyWords.USER);
		String write_user = settings.getString(NodeMode.WRITE + "_" + KeyWords.USER);
		execute("grant select on \"" + typeName + "\" to " + read_user);
		execute("grant select, insert, update, delete on \"" + typeName + "\" to " + write_user);

		for (Map.Entry<String, TypeField> entry : type.getFields().entrySet()) {
			String field = entry.getKey();
			TypeField typeField = entry.getValue();
			String fieldType = typeField.getType();

			setFieldType(typeName, field, fieldType);
		}

		for (Map.Entry<String, TypeIndex> entry : type.getIndexes().entrySet()) {
			String index = entry.getKey();

			checkIndex(typeName, index);

			addIndex(typeName, index, entry.getValue(), false);
		}

		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

		ZonedDateTime cdate = type.getCDate();
		if (cdate == null) {
			cdate = now;
		}

		ZonedDateTime adate = type.getADate();
		if (adate == null) {
			adate = now;
		}

		setTypeDates(typeName, cdate, adate);

		return adate;
	}

	@Override
	public ZonedDateTime addField(String type, String field, TypeField typeField) {
		return addField(type, field, typeField, true, true);
	}

	protected ZonedDateTime addField(String type, String field, TypeField typeField, boolean addReferences,
			boolean single) {
		cacheEnabled = false;

		ZonedDateTime adate = null;

		String fieldType = typeField.getType();

		checkNewFieldNullability(type, field, typeField.isNotNull());

		StringBuilder sql = new StringBuilder("alter table \"" + type + "\" add column \"" + field + "\" ");
		sql.append(sqlType(fieldType, sqlParameters(typeField.getParameters())));

		if (typeField.isNotNull()) {
			sql.append(" not null");
		}

		execute(sql);

		setFieldType(type, field, fieldType);

		if (addReferences && !PT.isPrimitiveType(fieldType)) {
			addReference(type, field, fieldType);
		}

		if (single) {
			adate = updateTypeDates(type);
		}

		return adate;
	}

	@Override
	public ZonedDateTime addIndex(String type, String index, TypeIndex typeIndex) {
		return addIndex(type, index, typeIndex, true);
	}

	protected ZonedDateTime addIndex(String type, String index, TypeIndex typeIndex, boolean single) {
		cacheEnabled = false;

		ZonedDateTime adate = null;

		String[] fields = typeIndex.getFields();
		checkIndexFieldsList(type, index, fields);

		StringBuilder sql = new StringBuilder("create ");

		IndexMode mode = typeIndex.getMode();
		String indexType = null, beforeFields = null, afterFields = null, fieldSeparator = null;

		switch (mode) {
		case INDEX:
			indexType = "index";
			beforeFields = "(";
			fieldSeparator = ",";
			afterFields = ")";
			break;
		case FULLTEXT:
			indexType = "index";
			beforeFields = "using gin(to_tsvector('simple',";
			fieldSeparator = FULLTEXT_SEARCH_FIELD_SEPARATOR;
			afterFields = "))";
			break;
		case UNIQUE:
			indexType = "unique index";
			beforeFields = "(";
			fieldSeparator = ",";
			afterFields = ")";
			break;
		}

		sql.append(indexType + " \"" + index + "\" on \"" + type + "\" " + beforeFields);

		LinkedHashMap<String, TypeField> typeFields = getTypeFields(type);

		for (String field : fields) {
			TypeField typeField = typeFields.get(field);

			if (typeField != null && PT.DOCUMENT.equals(typeField.getType())) {
				sql.append("(\"" + field + "\").text");
			} else {
				sql.append("\"" + field + "\"");
			}

			sql.append(fieldSeparator);
		}

		sql.delete(sql.length() - fieldSeparator.length(), sql.length()).append(afterFields);

		execute(sql);

		if (single) {
			adate = updateTypeDates(type);
		}

		return adate;
	}

	@Override
	public AlterFieldResult alterField(String type, String field, TypeField typeField) {
		return alterField(type, field, typeField, true, true);
	}

	protected AlterFieldResult alterField(String type, String field, TypeField typeField, boolean addReferences,
			boolean single) {
		cacheEnabled = false;

		AlterFieldResult result = new AlterFieldResult();

		TypeField nodeTypeField = getTypeField(type, field);

		String fieldType = typeField.getType();
		String nodeFieldType = nodeTypeField.getType();
		String fieldParameters = typeField.getParameters();
		String nodeParameters = nodeTypeField.getParameters();
		boolean notNull = typeField.isNotNull();
		boolean nodeNotNull = nodeTypeField.isNotNull();

		if (!fieldType.equals(nodeFieldType)) {
			setFieldType(type, field, fieldType);

			if (!PT.isPrimitiveType(nodeFieldType)) {
				dropReference(type, field);
			}

			result.setTypeAltered();
		}

		if (!((fieldParameters == null && nodeParameters == null)
				|| (fieldParameters != null && fieldParameters.equals(nodeParameters)))) {
			result.setParametersAltered();
		}

		if (result.isTypeAltered() || result.isParametersAltered()) {

			execute("alter table \"" + type + "\" alter column \"" + field + "\" type "
					+ sqlType(fieldType, sqlParameters(fieldParameters)));

			if (addReferences && result.isTypeAltered() && !PT.isPrimitiveType(fieldType)) {
				addReference(type, field, fieldType);
			}
		}

		if (notNull != nodeNotNull) {
			checkFieldNullability(type, field, notNull);

			String sqlNotNull = notNull ? "set not null" : "drop not null";

			execute("alter table \"" + type + "\" alter column \"" + field + "\" " + sqlNotNull);

			result.setNotNullAltered();
		}

		if (single) {
			result.setADate(updateTypeDates(type));
		}

		return result;
	}

	@Override
	public ZonedDateTime rename(String type, String newName) {
		return renameType(type, newName, true);
	}

	protected ZonedDateTime renameType(String type, String newName, boolean single) {
		
		checkType(type);
		checkNewName(type, newName);
		
		cacheEnabled = false;

		ZonedDateTime adate = null;

		TypeReference[] references = getDownReferences(type);

		for (TypeReference reference : references) {
			setFieldType(reference.getReferencingType(), reference.getReferencingField(), newName);
		}

		execute("alter table \"" + type + "\" rename to \"" + newName + "\"");

		if (single) {
			adate = updateTypeDates(newName);
		}

		return adate;
	}

	@Override
	public ZonedDateTime renameField(String type, String field, String newName) {
		return renameField(type, field, newName, true);
	}

	protected ZonedDateTime renameField(String type, String field, String newName, boolean single) {
		cacheEnabled = false;

		ZonedDateTime adate = null;

		execute("alter table \"" + type + "\" rename column \"" + field + "\" to \"" + newName + "\"");

		if (single) {
			adate = updateTypeDates(type);
		}

		return adate;
	}

	protected void setTypeDates(String type, ZonedDateTime cdate, ZonedDateTime adate) {
		execute("comment on table \"" + type + "\" is '" + cdate + "|" + adate + "'");
	}

	protected ZonedDateTime updateTypeDates(String type) {
		return updateTypeDates(type, ZonedDateTime.now(ZoneOffset.UTC));
	}

	protected ZonedDateTime updateTypeDates(String type, ZonedDateTime adate) {
		ZonedDateTime[] dates = getTypeDates(type);
		setTypeDates(type, dates[0], adate);
		return adate;
	}

	protected ZonedDateTime[] getTypeDates(String type) {
		return getUTCDateTimeArray(GET_TYPE_DATES_QUERY, type);
	}

	protected void setFieldType(String type, String field, String fieldType) {
		execute("comment on column \"" + type + "\".\"" + field + "\" is '" + fieldType + "'");
	}

	protected String sqlType(String fieldType, String sqlParameters) {
		String sqlType = null;

		switch (fieldType) {
		case PT.STRING:
		case PT.URL:
		case PT.EMAIL:
		case PT.TEL:
			sqlType = "character varying" + sqlParameters;
			break;
		case PT.BINARY:
			sqlType = "bytea";
			break;
		case PT.FILE:
			sqlType = "file";
			break;
		case PT.IMAGE:
			sqlType = "image";
			break;
		case PT.DOCUMENT:
			sqlType = "document";
			break;
		case PT.AUDIO:
			sqlType = "audio";
			break;
		case PT.VIDEO:
			sqlType = "video";
			break;
		case PT.INT16:
			sqlType = "smallint";
			break;
		case PT.INT32:
			sqlType = "integer";
			break;
		case PT.INT64:
			sqlType = "bigint";
			break;
		case PT.FLOAT32:
			sqlType = "real";
			break;
		case PT.FLOAT64:
			sqlType = "double precision";
			break;
		case PT.NUMERIC:
			sqlType = "numeric" + sqlParameters;
			break;
		case PT.BOOLEAN:
			sqlType = "boolean";
			break;
		case PT.DATE:
			sqlType = "date";
			break;
		case PT.TIME:
			sqlType = "time";
			break;
		case PT.DATETIME:
			sqlType = "timestamp";
			break;
		case PT.COLOR:
			sqlType = "character(7)";
			break;
		case PT.TIMEZONE:
			sqlType = "character varying(50)";
			break;
		case PT.TEXT:
		case PT.HTML:
			sqlType = "text";
			break;
		case PT.JSON:
			sqlType = "jsonb";
			break;
		case PT.XML:
			sqlType = "xml";
			break;
		case PT.PASSWORD:
			sqlType = "character(60)";
			break;
		default:
			sqlType = "character varying(100)";
		}

		return sqlType;
	}

	protected void addReference(String type, String field, String fieldType) {
		execute("alter table \"" + type + "\" add constraint " + type + "_" + field + " foreign key (\"" + field
				+ "\") references \"" + fieldType + "\"(id)" + " on update cascade deferrable initially immediate");
	}

	protected void dropReference(String type, String field) {
		execute("alter table \"" + type + "\" drop constraint " + type + "_" + field);
	}

	@Override
	public AlterResult alter(Type type) {
		return alter(type, null, true);
	}

	@Override
	public AlterResult alter(Type type, ZonedDateTime adate) {
		return alter(type, adate, true);
	}

	protected AlterResult alter(Type type, ZonedDateTime adate, boolean single) {
		cacheEnabled = false;

		String typeName = type.getName();

		checkType(typeName);

		if (adate != null && !adate.equals(getADate(typeName))) {
			throw new NXException(typeName, KeyWords.ALREADY_ALTERED_TYPE);
		}

		AlterResult result = new AlterResult();

		if (single) {
			setDeferredConstraints(true);
		}

		for (Map.Entry<String, TypeField> entry : type.getFields().entrySet()) {
			String name = entry.getKey();
			String oldName = entry.getValue().getOldName();

			if (oldName != null && !oldName.equals(name)) {
				renameField(typeName, oldName, name);
				result.addRenamedField(oldName);
			}
		}

		for (Map.Entry<String, TypeIndex> entry : type.getIndexes().entrySet()) {
			String name = entry.getKey();
			String oldName = entry.getValue().getOldName();

			if (oldName != null && !oldName.equals(name)) {
				renameIndex(typeName, oldName, name, false);
				result.addRenamedIndex(oldName);
			}
		}

		Type nodeType = getType(typeName);

		for (Map.Entry<String, TypeField> entry : type.getFields().entrySet()) {
			String field = entry.getKey();

			checkField(typeName, field);

			TypeField typeField = entry.getValue();

			if (nodeType.getFields().containsKey(field)) {
				AlterFieldResult fieldResult = alterField(typeName, field, typeField, single, false);
				if (fieldResult.isAltered()) {
					result.addAlteredField(field, fieldResult);
				}
			} else {
				addField(typeName, field, typeField, single, false);
				result.addAddedField(field);
			}
		}

		for (Map.Entry<String, TypeIndex> entry : type.getIndexes().entrySet()) {
			String index = entry.getKey();

			checkIndex(typeName, index);

			TypeIndex typeIndex = entry.getValue();

			if (nodeType.getIndexes().containsKey(index)) {
				AlterIndexResult indexResult = alterIndex(typeName, index, typeIndex, false);
				if (indexResult.isAltered()) {
					result.addAlteredIndex(index, indexResult);
				}
			} else {
				addIndex(typeName, index, typeIndex, false);
				result.addAddedIndex(index);
			}
		}

		for (Map.Entry<String, TypeIndex> entry : nodeType.getIndexes().entrySet()) {
			String index = entry.getKey();

			if (!type.getIndexes().containsKey(index)) {
				dropIndex(typeName, index, false);
				result.addDroppedIndex(index);
			}
		}

		for (Map.Entry<String, TypeField> entry : nodeType.getFields().entrySet()) {
			String field = entry.getKey();

			if (!type.getFields().containsKey(field)) {
				dropField(typeName, field, false);
				result.addDroppedField(field);
			}
		}

		if (single) {
			setDeferredConstraints(false);
		}

		if (result.isAltered()) {
			ZonedDateTime typeADate = type.getADate();
			if (typeADate == null) {
				typeADate = ZonedDateTime.now(ZoneOffset.UTC);
			}
			updateTypeDates(typeName, typeADate);
			result.setADate(typeADate);
			result.setMessage(strings.gts(typeName, KeyWords.TYPE_SUCCESSFULLY_ALTERED));
		} else {
			result.setADate(getADate(typeName));
			result.setMessage(strings.gts(typeName, KeyWords.TYPE_NOT_ALTERED));
		}

		return result;
	}

	@Override
	public ZonedDateTime dropField(String type, String field) {
		return dropField(type, field, true);
	}

	protected ZonedDateTime dropField(String type, String field, boolean single) {
		cacheEnabled = false;

		ZonedDateTime adate = null;

		checkFieldIsPartOfIndex(type, field, getTypeIndexes(type));

		execute("alter table \"" + type + "\" drop column \"" + field + "\"");

		if (single) {
			adate = updateTypeDates(type);
		}

		return adate;
	}

	protected void checkFieldNullability(String type, String field, boolean notNull) {
		if (notNull && hasNullValues(type, field)) {
			throw new FieldException(type, field, KeyWords.FIELD_HAS_NULL_VALUES);
		}
	}

	protected void checkNewFieldNullability(String type, String field, boolean notNull) {
		if (notNull && hasObjects(type)) {
			throw new FieldException(type, field, KeyWords.TYPE_ALREADY_HAS_OBJECTS);
		}
	}

	protected void checkFieldIsPartOfIndex(String type, String field, LinkedHashMap<String, TypeIndex> indexes) {
		for (Map.Entry<String, TypeIndex> entry : indexes.entrySet()) {
			if (ArrayUtils.contains(entry.getValue().getFields(), field)) {
				throw new FieldException(type, field, KeyWords.FIELD_IS_PART_OF_INDEX, entry.getKey());
			}
		}
	}

	protected void checkMissingReferences(LinkedHashMap<String, ArrayList<String>> objects) {
		if (objects.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder();
		ArrayList<Object> parameters = new ArrayList<>();

		for (Reference reference : getUpReferences(objects.keySet().toArray(new String[] {}))) {
			String referencingType = reference.getReferencingType();
			String referencingField = reference.getReferencingField();
			String referencedType = reference.getReferencedType();

			sql.append("select distinct '" + referencedType + "' as type, \"" + referencingField + "\" as id"
					+ " from \"" + referencingType + "\" where \"" + referencingField + "\""
					+ " not in(select id from \"" + referencedType + "\") and id in(?) union ");
			parameters.add(objects.get(referencingType).toArray());
		}

		if (sql.length() > 0) {
			sql.delete(sql.length() - 6, sql.length());
			sql.append(" order by type, id");

			Tuple[] references = query(sql, parameters);

			LinkedHashMap<String, List<String>> referencesByType = Arrays.stream(references)
					.collect(Collectors.groupingBy(reference -> reference.getString(KeyWords.TYPE), LinkedHashMap::new,
							Collectors.mapping(reference -> reference.getString(KeyWords.ID), Collectors.toList())));

			if (referencesByType != null && referencesByType.size() > 0) {
				throw new StringException(
						strings.gts(KeyWords.MISSING_REFERENCES) + ": " + new Serial(referencesByType, Format.JSON));
			}
		}
	}

	protected void checkFileField(String type, String field, Object value) {
		if (value instanceof File) {
			String[] allowedContentTypes = typeSettings.getFieldStringArray(type, field,
					KeyWords.ALLOWED_CONTENT_TYPES);

			if (allowedContentTypes != null) {
				String contentType = ((File) value).getContentType();

				if (!ArrayUtils.contains(allowedContentTypes, contentType)) {
					throw new FieldException(type, field, KeyWords.DISALLOWED_CONTENT_TYPE, contentType);
				}
			}
		}
	}
	
	protected void checkFieldRange(String type, String field, Object value) {
		String fieldType = getFieldType(type, field);
		
		if (PT.isTimeType(fieldType) || PT.isNumericType(fieldType)) {
			FieldRange range = getFieldRange(type, field);
			if (range != null && !range.isInRange(value)) {
				throw new FieldException(type, field, KeyWords.OUT_OF_RANGE_VALUE, value);
			}
		}
	}

	@Override
	public AlterIndexResult alterIndex(String type, String index, TypeIndex typeIndex) {
		return alterIndex(type, index, typeIndex, true);
	}

	protected AlterIndexResult alterIndex(String type, String index, TypeIndex typeIndex, boolean single) {
		cacheEnabled = false;

		AlterIndexResult result = new AlterIndexResult();

		TypeIndex nodeTypeIndex = getTypeIndex(type, index);
		IndexMode mode = typeIndex.getMode();
		IndexMode nodeMode = nodeTypeIndex.getMode();
		String[] fields = typeIndex.getFields();
		String[] nodeFields = nodeTypeIndex.getFields();

		if (!mode.equals(nodeMode)) {
			result.setModeAltered();
		}

		if (!Arrays.equals(fields, nodeFields)) {
			result.setFieldsAltered();
		}

		if (result.isModeAltered() || result.areFieldsAltered()) {
			dropIndex(type, index, false);
			addIndex(type, index, typeIndex, false);
		}

		if (single) {
			result.setADate(updateTypeDates(type));
		}

		return result;
	}

	@Override
	public ZonedDateTime dropIndex(String type, String index) {
		return dropIndex(type, index, true);
	}

	protected ZonedDateTime dropIndex(String type, String index, boolean single) {
		cacheEnabled = false;

		ZonedDateTime adate = null;

		execute("drop index \"" + index + "\"");

		if (single) {
			adate = updateTypeDates(type);
		}

		return adate;
	}

	@Override
	public ZonedDateTime renameIndex(String type, String index, String newName) {
		return renameIndex(type, index, newName, true);
	}

	protected ZonedDateTime renameIndex(String type, String index, String newName, boolean single) {
		cacheEnabled = false;

		ZonedDateTime adate = null;

		execute("alter index \"" + index + "\" rename to \"" + newName + "\"");

		if (single) {
			adate = updateTypeDates(type);
		}

		return adate;
	}

	@Override
	public ZonedDateTime insert(NXObject object) {
		return insert(object, true);
	}

	public ZonedDateTime insert(NXObject object, boolean single) {
		String id = object.getId();
		String type = object.getType();

		checkType(type);
		
		if (id != null && single && existsObject(type, id)) {
			throw new ObjectException(type, id, KeyWords.OBJECT_ALREADY_EXISTS);
		}

		for (Map.Entry<String, TypeField> entry : getTypeFields(type).entrySet()) {
			String field = entry.getKey();
			TypeField typeField = entry.getValue();
			String fieldType = typeField.getType();
			Object value = null;

			if (object.containsKey(field)) {
				value = object.get(field);

				if (value == null && typeField.isNotNull()){
					throw new FieldException(type, field, KeyWords.EMPTY_FIELD);
				}
			} else {
				value = getFieldDefault(type, field, fieldType);
				
				if (value != null) {
					object.put(field, value);
				} else if (typeField.isNotNull()) {
					throw new FieldException(type, field, KeyWords.MISSING_FIELD);
				}
			}	

			if (value != null) {
				checkFieldRange(type, field, value);
				checkFileField(type, field, value);
			}
		}

		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

		ZonedDateTime cdate = object.getCDate();
		if (cdate == null) {
			cdate = now;
		}

		ZonedDateTime udate = object.getUDate();
		if (udate == null) {
			udate = now;
		}

		StringBuilder sql = new StringBuilder("insert into \"" + type + "\"");
		StringBuilder sqlFields = new StringBuilder(" (id, cdate, udate, backup,");
		StringBuilder sqlValues = new StringBuilder();
		ArrayList<Object> parameters = new ArrayList<Object>();
		if (object.getId() == null) {
			sqlValues.append(" values(" + UUID_FUNCTION + ",");
		} else {
			sqlValues.append(" values(?,");
			parameters.add(object.getId());
		}

		sqlValues.append("?,?,?,");
		parameters.add(cdate);
		parameters.add(udate);
		parameters.add(false);

		for (Entry<String, Object> entry : object.getFields().entrySet()) {
			String field = entry.getKey();

			checkField(type, field);

			sqlFields.append("\"" + field + "\",");
			sqlValues.append("?,");
			parameters.add(entry.getValue());
		}

		sqlFields.deleteCharAt(sqlFields.length() - 1).append(")");
		sqlValues.deleteCharAt(sqlValues.length() - 1).append(")");

		sql.append(sqlFields.toString() + sqlValues.toString());

		execute(sql, true, 1, parameters);

		return udate;
	}

	@Override
	public ZonedDateTime update(NXObject object) {
		return update(object, null, true);
	}

	@Override
	public ZonedDateTime update(NXObject object, ZonedDateTime udate) {
		return update(object, udate, true);
	}

	public ZonedDateTime update(NXObject object, ZonedDateTime udate, boolean single) {
		String type = object.getType();

		checkType(type);

		String id = object.getId();

		checkId(type, id);

		for (Map.Entry<String, TypeField> entry : getTypeFields(type).entrySet()) {
			String field = entry.getKey();
			TypeField typeField = entry.getValue();

			if (object.containsKey(field) && typeField.isNotNull() && object.get(field) == null) {
				throw new FieldException(type, field, KeyWords.EMPTY_FIELD);
			}

			Object value = object.get(field);

			if (value != null) {
				checkFieldRange(type, field, value);
				checkFileField(type, field, value);
			}
		}

		if (single && !existsObject(type, id)) {
			throw new ObjectNotFoundException(type, id);
		}

		if (udate != null && !udate.equals(getUDate(type, id))) {
			throw new ObjectException(type, id, KeyWords.ALREADY_UPDATED_OBJECT);
		}

		StringBuilder sql = new StringBuilder("update \"" + type + "\" set ");
		ArrayList<Object> parameters = new ArrayList<Object>();

		ZonedDateTime objectCDate = object.getCDate();
		if (objectCDate != null) {
			sql.append("cdate = ?,");
			parameters.add(objectCDate);
		}

		ZonedDateTime objectUDate = object.getUDate();
		if (objectUDate == null) {
			objectUDate = ZonedDateTime.now(ZoneOffset.UTC);
		}

		sql.append(" udate = ?, backup = ?,");
		parameters.add(objectUDate);
		parameters.add(false);

		for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
			String field = entry.getKey();

			checkField(type, field);

			sql.append("\"" + field + "\" = ?,");
			parameters.add(entry.getValue());
		}

		sql.deleteCharAt(sql.length() - 1).append(" where id = ?");
		parameters.add(id);

		execute(sql, true, 1, parameters);

		return objectUDate;
	}

	@Override
	public UpdateIdResult updateId(String type, String id, String newId) {
		StringBuilder sql = new StringBuilder("update \"" + type + "\" set id = ");
		ArrayList<Object> parameters = new ArrayList<>();
		ZonedDateTime udate = ZonedDateTime.now(ZoneOffset.UTC);
		
		if (newId == null) {
			sql.append(UUID_FUNCTION);
		} else {
			sql.append("?");
			parameters.add(newId);
		}
		
		sql.append(", udate = ? where id = ?");
		parameters.add(udate);
		parameters.add(id);
				
		if (newId == null) {
			sql.append(" returning id");
			newId = getString(sql.toString(), parameters.toArray());
		} else {
			execute(sql, parameters);
		}		
		
		String message = strings.gts(type, KeyWords.OBJECT_ID_SUCCESSFULLY_UPDATED);
		
		return new UpdateIdResult(message, udate, newId);
	}

	@Override
	public ZonedDateTime updateField(String type, String id, String field, Object value) {
		NXObject object = new NXObject(type, id);
		object.put(field, value);
		return update(object);
	}

	@Override
	public ZonedDateTime updatePassword(String type, String id, String field, String currentPassword,
			String newPassword, String newPasswordRepeat) {

		if (currentPassword == null) {
			if (!auth.isAdministrator() && getPasswordField(type, id, field) != null) {
				throw new NXException(type, KeyWords.EMPTY_CURRENT_PASSWORD);
			}
		} else {
			if (!Security.checkPassword(getPasswordField(type, id, field), currentPassword)) {
				throw new NXException(type, KeyWords.INVALID_CURRENT_PASSWORD);
			}
		}

		if (!Security.passwordsMatch(newPassword, newPasswordRepeat)) {
			throw new NXException(type, KeyWords.PASSWORDS_DONT_MATCH);
		}

		if (!Security.checkPasswordStrength(newPassword)) {
			throw new NXException(type, KeyWords.INVALID_PASSWORD);
		}

		return updateField(type, id, field, Security.passwordHash(newPassword));
	}

	@Override
	public boolean checkPassword(String type, String id, String field, String password) {
		return Security.checkPassword(getPasswordField(type, id, field), password);
	}

	@Override
	public NXObject get(String type, String id, String[] fields, String lang, boolean fulltext,
			boolean binary, boolean documentPreview, boolean password, boolean objectName, 
			boolean referencesName) {
		Objects objects = select(type, fields, lang, new IdFilter(Comparison.EQUAL, id), null, null,
				fulltext, binary, documentPreview, password, objectName, referencesName, 0L, 1L);

		if (objects != null && objects.getItems().length == 1) {
			return objects.getItems()[0];
		} else {
			return null;
		}
	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		Filter[] filters = filter != null ? new Filter[] { filter } : null;
		return select(type, fields, lang, filters, search, order, false, false, true, false, true, true, 
				offset, limit);
	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {

		Filter[] filters = filter != null ? new Filter[] { filter } : null;

		return select(type, fields, lang, filters, search, order, fulltext, binary, documentPreview,
				password, objectsName, referencesName, offset, limit);
	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {

		return select(type, fields, lang, filters, search, order, false, false, true, false, true, true,
				offset, limit);
	}

	@Override
	public Objects select(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {

		Objects objects = null;

		try {

			SelectQuery query = new SelectQuery(type, fields, lang, filters, search, order, fulltext,
					binary, documentPreview, password, objectsName, referencesName, offset, limit);

			if (query.getCount() > 0) {
			
				Tuple[] tuples = query(query.getSQL(), query.getParameters());

				ArrayList<NXObject> items = new ArrayList<NXObject>();

				for (Tuple tuple : tuples) {
					items.add(getObject(type, query.getTypeFields(), fulltext, binary, documentPreview,
							objectsName, referencesName, tuple));
				}

				objects = new Objects(items.toArray(new NXObject[] {}), query.getCount(), query.getOffset(),
					query.getLimit(), query.getMinLimit(), query.getMaxLimit(), query.getLimitIncrement());
			} else {
				objects = new Objects();
			}
		} catch (FulltextIndexNotFoundException e) {
			objects = new Objects();
		}

		return objects;
	}

	@Override
	public Tuples select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters, String search,
			String[] searchFields, String order, Long offset, Long limit) {

		Tuples tuples = null;
		
		SelectQuery query = new SelectQuery(type, sql, parameters, filters, search, searchFields,
				order, offset, limit);

		if (query.getCount() > 0) {
		
			tuples = new Tuples(query(query.getSQL(), query.getParameters()), query.getCount(), query.getOffset(),
				query.getLimit(), query.getMinLimit(), query.getMaxLimit(), query.getLimitIncrement());
		} else {
			tuples = new Tuples();
		}
		
		return tuples;
	}

	@Override
	public Tuple[] select(String type, StringBuilder sql, ArrayList<Object> parameters, String filters, String order) {

		Tuple[] tuples = null;
		
		SelectQuery query = new SelectQuery(type, sql, parameters, filters, order);

		if (query.getCount() > 0) {
		
			tuples = query(query.getSQL(), query.getParameters());
		
		} else {
			
			tuples = new Tuple[] {};
		}
		
		return tuples;
	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		Filter[] filters = filter != null ? new Filter[] { filter } : null;
		return selectStream(type, fields, lang, filters, search, order, false, false, true, false,
				true, true, offset, limit);
	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter filter, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {

		Filter[] filters = filter != null ? new Filter[] { filter } : null;
		return selectStream(type, fields, lang, filters, search, order, fulltext, binary,
				documentPreview, password, objectsName, referencesName, offset, limit);
	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		return selectStream(type, fields, lang, filters, search, order, false, false, true, false,
				true, true, offset, limit);
	}

	@Override
	public ObjectsStream selectStream(String type, String[] fields, String lang, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
			boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {

		ObjectsStream objects = null;

		try {
			SelectQuery query = new SelectQuery(type, fields, lang, filters, search, order, fulltext,
					binary, documentPreview, password, objectsName, referencesName, offset, limit);
			
			if (query.getCount() > 0) {
				TuplesStream tuples = new PostgreSQLTuplesStream(query.getSQL(), query.getParameters());
				objects = new PostgreSQLObjectsStream(type, query.getTypeFields(), fulltext, binary,
						documentPreview, objectsName, referencesName, query.getCount(), tuples);
			} else {
				objects = new PostgreSQLObjectsStream();
			}
		} catch (FulltextIndexNotFoundException e) {
			objects = new PostgreSQLObjectsStream();
		}

		return objects;
	}

	protected NXObject getObject(String type, LinkedHashMap<String, TypeField> typeFields,
			boolean fulltext, boolean binary, boolean documentPreview, boolean objectName,
			boolean referencesName, Tuple tuple) {
		
		String name = objectName ? tuple.getString("@name") : null;
		
		NXObject object = new NXObject(type, tuple.getString(KeyWords.ID), name,
				tuple.getUTCDateTime(KeyWords.CDATE), tuple.getUTCDateTime(KeyWords.UDATE),
				tuple.getBoolean(KeyWords.BACKUP));

		for (Entry<String, TypeField> entry : typeFields.entrySet()) {
			String field = entry.getKey();
			TypeField typeField = entry.getValue();
			String fieldType = typeField.getType();
			Object value = null;

			switch (fieldType) {
			case PT.HTML:
				if (fulltext) {
					value = tuple.getHTML(field, lang, typeSettings.getFieldString(type, field,
							KeyWords.HTML_ALLOWED_TAGS));
				} else {
					value = tuple.getHTMLText(field);
				}
				break;
			case PT.XML:
				if (fulltext) {
					value = tuple.getXML(field, lang, typeSettings.getFieldString(type, field,
							KeyWords.XML_ALLOWED_TAGS));
				} else {
					value = tuple.getString(field);
				}
				break;
			case PT.URL:
				value = tuple.getURL(field);
				break;
			case PT.EMAIL:
				value = tuple.getEmail(field);
				break;
			case PT.TIMEZONE:
				value = tuple.getTimeZone(field);
				break;
			case PT.COLOR:
				value = tuple.getColor(field);
				break;
			case PT.DATE:
				value = tuple.getDate(field);
				break;
			case PT.TIME:
				value = tuple.getTime(field);
				break;
			case PT.DATETIME:
				value = tuple.getDateTime(field);
				break;
			case PT.JSON:
				value = tuple.getJSON(field);
				break;
			case PT.DOCUMENT:
				if (binary) {
					value = tuple.getDocument(field);
				} else {
					if (documentPreview) {
						String text = tuple.getText("@" + field + "_text");
						if (text != null) {
							Integer size = tuple.getInt32("@" + field + "_size");
							value = new DocumentPreview(text, size);
						}
					} else {
						value = tuple.get("@" + field + "_size");
					}
				}
				break;
			case PT.FILE:
			case PT.IMAGE:
			case PT.AUDIO:
			case PT.VIDEO:
			case PT.INT16:
			case PT.INT32:
			case PT.INT64:
			case PT.FLOAT32:
			case PT.FLOAT64:
			case PT.NUMERIC:
			case PT.BOOLEAN:
			case PT.BINARY:
			case PT.STRING:
			case PT.TEXT:
			case PT.TEL:
			case PT.PASSWORD:
				value = tuple.get(field);
				break;
			default:
				if (referencesName) {
					String id = tuple.getString("@" + field + "_id");
					if (id != null) {
						value = new ObjectReference(id, tuple.getString("@" + field + "_name"));
					}
				} else {
					value = tuple.get(field);
				}
			}

			object.put(field, value);
		}

		return object;
	}

	@Override
	public Type getType(String type) {
		ZonedDateTime[] typeDates = getTypeDates(type);

		return new Type(type, typeDates[0], typeDates[1], getTypeFields(type), getTypeIndexes(type));
	}

	@Override
	public LinkedHashMap<String, Type> getTypes(String[] types) {
		LinkedHashMap<String, Type> typeObjects = new LinkedHashMap<>();

		for (String type : types) {
			typeObjects.put(type, getType(type));
		}

		return typeObjects;
	}

	@Override
	public String[] getTypesName() {
		return getStringArray(GET_TYPES_NAME_QUERY);
	}

	@Override
	public TypeInfo[] getTypesInfo() {
		TypeInfo[] types = query(GET_TYPES_SIZE_QUERY, TypeInfo.class);
		for (TypeInfo type : types) {
			Tuple objectsUDate = getTuple(
					"select count(*) as objects, max(udate) as udate from \"" + type.getName() + "\"");
			type.setObjects(objectsUDate.getInt64(KeyWords.OBJECTS));
			type.setUDate(objectsUDate.getUTCDateTime(KeyWords.UDATE));
		}
		return types;
	}
	
	@Override
	public TreeMap<String, TypeInfo> getTypesInfoOrderByName() {
		TreeMap<String, TypeInfo> types = new TreeMap<>();
		
		for (TypeInfo type : getTypesInfo()) {
			types.put(strings.getTypeName(type.getName()), type);
		}
		
		return types;
	}

	@Override
	public Boolean existsType(String type) {
		return getBoolean(EXISTS_TYPE_QUERY, type);
	}

	@Override
	public Boolean existsObject(String type, String id) {
		return getBoolean("select exists(select 1 from \"" + type + "\" where id = ?)", id);
	}

	@Override
	public String getName(String type, String id, String lang) {
		String name = null;
		String idName = typeSettings.gts(type, KeyWords.ID_NAME);

		if (idName != null) {
			String sql = "select id, name from (" + idName + ") as id_name where id = ?";
			Object[] parameters = null;

			if (idName.contains("?")) {
				parameters = new Object[] { lang, id };
			} else {
				parameters = new Object[] { id };
			}

			name = getTuple(sql, parameters).getString(KeyWords.NAME);

			if (name == null) {
				name = id;
			}

		} else {
			name = id;
		}

		return name;
	}
	
	@Override
	public Names getNames(String referencedType, String referencingAction,
			String referencingType, String referencingField, String lang) {
		return getNames(referencedType, lang, (String) null, (Long) null, (Long) null);
	}
	
	@Override
	public Names getNames(String referencedType, String referencingAction,
			String referencingType, String referencingField, String lang, String search, Long offset,
			Long limit) {
		return getNames(referencedType, lang, search, offset, limit);
	}
	
	@Override
	public Names getNames(String type, String lang) {
		return getNames(type, lang, (String) null, (Long) null, (Long) null);
	}
	
	@Override 
	public Names getNames(String type, String lang, String search,
			Long offset, Long limit) {
		StringBuilder sql = new StringBuilder();
		ArrayList<Object> parameters = new ArrayList<>();
		String idName = typeSettings.gts(type, KeyWords.ID_NAME);

		if (idName != null) {
			sql.append(idName);
			
			if (idName.contains("?")) {
				parameters.add(lang);
			}
		} else {
			sql.append("select id, id as name from \"" + type + "\"");
		}
		
		return getNames(type, sql, parameters, lang, search, offset, limit);
	}
	
	@Override
	public Names getNames(String type, String sql,
			Object[] parameters, String lang, String search, Long offset, Long limit) {
		
		return getNames(type, new StringBuilder(sql), 
				new ArrayList<Object>(Arrays.asList(parameters)), lang, search, offset, limit);
	}

	@Override
	public Names getNames(String type, StringBuilder sql, ArrayList<Object> parameters, String lang,
			String search, Long offset, Long limit) {
				
		if (search != null) {
			sql = new StringBuilder("select id, name from (" + sql.toString() + ") as names"
					+ " where id ilike ? or name ilike ?");
			search = "%" + search + "%";
			parameters.add(search);
			parameters.add(search);
		}
		
		String order = typeSettings.gts(type, KeyWords.ID_NAME + "." + KeyWords.ORDER);
		if (order != null) {
			sql.append(" order by " + order);
		} else {
			sql.append(" order by name, id");
		}	
		
		Long count = count(sql, parameters);
		
		if (offset != null) {
			sql.append(" offset ?");
			parameters.add(offset);
		}
		
		if (limit != null) {
			sql.append(" limit ?");
			parameters.add(limit);
		}
		
		LinkedHashMap<String, String> items = new LinkedHashMap<String, String>();

		Tuple[] tuples = query(sql, parameters);
		
		for (Tuple tuple : tuples) {
			String id = tuple.getString(KeyWords.ID);
			String name = tuple.getString(KeyWords.NAME);

			if (name == null) {
				name = id;
			}

			items.put(id, name);
		}

		return new Names(items, count);
	}

	@Override
	public LinkedHashMap<String, ObjectInfo[]> getObjectsInfo(String[] types) {
		LinkedHashMap<String, ObjectInfo[]> objects = new LinkedHashMap<>();

		if (types != null) {
			for (String type : types) {
				objects.put(type, query("select id, udate from \"" + type + "\" order by id", ObjectInfo.class));
			}
		}

		return objects;
	}

	@Override
	public Reference[] getReferences() {
		return query(GET_REFERENCES_QUERY + " order by referenced_type, referencing_type", Reference.class);
	}
	
	@Override
	public TreeMap<String, TreeMap<String, TreeMap<String, Reference>>> getReferencesOrderByNames() {
		TreeMap<String, TreeMap<String, TreeMap<String, Reference>>> references = new TreeMap<>();
		
		HashMap<String, String> referencedTypeNames = new HashMap<>();
		HashMap<String, HashMap<String, String>> referencingTypeNamesMap = new HashMap<>();
		HashMap<String, HashMap<String, TreeMap<String, Reference>>> referencingFieldNamesMapMap
			= new HashMap<>();	
		
		for (Reference reference : getReferences()) {
			
			String referencedType = reference.getReferencedType();
			String referencingType = reference.getReferencingType();
			String referencingField = reference.getReferencingField();
			
			HashMap<String, String> referencingTypeNames = referencingTypeNamesMap.get(referencedType);
			HashMap<String, TreeMap<String, Reference>> referencingFieldNamesMap
				= referencingFieldNamesMapMap.get(referencedType);
			
			if (referencingTypeNames == null) {
								
				referencedTypeNames.put(referencedType, strings.getTypeName(referencedType));
				
				referencingTypeNames = new HashMap<>();
				referencingTypeNamesMap.put(referencedType, referencingTypeNames);
				
				referencingFieldNamesMap = new HashMap<>();
				referencingFieldNamesMapMap.put(referencedType, referencingFieldNamesMap);
			}
			
			referencingTypeNames.put(referencingType, strings.getTypeName(referencingType));
			
			TreeMap<String, Reference> referencingFieldNames = referencingFieldNamesMap
					.get(referencingType);
			
			if (referencingFieldNames == null) {
				referencingFieldNames = new TreeMap<>();
				referencingFieldNamesMap.put(referencingType, referencingFieldNames);
			}
			
			referencingFieldNames.put(strings.getFieldName(referencingType, referencingField), 
					reference);
		}
		
		for (Map.Entry<String, String> referencedTypeEntry 	: referencedTypeNames.entrySet()) {
			String referencedType = referencedTypeEntry.getKey();
			String referencedTypeName = referencedTypeEntry.getValue();
			
			TreeMap<String, TreeMap<String, Reference>> referencingTypes = new TreeMap<>();
			references.put(referencedTypeName, referencingTypes);		
			
			for (Map.Entry<String, String> referencingTypeEntry : referencingTypeNamesMap
					.get(referencedType).entrySet()) {
				
				String referencingType = referencingTypeEntry.getKey();
				String referencingTypeName = referencingTypeEntry.getValue();
				
				referencingTypes.put(referencingTypeName, referencingFieldNamesMapMap
						.get(referencedType).get(referencingType));
			}
		}
		
		return references;
	}

	@Override
	public TypeReference[] getDownReferences(String type) {
		return query(GET_DOWN_REFERENCES_QUERY, TypeReference.class, type);
	}

	@Override
	public TypeReference[] getUpReferences(String type) {
		return query(GET_UP_REFERENCES_QUERY, TypeReference.class, type);
	}

	@Override
	public Reference[] getUpReferences(String[] types) {
		return query(GET_REFERENCES_QUERY + " where referencing.relname in (?)", Reference.class,
				new Object[] { types });
	}

	@Override
	public TypeField getTypeField(String type, String field) {
		return getTypeFields(type, new String[] { field }).get(field);
	}

	@Override
	public LinkedHashMap<String, TypeField> getTypeFields(String type, String... fields) {
		LinkedHashMap<String, TypeField> typeFields = null;
		LinkedHashMap<String, TypeField> nodeTypeFields = getTypeFields(type);

		if (fields != null) {
			String[] nodeFields = nodeTypeFields.keySet().toArray(new String[] {});
			typeFields = new LinkedHashMap<>();

			for (String field : fields) {
				if (!ArrayUtils.contains(nodeFields, field)) {
					throw new FieldNotFoundException(type, field);
				} else {
					typeFields.put(field, nodeTypeFields.get(field));
				}
			}
		} else {
			typeFields = nodeTypeFields;
		}

		return typeFields;
	}

	@Override
	public LinkedHashMap<String, TypeField> getTypeFields(String type) {

		LinkedHashMap<String, TypeField> fields = null;

		if (cacheEnabled) {
			fields = cache.getFields(type);
		}

		if (fields == null) {
			if (!existsType(type)) {
				throw new TypeNotFoundException(type);
			}

			fields = new LinkedHashMap<>();

			Tuple[] tuples = query(GET_TYPE_FIELDS_QUERY, type);

			for (Tuple tuple : tuples) {
				String field = tuple.getString(KeyWords.NAME);
				String fieldType = tuple.getString(KeyWords.TYPE);
				FieldRange range = null;
				
				if (PT.isTimeType(fieldType) || PT.isNumericType(fieldType)) {
				
					String min = typeSettings.getFieldString(type, field, KeyWords.MIN);
					String max = typeSettings.getFieldString(type, field, KeyWords.MAX);
					
					if (min != null || max != null) {
						range = new FieldRange(min, max);
					}
				}
				
				fields.put(field, new TypeField(fieldType, tuple.getInt32(KeyWords.LENGTH),
						tuple.getInt32(KeyWords.PRECISION), tuple.getInt32(KeyWords.SCALE),
							range, tuple.getBoolean(KeyWords.NOT_NULL)));
			}

			if (cacheEnabled) {
				cache.addFields(type, fields);
			}
		}

		return new LinkedHashMap<String, TypeField>(fields);

	}

	@Override
	public TypeIndex getTypeIndex(String type, String index) {
		return getTypeIndexes(type, new String[] { index }).get(index);
	}

	@Override
	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String type, String... indexes) {
		LinkedHashMap<String, TypeIndex> typeIndexes = null;
		LinkedHashMap<String, TypeIndex> nodeTypeIndexes = getTypeIndexes(type);

		if (indexes != null) {
			String[] nodeIndexes = nodeTypeIndexes.keySet().toArray(new String[] {});
			typeIndexes = new LinkedHashMap<>();

			for (String index : indexes) {
				if (!ArrayUtils.contains(nodeIndexes, index)) {
					throw new IndexNotFoundException(type, index);
				} else {
					typeIndexes.put(index, nodeTypeIndexes.get(index));
				}
			}
		} else {
			typeIndexes = nodeTypeIndexes;
		}

		return typeIndexes;
	}

	@Override
	public LinkedHashMap<String, TypeIndex> getTypeIndexes(String type) {
		
		LinkedHashMap<String, TypeIndex> indexes = null;

		if (cacheEnabled) {
			indexes = cache.getIndexes(type);
		}

		if (indexes == null) {
			indexes = new LinkedHashMap<>();

			Tuple[] tuples = query(GET_TYPE_INDEXES_QUERY, type);

			for (Tuple tuple : tuples) {
				String name = tuple.getString(KeyWords.NAME);
				IndexMode mode = IndexMode.valueOf(tuple.getString(KeyWords.MODE).toUpperCase());
				String[] fields = tuple.getStringArray(KeyWords.FIELDS);

				indexes.put(name, new TypeIndex(mode, fields));
			}

			if (cacheEnabled) {
				cache.addIndexes(type, indexes);
			}
		}

		return new LinkedHashMap<String, TypeIndex>(indexes);
	}

	@Override
	public String getFieldType(String type, String field) {
		return getTypeField(type, field).getType();
	}

	@Override
	public void drop(String... types) {
		cacheEnabled = false;

		checkTypes(types);

		StringBuilder sql = new StringBuilder("drop table ");

		for (String type : types) {
			if (ArrayUtils.contains(Type.SYSTEM_TYPES, type)) {
				throw new TypeException(type, KeyWords.SYSTEM_TYPES_CANT_BE_DROPPED);
			}

			sql.append("\"" + type + "\",");
		}

		sql.deleteCharAt(sql.length() - 1);

		execute(sql);
	}

	@Override
	public void delete(String type, String... objects) {
		checkObjects(type, objects);

		execute("delete from \"" + type + "\" where id in(?)", objects.length, new Object[] { objects });
	}

	@Override
	public Object getField(String type, String id, String field) {
		return getObject("select \"" + field + "\" from \"" + type + "\" where id = ?", id);
	}

	@Override
	public String getStringField(String type, String id, String field) {
		return (String) getField(type, id, field);
	}

	@Override
	public byte[] getBinaryField(String type, String id, String field) {
		return (byte[]) getField(type, id, field);
	}

	@Override
	public Image getImageField(String type, String id, String field) {
		return Tuple.parseImage(getField(type, id, field));
	}

	@Override
	public byte[] getImageContent(String type, String id, String field) {
		return getBinary("select (\"" + field + "\").content from \"" + type + "\" where id = ?", id);
	}

	@Override
	public byte[] getImageThumbnail(String type, String id, String field) {
		return getBinary("select (\"" + field + "\").thumbnail from \"" + type + "\" where id = ?", id);
	}

	@Override
	public String getImageContentType(String type, String id, String field) {
		return getCompositeFieldContentType(type, id, field);
	}

	@Override
	public String getDocumentContentType(String type, String id, String field) {
		return getCompositeFieldContentType(type, id, field);
	}

	@Override
	public String getCompositeFieldContentType(String type, String id, String field) {
		return getString("select (\"" + field + "\").content_type from \"" + type + "\" where id = ?", id);
	}

	@Override
	public XML getXMLField(String type, String id, String field) {
		String xml = getStringField(type, id, field);
		String allowedTags = typeSettings.getFieldString(type, field, KeyWords.XML_ALLOWED_TAGS);
		return Tuple.parseXML(xml, lang, allowedTags);
	}

	@Override
	public Element getHTMLElement(String type, String id, String field, String element) {
		Element htmlElement = null;

		HTMLFragment htmlFragment = getHTMLField(type, id, field);

		if (htmlFragment != null) {
			htmlElement = htmlFragment.getElementById(element);
		}

		return htmlElement;
	}

	@Override
	public Element getXMLElement(String type, String id, String field, String element) {
		Element xmlElement = null;

		String xpath = "//*[@id='" + element + "']";

		PgArray result = (PgArray) getObject("select xpath(?, \"" + field + "\") from \"" + type + "\" where id = ?",
				xpath, id);

		if (result != null) {
			Object[] nodes = null;

			try {
				nodes = (Object[]) result.getArray();
			} catch (SQLException e) {
				throwException(e);
			}

			if (nodes != null && nodes.length > 0) {
				StringBuilder xml = new StringBuilder();
				for (Object node : nodes) {
					xml.append(node);
				}

				String allowedTags = typeSettings.getFieldString(type, field, KeyWords.XML_ALLOWED_TAGS);
				XML xmlDocument = new XML(xml.toString(), lang, allowedTags);
				xmlElement = xmlDocument.getDocumentElement();
			}
		}

		return xmlElement;
	}

	@Override
	public HTMLFragment getHTMLField(String type, String id, String field) {
		String html = getStringField(type, id, field);
		String allowedTags = typeSettings.getFieldString(type, field, KeyWords.HTML_ALLOWED_TAGS);
		return Tuple.parseHTML(html, lang, allowedTags);
	}

	@Override
	public Document getDocumentField(String type, String id, String field) {
		return Tuple.parseDocument(getField(type, id, field));
	}

	@Override
	public String getPasswordField(String type, String id, String field) {
		return getStringField(type, id, field);
	}

	@Override
	public ObjectField getObjectField(String type, String id, String field) {
		TypeField typeField = getTypeField(type, field);
		String fieldType = typeField.getType();
		StringBuilder sql = new StringBuilder("select udate, ");

		String contentType = getFieldContentType(type, field);

		switch (fieldType) {
		case PT.PASSWORD:
			sql.append("'" + Security.HIDDEN_PASSWORD + "' as field");
			break;

		case PT.FILE:
		case PT.IMAGE:
		case PT.DOCUMENT:
		case PT.AUDIO:
		case PT.VIDEO:
			sql.append("(\"" + field + "\").content as field");

			if (contentType == null) {
				sql.append(", (\"" + field + "\").content_type");
			}

			break;

		default:
			sql.append("\"" + field + "\" as field");
		}

		sql.append(" from \"" + type + "\" where id = ?");

		Tuple tuple = getTuple(sql, id);
		if (tuple == null) {
			throw new ObjectNotFoundException(type, id);
		}

		Object value = tuple.get("field");

		if (value instanceof PgSQLXML) {
			try {
				value = ((PgSQLXML) value).getString();
			} catch (SQLException e) {
				throwException(e);
			}
		}

		if (contentType == null) {
			contentType = tuple.getString(KeyWords.CONTENT_TYPE);
		}

		return new ObjectField(value, tuple.getUTCDateTime(KeyWords.UDATE), contentType);
	}

	@Override
	public LinkedHashMap<String, String> getFieldsContentType(String type) {
		LinkedHashMap<String, String> contentTypes = null;

		if (cacheEnabled) {
			contentTypes = cache.getContentTypes(type);
		}

		if (contentTypes == null) {
			contentTypes = new LinkedHashMap<>();

			for (Map.Entry<String, TypeField> entry : getTypeFields(type).entrySet()) {
				String field = entry.getKey();
				String contentType = getFieldContentType(type, field);
				contentTypes.put(field, contentType);
			}

			if (cacheEnabled) {
				cache.addContentTypes(type, contentTypes);
			}

		}

		return contentTypes;
	}

	@Override
	public String getFieldContentType(String type, String field) {
		return getFieldContentType(type, null, field);
	}

	@Override
	public String getFieldContentType(String type, String id, String field) {
		String contentType = typeSettings.getFieldString(type, field, KeyWords.CONTENT_TYPE);

		if (contentType == null) {
			String fieldType = getTypeField(type, field).getType();

			switch (fieldType) {
			case PT.BINARY:
				contentType = Format.BINARY.getContentType();
				break;

			case PT.FILE:
			case PT.IMAGE:
			case PT.DOCUMENT:
			case PT.AUDIO:
			case PT.VIDEO:
				if (id != null) {
					contentType = getCompositeFieldContentType(type, id, field);
				}
				break;

			case PT.HTML:
				contentType = Format.XHTML.getContentType();
				break;

			case PT.JSON:
				contentType = Format.JSON.getContentType();
				break;

			case PT.XML:
				contentType = Format.XML.getContentType();
				break;

			default:
				contentType = Format.TEXT.getContentType();
			}
		}

		return contentType;
	}
	
	@Override
	public Object getFieldDefault(String type, String field) {
		return getFieldDefault(type, field, getFieldType(type, field));
	}
	
	protected Object getFieldDefault(String type, String field, String fieldType) {
		Object value = null;
		
		String setting = strings.getFieldString(type, field, KeyWords.DEFAULT);
		
		if (setting == null) {
			setting = typeSettings.getFieldString(type, field, KeyWords.DEFAULT);
		}
		
		if (setting != null) {
			
			if (PT.isBinaryType(fieldType) || PT.isTextType(fieldType)) {
			
				byte [] content = context.getDefault(setting);
			
				if (content != null) {
					
					switch (fieldType) {
					case PT.BINARY:
						value = content;
						break;
						
					case PT.FILE:
						value = new File(content);
						break;
						
					case PT.IMAGE:
						value = new Image(content);
						break;
						
					case PT.DOCUMENT:
						value = new Document(content);
						break;
						
					case PT.AUDIO:
						value = new Audio(content);
						break;
						
					case PT.VIDEO:
						value = new Video(content);
						break;	
					
					case PT.TEXT:
						value = Utils.toString(content);
						break;
						
					case PT.HTML:
						value = new HTMLFragment(content, lang, typeSettings.getFieldString(type, field,
								KeyWords.HTML_ALLOWED_TAGS));
						break;
						
					case PT.JSON:
						value = new JSON(content);
						break;
					
					case PT.XML:
						value = new XML(content, lang, typeSettings.getFieldString(type, field,
								KeyWords.XML_ALLOWED_TAGS));
						break;
					}
				}
			} else {
				switch (fieldType) {
				case PT.INT16:
					value = Tuple.parseInt16(setting);
					break;
					
				case PT.INT32:
					value = Tuple.parseInt32(setting);
					break;
					
				case PT.INT64:
					value = Tuple.parseInt64(setting);
					break;
					
				case PT.FLOAT32:
					value = Tuple.parseFloat32(setting);
					break;
					
				case PT.FLOAT64:
					value = Tuple.parseFloat64(setting);
					break;
					
				case PT.NUMERIC:
					value = Tuple.parseNumeric(setting);
					break;
					
				case PT.BOOLEAN:
					value = Tuple.parseBoolean(setting);
					break;
					
				case PT.COLOR:
					value = Tuple.parseColor(setting);
					break;
					
				case PT.DATE:
					value = Tuple.parseDate(setting);
					break;
					
				case PT.TIME:
					value = Tuple.parseTime(setting);
					break;
					
				case PT.DATETIME:
					value = Tuple.parseDateTime(setting);
					break;
					
				case PT.TIMEZONE:
					value = Tuple.parseTimeZone(setting);
					break;
					
				case PT.EMAIL:
					value = Tuple.parseEmail(setting);
					break;
					
				case PT.URL:
					value = Tuple.parseURL(setting);
					break;
					
				case PT.PASSWORD:
					throw new FieldException(type, field, KeyWords.PASSWORD_FIELD_DEFAULT_VALUE);
							
				default:
					value = setting;	
				}
			}
		}
		
		return value;
	}
	
	@Override
	public FieldRange getFieldRange(String type, String field) {
		return getTypeField(type, field).getRange();
	}
	
	@Override
	public FieldRange getActionFieldRange(String type, String action, String field) {
		throw new NotImplementedException();
	}

	@Override
	public ZonedDateTime getADate(String type) {
		return getTypeDates(type)[1];
	}

	@Override
	public ZonedDateTime getUDate(String type, String id) {
		checkType(type);

		checkId(type, id);

		return getUTCDateTime("select udate from \"" + type + "\" where id=?", id);
	}

	@Override
	public String getETag(String type, String id) {
		return Tuple.parseETag(getUDate(type, id));
	}

	@Override
	public TypesStream backup(boolean full) {
		Filter filter = full ? null : new Filter(KeyWords.BACKUP, Comparison.EQUAL, false, true);

		return new BackupStream(exportTypes(getTypesName(), filter, true));
	}

	@Override
	public TypesStream exportTypes(String[] types, boolean includeObjects) {
		return exportTypes(types, (Filter[]) null, includeObjects);
	}

	@Override
	public TypesStream exportTypes(String[] types, Filter filter, boolean includeObjects) {

		Filter[] filters = filter != null ? new Filter[] { filter } : null;

		return exportTypes(types, filters, includeObjects);
	}

	@Override
	public TypesStream exportTypes(String[] types, Filter[] filters, boolean includeObjects) {

		checkTypes(types);

		TypesStream export = new PostgreSQLTypesStream();

		for (String type : types) {
			Type typeObject = getType(type);
			export.getTypes().put(type, typeObject);

			if (includeObjects) {
				ObjectsStream objects = selectStream(type, null, lang, filters, null, null, true,
						true, false, true, false, false, 0L, 0L);

				if (objects.getCount() > 0) {
					export.getObjects().put(type, objects);
				}
			}
		}

		return export;
	}

	@Override
	public ObjectsStream exportObjects(String type, String[] objects, 
			LinkedHashMap<String, Order> order) {
		
		checkType(type);

		IdFilter filter = null;

		if (objects != null && objects.length > 0) {
			filter = new IdFilter(Comparison.EQUAL, objects);
		}

		return selectStream(type, null, lang, filter, null, order, true, true, false, true,
				false, false, 0L, 0L);
	}

	@Override
	public ImportTypesResult importTypes(InputStream types, ImportAction existingTypesAction,
			ImportAction existingObjectsAction) {
		return importTypes(new TypesStreamDeserializer(types, lang, this, typeSettings, strings), existingTypesAction,
				existingObjectsAction);
	}

	@Override
	public ImportTypesResult importTypes(TypesStream types, ImportAction existingTypesAction,
			ImportAction existingObjectsAction) {
		cacheEnabled = false;

		ImportTypesResult result = new ImportTypesResult();

		try (TypesStream t = types) {
			t.exec();

			for (Map.Entry<String, Type> entry : t.getTypes().entrySet()) {
				String typeName = entry.getKey();

				Type type = entry.getValue();
				Checks.checkType(type);

				if (existsType(typeName)) {
					if (ImportAction.IGNORE.equals(existingTypesAction)) {
						result.addIgnoredType(typeName);
					} else if (ImportAction.ALTER.equals(existingTypesAction)) {
						AlterResult alterResult = alter(type, null, false);
						if (alterResult.isAltered()) {
							result.addAlteredType(typeName, alterResult);
						} else {
							result.addIgnoredType(typeName);
						}
					} else {
						throw new TypeException(typeName, KeyWords.TYPE_ALREADY_EXISTS);
					}
				} else {
					create(type, false);
					result.addImportedType(typeName);
				}
			}

			setDeferredConstraints(true);

			for (String importedType : result.getImportedTypes()) {
				for (Map.Entry<String, TypeField> entry : t.getTypes().get(importedType).getFields().entrySet()) {
					String fieldType = entry.getValue().getType();
					if (!PT.isPrimitiveType(fieldType)) {
						addReference(importedType, entry.getKey(), fieldType);
					}
				}
			}

			for (Map.Entry<String, AlterResult> entry : result.getAlteredTypes().entrySet()) {
				String alteredType = entry.getKey();

				for (String addedField : entry.getValue().getAddedFields()) {
					String addedFieldType = t.getTypes().get(entry.getKey()).getFields().get(addedField).getType();
					if (!PT.isPrimitiveType(addedFieldType)) {
						addReference(alteredType, addedField, addedFieldType);
					}
				}

				for (Map.Entry<String, AlterFieldResult> alteredField : entry.getValue().getAlteredFields()
						.entrySet()) {
					String alteredFieldName = alteredField.getKey();
					AlterFieldResult alteredFieldResult = alteredField.getValue();

					if (alteredFieldResult.isTypeAltered()) {
						String alteredFieldType = t.getTypes().get(entry.getKey()).getFields().get(alteredFieldName)
								.getType();
						if (!PT.isPrimitiveType(alteredFieldType)) {
							addReference(alteredType, alteredFieldName, alteredFieldType);
						}
					}
				}
			}

			while (t.next()) {
				result.addResult(importObjects(t.getObjectsStream(), existingObjectsAction, false,
						result.getImportedTypes()));
			}

			checkMissingReferences(result.getImportedObjects());

			setDeferredConstraints(false);
		}

		return result;
	}

	@Override
	public ImportObjectsResult importObjects(InputStream objects, ImportAction existingObjectsAction) {
		return importObjects(new ObjectsStreamDeserializer(objects, lang, false, this, typeSettings),
				existingObjectsAction);
	}

	protected ImportObjectsResult importObjects(InputStream objects, ImportAction existingObjectsAction,
			boolean deferredConstraints) {
		return importObjects(new ObjectsStreamDeserializer(objects, lang, false, this, typeSettings),
				existingObjectsAction, deferredConstraints, null);
	}

	@Override
	public ImportObjectsResult importObjects(ObjectsStream objects, ImportAction existingObjectsAction) {
		return importObjects(objects, existingObjectsAction, true, null);
	}

	protected ImportObjectsResult importObjects(ObjectsStream objects, ImportAction existingObjectsAction,
			boolean deferredConstraints, ArrayList<String> importedTypes) {

		ImportObjectsResult result = new ImportObjectsResult();

		if (deferredConstraints) {
			setDeferredConstraints(true);
		}

		try (ObjectsStream o = objects) {
			o.exec();

			while (o.next()) {
				NXObject item = o.getItem();
				Checks.checkObject(item);

				String type = item.getType();
				String id = item.getId();

				boolean importedType = importedTypes != null && importedTypes.contains(type);

				if (!importedType && existsObject(type, id)) {
					if (ImportAction.IGNORE.equals(existingObjectsAction)) {
						result.addIgnoredObject(type, id);
					} else if (ImportAction.UPDATE.equals(existingObjectsAction)) {
						update(item, null, false);
						result.addUpdatedObject(type, id);
					} else {
						throw new ObjectException(type, id, KeyWords.OBJECT_ALREADY_EXISTS);
					}
				} else {
					insert(item, false);
					result.addImportedObject(type, id);
				}
			}
		}

		if (deferredConstraints) {
			checkMissingReferences(result.getImportedObjects());
			setDeferredConstraints(false);
		}

		return result;
	}

	@Override
	public Long count(String type) {
		return getInt64("select count(*) from \"" + type + "\"");
	}
	
	@Override
	public Long count(StringBuilder sql, Object... parameters) {
		return count(sql.toString(), parameters);
	}
	
	@Override
	public Long count(StringBuilder sql, ArrayList<Object> parameters) {
		return count(sql.toString(), parameters.toArray());
	}

	@Override
	public Long count(String sql, Object... parameters) {
		return getInt64("select count(*) from (" + sql + ") as count", parameters);
	}

	@Override
	public boolean hasObjects(String type) {
		return getBoolean("select exists(select 1 from \"" + type + "\")");
	}

	@Override
	public boolean hasNullValues(String type, String field) {
		return getBoolean("select exists(select 1 from \"" + type + "\" where \"" + field + "\" is null)");
	}

	
	@Override
	public int execute(String sql, Object... parameters) {
		return execute(sql, false, null, parameters);
	}
	
	@Override
	public int execute(StringBuilder sql, Object... parameters) {
		return execute(sql.toString(), parameters);
	}
	
	@Override
	public int execute(StringBuilder sql, ArrayList<Object> parameters) {
		return execute(sql.toString(), parameters.toArray());
	}

	@Override
	public int execute(String sql, Integer expectedRows, Object... parameters) {
		return execute(sql, true, expectedRows, parameters);
	}
	
	@Override
	public int execute(StringBuilder sql, Integer expectedRows, Object... parameters) {
		return execute(sql.toString(), expectedRows, parameters);
	}
	
	@Override
	public int execute(StringBuilder sql, Integer expectedRows, ArrayList<Object> parameters) {
		return execute(sql.toString(), expectedRows, parameters.toArray());
	}
	
	@Override
	public int execute(StringBuilder sql, boolean useSavepoint, Integer expectedRows,
			Object... parameters) {
		return execute(sql.toString(), useSavepoint, expectedRows, parameters);
	}
	
	@Override
	public int execute(StringBuilder sql, boolean useSavepoint, Integer expectedRows,
			ArrayList<Object> parameters) {
		return execute(sql.toString(), useSavepoint, expectedRows, parameters.toArray());
	}

	@Override
	public int execute(String sql, boolean useSavepoint, Integer expectedRows, Object... parameters) {
		log(sql);

		int rows = 0;

		SQLParameters sqlParameters = sqlPreprocessor(sql, parameters);

		Savepoint savepoint = null;
		if (useSavepoint) {
			savepoint = setSavepoint();
		}

		try (PreparedStatement statement = connection.prepareStatement(sqlParameters.sql)) {

			setParameters(statement, sqlParameters.parameters);

			rows = statement.executeUpdate();

			if (expectedRows != null && rows != expectedRows) {
				throw new InvalidValueException(KeyWords.INVALID_ROW_COUNT, rows);
			}

		} catch (Exception e) {
			if (useSavepoint) {
				rollback(savepoint);
			}
			throwException(e);
		}

		return rows;
	}

	@Override
	public Short getInt16(String sql, Object... parameters) {
		return (Short) getObject(sql, parameters);
	}

	@Override
	public Integer getInt32(String sql, Object... parameters) {
		return (Integer) getObject(sql, parameters);
	}

	@Override
	public Long getInt64(String sql, Object... parameters) {
		return (Long) getObject(sql, parameters);
	}

	@Override
	public Float getFloat32(String sql, Object... parameters) {
		return (Float) getObject(sql, parameters);
	}

	@Override
	public Double getFloat64(String sql, Object... parameters) {
		return (Double) getObject(sql, parameters);
	}

	@Override
	public BigDecimal getNumeric(String sql, Object... parameters) {
		return (BigDecimal) getObject(sql, parameters);
	}

	@Override
	public Boolean getBoolean(String sql, Object... parameters) {
		return Tuple.parseBoolean(getObject(sql, parameters));
	}

	@Override
	public String getString(String sql, Object... parameters) {
		return (String) getObject(sql, parameters);
	}

	@Override
	public String getText(String sql, Object... parameters) {
		return (String) getObject(sql, parameters);
	}

	@Override
	public LocalDate getDate(String sql, Object... parameters) {
		return Tuple.parseDate(getObject(sql, parameters));
	}

	@Override
	public LocalTime getTime(String sql, Object... parameters) {
		return Tuple.parseTime(getObject(sql, parameters));
	}

	@Override
	public LocalDateTime getDateTime(String sql, Object... parameters) {
		return Tuple.parseDateTime(getObject(sql, parameters));
	}

	@Override
	public byte[] getBinary(String sql, Object... parameters) {
		return (byte[]) getObject(sql, parameters);
	}

	@Override
	public HTMLFragment getHTML(String sql, String allowedTags, Object... parameters) {
		return Tuple.parseHTML(getObject(sql, parameters), lang, allowedTags);
	}

	@Override
	public URL getURL(String sql, Object... parameters) {
		return Tuple.parseURL(getObject(sql, parameters));
	}

	@Override
	public InternetAddress getEmail(String sql, Object... parameters) {
		return Tuple.parseEmail(getObject(sql, parameters));
	}

	@Override
	public String getTel(String sql, Object... parameters) {
		return (String) getObject(sql, parameters);
	}

	@Override
	public ZoneId getTimeZone(String sql, Object... parameters) {
		return Tuple.parseTimeZone(getObject(sql, parameters));
	}

	@Override
	public Color getColor(String sql, Object... parameters) {
		return Tuple.parseColor(getObject(sql, parameters));
	}

	@Override
	public Image getImage(String sql, Object... parameters) {
		return Tuple.parseImage(getObject(sql, parameters));
	}

	@Override
	public Document getDocument(String sql, Object... parameters) {
		return Tuple.parseDocument(getObject(sql, parameters));
	}

	@Override
	public ZonedDateTime getUTCDateTime(String sql, Object... parameters) {
		return Tuple.parseUTCDateTime(getObject(sql, parameters));
	}

	@Override
	public Object getObject(String sql, Object... parameters) {
		Object[] objects = getArray(sql, Object.class, parameters);
		return objects.length == 1 ? objects[0] : null;
	}

	@Override
	public Short[] getInt16Array(String sql, Object... parameters) {
		return getArray(sql, Short.class, parameters);
	}

	@Override
	public Integer[] getInt32Array(String sql, Object... parameters) {
		return getArray(sql, Integer.class, parameters);
	}

	@Override
	public Long[] getInt64Array(String sql, Object... parameters) {
		return getArray(sql, Long.class, parameters);
	}

	@Override
	public Float[] getFloat32Array(String sql, Object... parameters) {
		return getArray(sql, Float.class, parameters);
	}

	@Override
	public Double[] getFloat64Array(String sql, Object... parameters) {
		return getArray(sql, Double.class, parameters);
	}

	@Override
	public BigDecimal[] getNumericArray(String sql, Object... parameters) {
		return getArray(sql, BigDecimal.class, parameters);
	}

	@Override
	public Boolean[] getBooleanArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters)).map(value -> Tuple.parseBoolean(value))
				.toArray(Boolean[]::new);
	}

	@Override
	public String[] getStringArray(String sql, Object... parameters) {
		return getArray(sql, String.class, parameters);
	}

	@Override
	public String[] getTextArray(String sql, Object... parameters) {
		return getArray(sql, String.class, parameters);
	}

	@Override
	public LocalDate[] getDateArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters)).map(date -> Tuple.parseDate(date))
				.toArray(LocalDate[]::new);
	}

	@Override
	public LocalTime[] getTimeArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters)).map(time -> Tuple.parseTime(time))
				.toArray(LocalTime[]::new);
	}

	@Override
	public LocalDateTime[] getDateTimeArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters)).map(dateTime -> Tuple.parseDateTime(dateTime))
				.toArray(LocalDateTime[]::new);
	}

	@Override
	public ZonedDateTime[] getUTCDateTimeArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters))
				.map(utcDateTime -> Tuple.parseUTCDateTime(utcDateTime)).toArray(ZonedDateTime[]::new);
	}

	@Override
	public byte[][] getBinaryArray(String sql, Object... parameters) {
		return getArray(sql, byte[].class, parameters);
	}

	@Override
	public HTMLFragment[] getHTMLArray(String sql, String allowedTags, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters))
				.map(html -> Tuple.parseHTML(html, lang, allowedTags)).toArray(HTMLFragment[]::new);
	}

	@Override
	public URL[] getURLArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters)).map(url -> Tuple.parseURL(url))
				.toArray(URL[]::new);
	}

	@Override
	public InternetAddress[] getEmailArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters)).map(email -> Tuple.parseEmail(email))
				.toArray(InternetAddress[]::new);
	}

	@Override
	public String[] getTelArray(String sql, Object... parameters) {
		return getArray(sql, String.class, parameters);
	}

	@Override
	public ZoneId[] getTimeZoneArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters)).map(timeZone -> Tuple.parseTimeZone(timeZone))
				.toArray(ZoneId[]::new);
	}

	@Override
	public Color[] getColorArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters)).map(color -> Tuple.parseColor(color))
				.toArray(Color[]::new);
	}

	@Override
	public Image[] getImageArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters)).map(image -> Tuple.parseImage(image))
				.toArray(Image[]::new);
	}

	@Override
	public Document[] getDocumentArray(String sql, Object... parameters) {
		return Arrays.stream(getArray(sql, Object.class, parameters)).map(document -> Tuple.parseDocument(document))
				.toArray(Document[]::new);
	}

	@Override
	public <T> T[] getArray(String sql, Class<T> type, Object... parameters) {
		log(sql);

		ArrayList<T> objects = null;

		SQLParameters sqlParameters = sqlPreprocessor(sql, parameters);

		try (PreparedStatement statement = connection.prepareStatement(sqlParameters.sql)) {
			setParameters(statement, sqlParameters.parameters);

			objects = new ArrayList<>();

			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					objects.add((T) result.getObject(1));
				}
			}

		} catch (SQLException e) {
			throwException(e);
		}

		return objects.toArray((T[]) Array.newInstance(type, 0));
	}

	@Override
	public Tuple getTuple(String sql, Object... parameters) {
		Tuple[] tuples = query(sql, parameters);
		return tuples.length == 1 ? tuples[0] : null;
	}
	
	@Override
	public Tuple getTuple(StringBuilder sql, Object... parameters) {
		return getTuple(sql.toString(), parameters);
	}
	
	@Override
	public Tuple getTuple(StringBuilder sql, ArrayList<Object> parameters) {
		return getTuple(sql.toString(), parameters.toArray());
	}

	@Override
	public Matrix getMatrix(String sql, String[] axes, Object... parameters) {
		return new Matrix(query(sql, parameters), axes);
	}
	
	@Override
	public Tuple[] query(StringBuilder sql, ArrayList<Object> parameters) {
		return query(sql.toString(), parameters.toArray());
	}
	
	@Override
	public Tuple[] query(StringBuilder sql, Object... parameters) {
		return query(sql.toString(), parameters);
	}

	@Override
	public Tuple[] query(String sql, Object... parameters) {
		log(sql);

		ArrayList<Tuple> tuples = null;

		SQLParameters sqlParameters = sqlPreprocessor(sql, parameters);

		try (PreparedStatement statement = connection.prepareStatement(sqlParameters.sql)) {
			setParameters(statement, sqlParameters.parameters);

			try (ResultSet result = statement.executeQuery()) {
				ResultSetMetaData metaData = result.getMetaData();
				tuples = new ArrayList<>();

				while (result.next()) {
					tuples.add(getTuple(result, metaData));
				}
			}
		} catch (SQLException e) {
			throwException(e);
		}

		return tuples.toArray(new Tuple[] {});
	}

	@Override
	public <T> T[] query(String sql, Class<T> type, Object... parameters) {
		log(sql);

		ArrayList<T> objects = null;

		SQLParameters sqlParameters = sqlPreprocessor(sql, parameters);

		try (PreparedStatement statement = connection.prepareStatement(sqlParameters.sql)) {
			setParameters(statement, sqlParameters.parameters);

			try (ResultSet result = statement.executeQuery()) {
				ResultSetMetaData metaData = result.getMetaData();
				ArrayList<Class> fieldTypes = new ArrayList<>();
				for (int y = 1; y <= metaData.getColumnCount(); y++) {
					fieldTypes.add(Class.forName(metaData.getColumnClassName(y)));
				}
				Constructor<T> constructor = type.getDeclaredConstructor(fieldTypes.toArray(new Class[] {}));

				objects = new ArrayList<>();
				while (result.next()) {
					objects.add(getClassObject(result, metaData, constructor));
				}
			}
		} catch (SQLException | ClassNotFoundException | NoSuchMethodException e) {
			throwException(e);
		}

		return objects.toArray((T[]) Array.newInstance(type, 0));
	}

	protected Tuple getTuple(ResultSet result, ResultSetMetaData metaData) {
		Tuple tuple = null;

		try {
			tuple = new Tuple();
			for (int y = 1; y <= metaData.getColumnCount(); y++) {
				tuple.put(metaData.getColumnLabel(y), result.getObject(y));
			}
		} catch (SQLException e) {
			throwException(e);
		}

		return tuple;
	}

	protected <T> T getClassObject(ResultSet result, ResultSetMetaData metaData, Constructor<T> constructor) {
		T classObject = null;

		try {
			ArrayList<Object> fields = new ArrayList<>();

			for (int y = 1; y <= metaData.getColumnCount(); y++) {
				fields.add(result.getObject(y));
			}

			classObject = constructor.newInstance(fields.toArray());

		} catch (SQLException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			throwException(e);
		}

		return classObject;
	}

	@Override
	public void setDeferredConstraints(boolean status) {
		String value = status ? "deferred" : "immediate";
		execute("set constraints all " + value);
	}

	@Override
	public Savepoint setSavepoint() {
		Savepoint savepoint = null;

		try {
			savepoint = connection.setSavepoint();
		} catch (SQLException e) {
			throwException(e);
		}

		return savepoint;
	}

	@Override
	public void rollback() {
		try {
			connection.rollback();
		} catch (SQLException e) {
			throwException(e);
		}
	}

	@Override
	public void rollback(Savepoint savepoint) {
		try {
			connection.rollback(savepoint);
		} catch (SQLException e) {
			throwException(e);
		}
	}

	@Override
	public void commit() {
		try {
			if (!cacheEnabled) {
				synchronized (cache) {

					cache.clear();
					connection.commit();
					cacheEnabled = true;

				}
			} else {
				connection.commit();
			}
		} catch (SQLException e) {
			throwException(e);
		}
	}

	@Override
	public void close() {

		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			throwException(e);
		}

	}

	@Override
	public ZonedDateTime update(String type, String id, byte[] data) {
		throw new NotImplementedException();
	}

	protected SQLParameters sqlPreprocessor(String sql, Object[] parameters) {
		if (parameters != null) {
			StringBuilder newSQL = new StringBuilder();
			ArrayList<Object> newParameters = new ArrayList<>();

			StringReader reader = new StringReader(sql);
			int x = 0;
			int c;

			try {
				while ((c = reader.read()) != -1) {
					if (c == '#') {
						Object parameter = parameters[x];
						if (parameter instanceof String[]) {
							for (String name : (String[]) parameter) {
								Checks.checkTypeOrField(name);
								newSQL.append("\"" + name + "\",");
							}
							newSQL.deleteCharAt(newSQL.length() - 1);
						} else if (parameter instanceof String) {
							Checks.checkTypeOrField((String) parameter);
							newSQL.append("\"" + parameter + "\"");
						} else {
							throw new InvalidValueException(KeyWords.INVALID_TYPE_OR_FIELD_NAME, parameter);
						}
						x++;
					} else if (c == '?') {
						reader.mark(1);
						c = reader.read();
						if (c == '?') {
							newSQL.append("??");
						} else {
							reader.reset();
							Object parameter = parameters[x];
							if (parameter instanceof Object[]) {
								newSQL.append(StringUtils.repeat("?,", ((Object[]) parameter).length));
								newSQL.deleteCharAt(newSQL.length() - 1);
								newParameters.addAll(Arrays.asList((Object[]) parameter));
							} else {
								newSQL.append("?");
								newParameters.add(parameter);
							}
							x++;
						}
					} else if (c == '\'') {
						newSQL.append("'");

						while ((c = reader.read()) != -1) {
							newSQL.append((char) c);

							if (c == '\'') {
								reader.mark(1);
								c = reader.read();

								if (c == '\'') {
									newSQL.append("'");
								} else {
									reader.reset();
									break;
								}
							}
						}
					} else {
						newSQL.append((char) c);
					}
				}
			} catch (Exception e) {
				throwException(e);
			}

			sql = newSQL.toString();
			parameters = newParameters.toArray();
		}

		return new SQLParameters(sql, parameters);
	}

	protected class SQLParameters {
		protected String sql;
		protected Object[] parameters;

		protected SQLParameters(String sql, Object[] parameters) {
			this.sql = sql;
			this.parameters = parameters;
		}
	}

	protected void setParameters(PreparedStatement statement, Object[] parameters) throws SQLException {
		if (parameters != null) {
			for (int x = 0; x < parameters.length; x++) {
				Object object = parameters[x];

				if (object instanceof URL || object instanceof InternetAddress || object instanceof HTMLFragment
						|| object instanceof Color || object instanceof ZoneId) {
					object = object.toString();
				} else if (object instanceof LocalTime) {
					object = Time.valueOf((LocalTime) object);
				} else if (object instanceof LocalDate) {
					object = Date.valueOf((LocalDate) object);
				} else if (object instanceof LocalDateTime) {
					object = Timestamp.valueOf((LocalDateTime) object);
				} else if (object instanceof ZonedDateTime) {
					object = Timestamp.valueOf(((ZonedDateTime) object).toLocalDateTime());
				} 

				statement.setObject(x + 1, object);
			}
		}
	}

	@Override
	public ActionResult executeAction(String type, String id, String action, Object... parameters) {
		throw new NotImplementedException();
	}

	@Override
	public ActionResult executeAction(String type, String[] objects, String action, Object... parameters) {
		throw new NotImplementedException();
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, TypeField>> getTypeActions(String type) {
		throw new NotImplementedException();
	}

	protected class PostgreSQLObjectsStream implements ObjectsStream {
		protected String type;
		protected LinkedHashMap<String, TypeField> typeFields;
		protected boolean fulltext;
		protected boolean binary;
		protected boolean documentPreview;
		protected boolean objectsName;
		protected boolean referencesName;
		protected Long count;
		protected TuplesStream tuples;

		protected PostgreSQLObjectsStream() {
			count = 0L;
		}
		
		protected PostgreSQLObjectsStream(String type, LinkedHashMap<String, TypeField> typeFields,
				boolean fulltext, boolean binary, boolean documentPreview, boolean objectsName,
				boolean referencesName, Long count, TuplesStream tuples) {
			
			this.type = type;
			this.typeFields = typeFields;
			this.fulltext = fulltext;
			this.binary = binary;
			this.documentPreview = documentPreview;
			this.objectsName = objectsName;
			this.referencesName = referencesName;
			this.count = count;
			this.tuples = tuples;
		}
		
		@Override
		public String getFormat() {
			return NEXTTYPES_OBJECTS;
		}
		
		@Override
		public String getVersion() {
			return Constants.VERSION;
		}

		@Override
		public void exec() {
			tuples.exec();
		}

		@Override
		public boolean next() {
			return tuples.next();
		}

		@Override
		public NXObject getItem() {
			return PostgreSQLNode.this.getObject(type, typeFields, fulltext, binary, documentPreview,
					objectsName, referencesName, tuples.getTuple());
		}

		@Override
		public Long getCount() {
			return count;
		}

		@Override
		public void close() {
			tuples.close();
		}
	}

	protected class PostgreSQLTuplesStream implements TuplesStream {
		protected String sql;
		protected Object[] parameters;
		protected PreparedStatement statement;
		protected ResultSet result;
		protected ResultSetMetaData metaData;

		protected PostgreSQLTuplesStream(String sql, Object... parameters) {
			this.sql = sql;
			this.parameters = parameters;
		}
		
		@Override
		public String getFormat() {
			return NEXTTYPES_TUPLES;
		}
		
		@Override
		public String getVersion() {
			return Constants.VERSION;
		}

		@Override
		public void exec() {
			SQLParameters sqlParameters = sqlPreprocessor(sql, parameters);

			log(sqlParameters.sql);

			try {
				statement = connection.prepareStatement(sqlParameters.sql, ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				statement.setFetchSize(10);
				setParameters(statement, sqlParameters.parameters);
				result = statement.executeQuery();
				metaData = result.getMetaData();
			} catch (SQLException e) {
				throwException(e);
			}
		}

		@Override
		public boolean next() {
			boolean next = false;

			try {
				next = result.next();
			} catch (SQLException e) {
				throwException(e);
			}

			return next;
		}

		@Override
		public Tuple getTuple() {
			return PostgreSQLNode.this.getTuple(result, metaData);
		}

		@Override
		public void close() {
			try {
				result.close();
			} catch (SQLException e) {
			}

			try {
				statement.close();
			} catch (SQLException e) {
			}
		}
	}

	protected class BackupStream extends PostgreSQLTypesStream {
		protected TypesStream stream;

		protected BackupStream(TypesStream stream) {
			this.stream = stream;
		}

		@Override
		public LinkedHashMap<String, Type> getTypes() {
			return stream.getTypes();
		}

		@Override
		public LinkedHashMap<String, ObjectsStream> getObjects() {
			return stream.getObjects();
		}

		@Override
		public ZonedDateTime getDate() {
			return stream.getDate();
		}

		@Override
		public void exec() {
			stream.exec();
		}

		@Override
		public boolean next() {
			return stream.next();
		}

		@Override
		public ObjectsStream getObjectsStream() {
			return stream.getObjectsStream();
		}

		@Override
		public void close() {
			for (String type : getTypes().keySet()) {
				execute("update \"" + type + "\" set backup = true where backup = false");
			}

			stream.close();
		}
	}

	protected class PostgreSQLTypesStream implements TypesStream {
		protected LinkedHashMap<String, Type> types = new LinkedHashMap<>();
		protected LinkedHashMap<String, ObjectsStream> objects = new LinkedHashMap<>();
		protected ZonedDateTime date = ZonedDateTime.now(ZoneOffset.UTC);

		@Override
		public String getFormat() {
			return NEXTTYPES_TYPES;
		}
		
		@Override
		public String getVersion() {
			return Constants.VERSION;
		}
		
		@Override
		public LinkedHashMap<String, Type> getTypes() {
			return types;
		}

		@Override
		public LinkedHashMap<String, ObjectsStream> getObjects() {
			return objects;
		}

		@Override
		public ZonedDateTime getDate() {
			return date;
		}

		@Override
		public void exec() {

		}

		@Override
		public boolean next() {
			return false;
		}

		@Override
		public ObjectsStream getObjectsStream() {
			throw new NotImplementedException();
		}

		@Override
		public void close() {

		}
	}

	protected class SelectQuery {
		protected StringBuilder sql;
		protected ArrayList<Object> parameters;
		protected LinkedHashMap<String, TypeField> typeFields;
		protected Long count;
		protected Long offset;
		protected Long limit;
		protected Long minLimit;
		protected Long maxLimit;
		protected Long limitIncrement;

		protected SelectQuery(String type, StringBuilder sql, ArrayList<Object> parameters, String filters,
				String order) {
			this.sql = sql;
			this.parameters = parameters;

			StringBuilder whereSQL = new StringBuilder();

			if (filters != null && filters.length() > 0) {
				whereSQL.append(" where " + filters);
			}

			addTypeFilters(type, whereSQL);

			sql.append(whereSQL);
			
			count = count(sql, parameters);
			
			if (count > 0) { 

				if (order != null) {
					sql.append(" order by " + order);
				}
			}
		}

		protected SelectQuery(String type, StringBuilder sql, ArrayList<Object> parameters, String filters,
				String search, String[] searchFields, String order, Long offset, Long limit) {

			this.sql = sql;
			this.parameters = parameters;

			StringBuilder whereSQL = new StringBuilder();

			if (filters != null && filters.length() > 0) {
				whereSQL.append(" where " + filters);
			}

			if (search != null && search.length() > 0 && searchFields != null && searchFields.length > 0) {
				if (whereSQL.length() == 0) {
					whereSQL.append(" where ");
				} else {
					whereSQL.append(" and ");
				}

				addSearch(whereSQL, search, searchFields);
			}

			addTypeFilters(type, whereSQL);

			sql.append(whereSQL);
			
			count = count(sql, parameters);

			if (count > 0) {
				if (order != null) {
					sql.append(" order by " + order);
				}

				sql.append(" " + offsetLimitSQL(type, offset, limit));
			}
		}

		protected SelectQuery(String type, String[] fields, String lang, Filter[] filters, String search,
				LinkedHashMap<String, Order> order, boolean fulltext, boolean binary, boolean documentPreview,
				boolean password, boolean objectsName, boolean referencesName, Long offset, Long limit) {

			StringBuilder fieldsSQL = new StringBuilder();
			StringBuilder joinSQL = new StringBuilder();
			StringBuilder whereSQL = new StringBuilder();
			parameters = new ArrayList<>();
			typeFields = PostgreSQLNode.this.getTypeFields(type, fields);
			LinkedHashMap<String, TypeIndex> typeIndexes = getTypeIndexes(type);

			if (objectsName) {
				String idName = typeSettings.gts(type, KeyWords.ID_NAME);
				if (idName != null) {
					fieldsSQL.append(", \"@@" + type + "\".name as \"@name\"");
					joinSQL.append(" join (" + idName + ") as \"@@" + type + "\"" + " on \""
							+ type + "\".id=\"@@" + type + "\".id");
					
					if (idName.contains("?")) {
						parameters.add(lang);
					}
				} else {
					fieldsSQL.append(", \"" + type + "\".id as \"@name\"");
				}
			}
			
			if (filters != null) {
				for (Filter filter : filters) {
					if (!filter.include()) {
						typeFields.remove(filter.getField());
					}
				}
			}

			for (Map.Entry<String, TypeField> entry : typeFields.entrySet()) {
				String field = entry.getKey();
				TypeField typeField = entry.getValue();

				switch (typeField.getType()) {
				case PT.BINARY:
					if (binary) {
						fieldsSQL.append(", \"" + type + "\".\"" + field + "\"");
					} else {
						fieldsSQL.append(", octet_length(\"" + type + "\".\"" + field + "\") as \""
								+ field + "\"");
					}
					break;
				case PT.FILE:
				case PT.IMAGE:
				case PT.AUDIO:
				case PT.VIDEO:
					if (binary) {
						fieldsSQL.append(", \"" + type + "\".\"" + field + "\"");
					} else {
						fieldsSQL.append(", octet_length((\"" + type + "\".\"" + field
								+ "\").content) as \"" + field + "\"");
					}
					break;
				case PT.DOCUMENT:
					if (binary) {
						fieldsSQL.append(", \"" + type + "\".\"" + field + "\"");
					} else {
						fieldsSQL.append(", octet_length((\"" + type + "\".\"" + field 
								+ "\").content) as \"@" + field + "_size\"");

						if (documentPreview) {
							if (fulltext) {
								fieldsSQL.append(", (\"" + type + "\".\"" + field + "\").text as \"@"
										+ field + "_text\"");
							} else {
								fieldsSQL.append(", left((\"" + type + "\".\"" + field
										+ "\").text, 200) as \"@" + field + "_text\"");
							}
						}
					}
					break;
				case PT.PASSWORD:
					if (password) {
						fieldsSQL.append(", \"" + type + "\".\"" + field + "\"");
					} else {
						fieldsSQL.append(", '" + Security.HIDDEN_PASSWORD + "' as \"" + field + "\"");
					}
					break;
				case PT.TEXT:
				case PT.HTML:
				case PT.XML:
				case PT.JSON:
					if (fulltext) {
						fieldsSQL.append(", \"" + type + "\".\"" + field + "\"");
					} else {
						fieldsSQL.append(", left(\"" + type + "\".\"" + field + "\"::text, 200) as \""
								+ field + "\"");
					}
					break;
				case PT.INT16:
				case PT.INT32:
				case PT.INT64:
				case PT.FLOAT32:
				case PT.FLOAT64:
				case PT.NUMERIC:
				case PT.BOOLEAN:
				case PT.STRING:
				case PT.TEL:
				case PT.URL:
				case PT.EMAIL:
				case PT.DATE:
				case PT.TIME:
				case PT.DATETIME:
				case PT.TIMEZONE:
				case PT.COLOR:
					fieldsSQL.append(", \"" + type + "\".\"" + field + "\"");
					break;
				default:
					if (referencesName) {
						addReference(fieldsSQL, joinSQL, type, field, typeField.getType(), lang);
					} else {
						fieldsSQL.append(", \"" + type + "\".\"" + field + "\"");
					}
					break;
				}
			}

			addFilters(type, whereSQL, filters);

			if (search != null) {
				ArrayList<TypeIndex> fulltextIndexes = new ArrayList<>();
				for (Map.Entry<String, TypeIndex> entry : typeIndexes.entrySet()) {
					TypeIndex typeIndex = entry.getValue();

					if (typeIndex.getMode().equals(IndexMode.FULLTEXT)) {
						fulltextIndexes.add(typeIndex);
					}
				}

				if (fulltextIndexes.size() > 0) {
					if (whereSQL.length() == 0) {
						whereSQL.append(" where (");
					} else {
						whereSQL.append(" and (");
					}

					for (TypeIndex index : fulltextIndexes) {
						addSearch(type, whereSQL, search, index);
					}

					whereSQL.delete(whereSQL.length() - 4, whereSQL.length()).append(")");
				} else {
					throw new FulltextIndexNotFoundException();
				}
			}

			addTypeFilters(type, whereSQL);

			sql = new StringBuilder("select \"" + type + "\".id,\"" + type + "\".cdate,\"" + type + "\".udate,\"" + type
					+ "\".backup" + fieldsSQL + " from \"" + type + "\"" + joinSQL + whereSQL);

			count = count(sql, parameters);

			if (count > 0) {
			
				sql.append(" order by ");
				if (order != null && order.size() > 0) {
					for (Map.Entry<String, Order> entry : order.entrySet()) {
						String field = entry.getKey();
						String settingsOrder = typeSettings.getFieldString(type, field,
								KeyWords.ORDER);
						
						if (settingsOrder != null) {
							sql.append(settingsOrder);
						} else {
							TypeField typeField = typeFields.get(field);
							
							if (typeField != null && !PT.isPrimitiveType(typeField.getType())) {
								sql.append("\"@" + field + "_name\"");
							} else {
								sql.append("\"" + field + "\"");
							}
						}
						
						Order orderValue = entry.getValue();

						if (orderValue != null) {
							sql.append(" " + orderValue);
						}

						sql.append(",");
					}
					sql.deleteCharAt(sql.length() - 1);
				} else {
					sql.append("id");
				}

				sql.append(" " + offsetLimitSQL(type, offset, limit));
			}
		}

		protected void addTypeFilters(String type, StringBuilder whereSQL) {
			String typeFilters = typeSettings.gts(type, KeyWords.FILTERS);
			if (typeFilters != null) {
				if (whereSQL.length() == 0) {
					whereSQL.append(" where ");
				} else {
					whereSQL.append(" and ");
				}
				whereSQL.append(typeFilters);
			}
		}

		protected void addFilters(String type, StringBuilder whereSQL, Filter[] filters) {
			if (filters != null && filters.length > 0) {
				LinkedHashMap<String, TypeField> typeFields = PostgreSQLNode.this.getTypeFields(type);
				
				whereSQL.append(" where ");

				for (Filter filter : filters) {
					
					String field = filter.getField();
					Comparison comparison = filter.getComparison();
					Object value = filter.getValue();
					TypeField typeField = typeFields.get(field);
					
					if (typeField != null && !PT.isPrimitiveType(typeField.getType())
							&& (Comparison.LIKE.equals(comparison)
									|| Comparison.NOT_LIKE.equals(comparison))) {
						whereSQL.append("\"@" + field + "\".name");
					
					} else {
						whereSQL.append("\"" + type + "\".\"" + field + "\" ");
					} 
					
					switch (comparison) {
					case EQUAL:
						if (value == null) {
							whereSQL.append("is null");
						} else {
							whereSQL.append("in(?)");
						}
						break;
						
					case NOT_EQUAL:
						if (value == null) {
							whereSQL.append("is not null");
						} else {
							whereSQL.append("not in(?)");
						}
						break;
						
					case GREATER:
						whereSQL.append("> ?");
						break;
						
					case GREATER_OR_EQUAL:
						whereSQL.append(">= ?");
						break;
						
					case LESS:
						whereSQL.append("< ?");
						break;
						
					case LESS_OR_EQUAL:
						whereSQL.append("<= ?");
						break;
						
					case LIKE:
						whereSQL.append("::text ilike ?");
						value = "%" + value + "%";
						break;
						
					case NOT_LIKE:
						whereSQL.append("::text not ilike ?");
						value = "%" + value + "%";
						break;
					}
					
					if (value != null) {
						parameters.add(value);
					}

					whereSQL.append(" and ");
				}

				whereSQL.delete(whereSQL.length() - 5, whereSQL.length());
			}
		}

		protected void addSearch(StringBuilder whereSQL, String search, String[] searchFields) {
			String fulltextFields = StringUtils.join(searchFields, FULLTEXT_SEARCH_FIELD_SEPARATOR);
			whereSQL.append("to_tsvector('simple', " + fulltextFields + ") @@ to_tsquery('simple', ?)");
			parameters.add(StringUtils.join(search.split(" "), " & "));
		}

		protected void addSearch(String type, StringBuilder whereSQL, String search, TypeIndex typeIndex) {
			StringBuilder fulltextFields = new StringBuilder();
			LinkedHashMap<String, TypeField> typeFields = PostgreSQLNode.this.getTypeFields(type);

			for (String field : typeIndex.getFields()) {
				TypeField typeField = typeFields.get(field);

				if (typeField != null && PT.DOCUMENT.equals(typeField.getType())) {
					fulltextFields.append("(\"" + type + "\".\"" + field + "\").text");
				} else {
					fulltextFields.append("\"" + type + "\".\"" + field + "\"");
				}

				fulltextFields.append(FULLTEXT_SEARCH_FIELD_SEPARATOR);
			}

			fulltextFields.delete(fulltextFields.length() - FULLTEXT_SEARCH_FIELD_SEPARATOR.length(),
					fulltextFields.length());

			whereSQL.append("to_tsvector('simple', " + fulltextFields + ") @@ to_tsquery('simple', ?)");
			parameters.add(StringUtils.join(search.split(" "), " & "));
			whereSQL.append(" or ");
		}

		protected void addReference(StringBuilder fieldsSQL, StringBuilder joinSQL, String type, String field,
				String fieldType, String lang) {

			String fieldIdName = typeSettings.gts(fieldType, KeyWords.ID_NAME);
			
			if (fieldIdName == null) {
				fieldIdName = "select id, id as name from \"" + fieldType + "\"";
			}

			fieldsSQL.append(", \"@" + field + "\".id as \"@" + field + "_id\","
					+ " \"@" + field + "\".name" + " as \"@" + field + "_name\"");

			joinSQL.append(" left join (select id_name.id, coalesce(id_name.name, id_name.id)"
					+ " as name from (" + fieldIdName + ") as id_name) as \"@" + field + "\""
					+ " on \"" + type + "\".\"" + field + "\"=\"@" + field + "\".id");

			if (fieldIdName.contains("?")) {
				parameters.add(lang);
			}
		}

		protected String offsetLimitSQL(String type, Long offset, Long limit) {
			String offsetLimitSQL = "";

			if (limit == null) {

				limit = Long.valueOf(typeSettings.gts(type, KeyWords.LIMIT));

			}

			if (limit != 0) {

				minLimit = typeSettings.getTypeInt64(type, KeyWords.MIN_LIMIT);
				if (limit < minLimit) {
					limit = minLimit;
				}

				maxLimit = typeSettings.getTypeInt64(type, KeyWords.MAX_LIMIT);
				if (limit > maxLimit) {
					limit = maxLimit;
				}

				limitIncrement = typeSettings.getTypeInt64(type, KeyWords.LIMIT_INCREMENT);

				if (offset != null && offset > 0) {
					long offsets = count / limit;
					long lastOffset = count % limit == 0 ? (offsets - 1) * limit : offsets * limit;

					if (offset > lastOffset) {
						offset = lastOffset;
					}

					long remainder = offset % limit;
					if (remainder != 0) {
						offset -= remainder;
					}

					offsetLimitSQL = "offset " + offset + " limit " + limit;
				} else {
					offsetLimitSQL = "limit " + limit;
				}
			}

			this.offset = offset;
			this.limit = limit;
			
			return offsetLimitSQL;
		}

		public String getSQL() {
			return sql.toString();
		}

		public Long getCount() {
			return count;
		}

		public Long getOffset() {
			return offset;
		}

		public Long getLimit() {
			return limit;
		}

		public Long getMinLimit() {
			return minLimit;
		}

		public Long getMaxLimit() {
			return maxLimit;
		}

		public Long getLimitIncrement() {
			return limitIncrement;
		}

		public Object[] getParameters() {
			return parameters.toArray();
		}

		public LinkedHashMap<String, TypeField> getTypeFields() {
			return typeFields;
		}
	}

	protected void checkType(String type) {
		if (type == null || type.length() == 0) {
			throw new NXException(KeyWords.EMPTY_TYPE_NAME);
		}
	}
	
	protected void checkNewName(String type, String newName) {
		if (newName == null || newName.length() == 0) {
			throw new TypeException(type, KeyWords.EMPTY_NEW_NAME);
		}
	}
		
	protected void checkField(String type, String field) {
		if (field == null || field.length() == 0) {
			throw new TypeException(type, KeyWords.EMPTY_FIELD_NAME);
		}
	}

	protected void checkIndex(String type, String index) {
		if (index == null || index.length() == 0) {
			throw new TypeException(type, KeyWords.EMPTY_INDEX_NAME);
		}
	}

	protected void checkIndexFieldsList(String type, String index, String[] fields) {
		if (fields == null || fields.length == 0) {
			throw new IndexException(type, index, KeyWords.EMPTY_INDEX_FIELDS_LIST);
		}
	}

	protected void checkId(String type, String id) {
		if (id == null || id.length() == 0) {
			throw new NXException(type, KeyWords.EMPTY_ID);
		}
	}

	protected void checkTypes(String[] types) {
		if (types == null || types.length == 0) {
			throw new NXException(KeyWords.EMPTY_TYPES_LIST);
		}
	}
	protected void checkObjects(String type, String[] objects) {
		if (objects == null || objects.length == 0) {
			throw new TypeException(type, KeyWords.EMPTY_OBJECTS_LIST);
		}
	}

	protected void throwException(Exception e) {
		if (e instanceof PSQLException) {
			PSQLException pe = (PSQLException) e;
			String sqlState = pe.getSQLState();
			ServerErrorMessage serverMessage = pe.getServerErrorMessage();
			String message = StringUtils.capitalize(serverMessage.getMessage()) + ".";

			switch (sqlState) {
			case "42P01":
				TypeNotFoundException te = new TypeNotFoundException(null);
				te.setMessage(message);
				throw te;

			case "42703":
				FieldNotFoundException fe = new FieldNotFoundException(null, null);
				fe.setMessage(message);
				throw fe;

			default:
				throw new StringException(message);
			}

		} else if (e instanceof NXException) {
			throw (NXException) e;
		} else {
			throw new NXException(e);
		}
	}

	protected String sqlParameters(String parameters) {
		return parameters != null ? "(" + parameters + ")" : "";
	}

	protected void log(String sql) {
		logger.info(this, auth.getUser(), remoteAddress, sql);
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public LinkedHashMap<String, TypeField> getActionFields(String type, String action) {
		throw new NotImplementedException();
	}
	
	@Override
	public TypeField getActionField(String type, String action, String field) {
		throw new NotImplementedException();
	}

	@Override
	public String getActionFieldType(String type, String action, String field) {
		throw new NotImplementedException();
	}

	@Override
	public Strings getStrings() {
		return strings;
	}

	@Override
	public TypeSettings getTypeSettings() {
		return typeSettings;
	}

	@Override
	public Node getNextNode() {
		return this;
	}
	
	@Override
	public Auth getAuth() {
		return auth;
	}
}