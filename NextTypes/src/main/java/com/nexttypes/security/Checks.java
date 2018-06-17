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

package com.nexttypes.security;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;

import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.Type;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.interfaces.Module;
import com.nexttypes.interfaces.QueryFilter;
import com.nexttypes.system.Constants;

public class Checks {
	public static final Pattern TYPE_FIELD_INDEX_ACTION_CHECK = Pattern.compile("[a-z0-9_]+");
	public static final Pattern ID_ELEMENT_CHECK = Pattern.compile("[a-z0-9\\-.]+");
	public static final Pattern VIEW_CHECK = Pattern.compile("[a-z0-9\\-]+");
	public static final Pattern LANG_CHECK = Pattern.compile("[a-z\\-]+");
	public static final Pattern FIELD_PARAMETERS_CHECK = Pattern.compile("[a-z0-9_\\,]+");
	public static final String[] FIELD_RESERVED_NAMES = { Constants.ID, Constants.CDATE, Constants.UDATE,
			Constants.BACKUP };

	public static void checkString(String value, Pattern check, String setting) {
		if (value != null) {
			Matcher matcher = check.matcher(value);

			if (!matcher.matches()) {
				throwException(setting, value);
			}
		}
	}

	public static void checkMaxLength(String value, int maxLength, String setting) {
		if (value != null && value.length() > maxLength) {
			throwException(setting, value);
		}
	}

	public static void throwException(String setting, String value) {
		throw new InvalidValueException(setting, value);
	}

	public static void checkType(String type) {
		if (Constants.STATIC.equals(type)) {
			throwException(Constants.TYPE_RESERVED_NAME, type);
		}
		
		checkMaxLength(type, Type.MAX_TYPE_NAME_LENGTH, Constants.TYPE_NAME_TOO_LONG);
		checkString(type, TYPE_FIELD_INDEX_ACTION_CHECK, Constants.INVALID_TYPE_NAME);
	}
	
	public static void checkCompositeType(String type) {
		if (ArrayUtils.contains(PT.PRIMITIVE_TYPES, type)) {
			throwException(Constants.PRIMITIVE_TYPE_WITH_THE_SAME_NAME, type);
		}
	}

	public static void checkType(Type type) {
		checkType(type.getName());
		
		checkCompositeType(type.getName());

		for (Entry<String, TypeField> entry : type.getFields().entrySet()) {
			checkField(entry.getKey());
			checkTypeField(entry.getValue());
		}

		for (Entry<String, TypeIndex> entry : type.getIndexes().entrySet()) {
			checkIndex(entry.getKey());
			checkTypeIndex(entry.getValue());
		}
	}

	public static void checkTypeField(TypeField field) {
		checkType(field.getType());
		checkFieldParameters(field.getParameters());
		checkField(field.getOldName());
	}

	public static void checkTypeIndex(TypeIndex index) {
		checkIndexFields(index.getFields());
		checkIndex(index.getOldName());
	}

	public static void checkTypes(String[] types) {
		if (types != null) {
			for (String type : types) {
				checkType(type);
			}
		}
	}

	public static void checkTypes(Type[] types) {
		if (types != null) {
			for (Type type : types) {
				checkType(type);
			}
		}
	}

	public static void checkId(String id) {
		checkMaxLength(id, Type.MAX_ID_LENGTH, Constants.ID_TOO_LONG);
		checkString(id, ID_ELEMENT_CHECK, Constants.INVALID_ID);
	}

	public static void checkObjects(String[] objects) {
		if (objects != null) {
			for (String id : objects) {
				checkId(id);
			}
		}
	}

	public static void checkLang(String lang) {
		checkString(lang, LANG_CHECK, Constants.INVALID_LANG);
	}

	public static void checkField(String field) {
		if (ArrayUtils.contains(FIELD_RESERVED_NAMES, field)) {
			throw new InvalidValueException(Constants.FIELD_RESERVED_NAME, field);
		}

		checkMaxLength(field, Type.MAX_FIELD_NAME_LENGTH, Constants.FIELD_NAME_TOO_LONG);
		checkString(field, TYPE_FIELD_INDEX_ACTION_CHECK, Constants.INVALID_FIELD_NAME);
	}

	public static void checkTypeOrField(String typeOrField) {
		checkMaxLength(typeOrField, Type.MAX_FIELD_NAME_LENGTH, Constants.TYPE_OR_FIELD_NAME_TOO_LONG);
		checkString(typeOrField, TYPE_FIELD_INDEX_ACTION_CHECK, Constants.INVALID_TYPE_OR_FIELD_NAME);
	}

	public static void checkIndex(String index) {
		checkMaxLength(index, Type.MAX_INDEX_NAME_LENGTH, Constants.INDEX_NAME_TOO_LONG);
		checkString(index, TYPE_FIELD_INDEX_ACTION_CHECK, Constants.INVALID_INDEX_NAME);
	}

	public static void checkIndexes(String[] indexes) {
		if (indexes != null) {
			for (String index : indexes) {
				checkIndex(index);
			}
		}
	}

	public static void checkFields(String[] fields) {
		if (fields != null) {
			for (String field : fields) {
				checkField(field);
			}
		}
	}

	public static void checkIndexFields(String[] fields) {
		if (fields != null) {
			for (String field : fields) {
				if (!ArrayUtils.contains(FIELD_RESERVED_NAMES, field)) {
					checkField(field);
				}
			}
		}
	}

	public static void checkFields(LinkedHashMap<String, TypeField> fields) {
		if (fields != null) {
			for (Map.Entry<String, TypeField> entry : fields.entrySet()) {
				checkField(entry.getKey());
				checkType(entry.getValue().getType());
			}
		}
	}

	public static void checkFilters(QueryFilter[] filters) {
		if (filters != null) {
			for (QueryFilter filter : filters) {
				checkFilter(filter);
			}
		}
	}

	public static void checkFilter(QueryFilter filter) {
		if (filter != null) {
			String field = filter.getName();

			if (Constants.ID.equals(field)) {
				Object value = filter.getValue();
				if (value instanceof String) {
					checkId((String) value);
				} else if (value instanceof String[]) {
					checkObjects((String[]) value);
				}
			} else if (!ArrayUtils.contains(FIELD_RESERVED_NAMES, field)) {
				checkField(field);
			}
		}
	}

	public static void checkRef(FieldReference ref) {
		if (ref != null) {
			checkField(ref.getField());
			checkId(ref.getId());
		}
	}

	public static void checkElement(String element) {
		checkString(element, ID_ELEMENT_CHECK, Constants.INVALID_ELEMENT_NAME);
	}

	public static void checkView(String view) {
		checkString(view, VIEW_CHECK, Constants.INVALID_VIEW_NAME);
	}

	public static void checkOrder(LinkedHashMap<String, Order> order) {
		if (order != null) {
			for (Map.Entry<String, Order> entry : order.entrySet()) {
				String field = entry.getKey();
				if (!ArrayUtils.contains(FIELD_RESERVED_NAMES, field)) {
					checkField(field);
				}
			}
		}
	}

	public static void checkAction(String action) {
		checkMaxLength(action, Type.MAX_ACTION_NAME_LENGTH, Constants.ACTION_NAME_TOO_LONG);
		checkString(action, TYPE_FIELD_INDEX_ACTION_CHECK, Constants.INVALID_ACTION_NAME);
	}

	public static void checkFieldParameters(String parameters) {
		checkString(parameters, FIELD_PARAMETERS_CHECK, Constants.INVALID_PARAMETERS);
	}

	public static void checkTupleField(String field) {
		checkMaxLength(field, Type.MAX_FIELD_NAME_LENGTH, Constants.FIELD_NAME_TOO_LONG);
		checkString(field, TYPE_FIELD_INDEX_ACTION_CHECK, Constants.INVALID_FIELD_NAME);
	}

	public static void checkObject(NXObject object) {
		checkId(object.getId());
		checkType(object.getType());

		for (Entry<String, Object> entry : object.getFields().entrySet()) {
			checkField(entry.getKey());
		}
	}

	public static void checkObjects(NXObject[] objects) {
		if (objects != null) {
			for (NXObject object : objects) {
				checkObject(object);
			}
		}
	}

	public static void checkTuple(Tuple tuple) {
		for (Entry<String, Object> entry : tuple.getFields().entrySet()) {
			checkTupleField((String) entry.getKey());
		}
	}

	public static void checkPermissions(String type, String action, JoinPoint joinPoint) {
		Module auth = (Module) joinPoint.getTarget();
		auth.getContext().getPermissionSettings().checkPermissions(type, action, auth.getUser(), auth.getGroups());
	}

	public static void checkPermissions(String[] types, String action, JoinPoint joinPoint) {
		Module auth = (Module) joinPoint.getTarget();
		auth.getContext().getPermissionSettings().checkPermissions(types, action, auth.getUser(), auth.getGroups());
	}

	public static void checkPermissions(String action, JoinPoint joinPoint) {
		Module auth = (Module) joinPoint.getTarget();
		auth.getContext().getPermissionSettings().checkPermissions(action, auth.getUser(), auth.getGroups());
	}

}