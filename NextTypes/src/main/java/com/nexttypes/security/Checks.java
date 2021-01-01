/*
 * Copyright 2015-2021 Alejandro Sánchez <alex@nexttypes.com>
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

import org.aspectj.lang.JoinPoint;

import com.nexttypes.datatypes.ActionReference;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.Type;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.nodes.Node;
import com.nexttypes.settings.Permissions;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Module;

public class Checks {
	public static final Pattern TYPE_FIELD_INDEX_ACTION_CHECK = Pattern.compile("[a-z0-9_]+");
	public static final Pattern ID_ELEMENT_CHECK = Pattern.compile("[a-z0-9\\-.]+");
	public static final Pattern VIEW_CHECK = Pattern.compile("[a-z0-9\\-]+");
	public static final Pattern LANG_CHECK = Pattern.compile("[a-z\\-]+");
	public static final Pattern FIELD_PARAMETERS_CHECK = Pattern.compile("[a-z0-9_\\,]+");
	
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
		if (KeyWords.STATIC.equals(type)) {
			throwException(KeyWords.TYPE_RESERVED_NAME, type);
		}
		
		checkMaxLength(type, Type.MAX_TYPE_NAME_LENGTH, KeyWords.TYPE_NAME_TOO_LONG);
		checkString(type, TYPE_FIELD_INDEX_ACTION_CHECK, KeyWords.INVALID_TYPE_NAME);
	}
	
	public static void checkCompositeType(String type) {
		if (PT.isPrimitiveType(type)) {
			throwException(KeyWords.PRIMITIVE_TYPE_WITH_THE_SAME_NAME, type);
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
		checkMaxLength(id, Type.MAX_ID_LENGTH, KeyWords.ID_TOO_LONG);
		checkString(id, ID_ELEMENT_CHECK, KeyWords.INVALID_ID);
	}

	public static void checkObjects(String[] objects) {
		if (objects != null) {
			for (String id : objects) {
				checkId(id);
			}
		}
	}

	public static void checkLang(String lang) {
		checkString(lang, LANG_CHECK, KeyWords.INVALID_LANG);
	}

	public static void checkField(String field) {
		if (TypeField.isReservedName(field)) {
			throw new InvalidValueException(KeyWords.FIELD_RESERVED_NAME, field);
		}

		checkMaxLength(field, Type.MAX_FIELD_NAME_LENGTH, KeyWords.FIELD_NAME_TOO_LONG);
		checkString(field, TYPE_FIELD_INDEX_ACTION_CHECK, KeyWords.INVALID_FIELD_NAME);
	}

	public static void checkTypeOrField(String typeOrField) {
		checkMaxLength(typeOrField, Type.MAX_FIELD_NAME_LENGTH, KeyWords.TYPE_OR_FIELD_NAME_TOO_LONG);
		checkString(typeOrField, TYPE_FIELD_INDEX_ACTION_CHECK, KeyWords.INVALID_TYPE_OR_FIELD_NAME);
	}

	public static void checkIndex(String index) {
		checkMaxLength(index, Type.MAX_INDEX_NAME_LENGTH, KeyWords.INDEX_NAME_TOO_LONG);
		checkString(index, TYPE_FIELD_INDEX_ACTION_CHECK, KeyWords.INVALID_INDEX_NAME);
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
				if (!TypeField.isReservedName(field)) {
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

	public static void checkFilters(Filter[] filters) {
		if (filters != null) {
			for (Filter filter : filters) {
				checkFilter(filter);
			}
		}
	}

	public static void checkFilter(Filter filter) {
		if (filter != null) {
			String field = filter.getField();

			if (KeyWords.ID.equals(field)) {
				Object value = filter.getValue();
				if (value instanceof String) {
					checkId((String) value);
				} else if (value instanceof String[]) {
					checkObjects((String[]) value);
				}
			} else if (!TypeField.isReservedName(field)) {
				checkField(field);
			}
		}
	}

	public static void checkRef(FieldReference ref) {
		if (ref != null) {
			checkField(ref.getReferencingField());
			checkType(ref.getReferencedType());
			checkId(ref.getReferencedId());
		}
	}
	
	public static void checkARef(ActionReference aref) {
		if (aref != null) {
			checkType(aref.getReferencingType());
			checkAction(aref.getReferencingAction());
			checkField(aref.getReferencingField());
		}
	}

	public static void checkElement(String element) {
		checkString(element, ID_ELEMENT_CHECK, KeyWords.INVALID_ELEMENT_NAME);
	}

	public static void checkView(String view) {
		checkString(view, VIEW_CHECK, KeyWords.INVALID_VIEW_NAME);
	}

	public static void checkOrder(LinkedHashMap<String, Order> order) {
		if (order != null) {
			for (Map.Entry<String, Order> entry : order.entrySet()) {
				String field = entry.getKey();
				if (!TypeField.isReservedName(field)) {
					checkField(field);
				}
			}
		}
	}

	public static void checkAction(String action) {
		checkMaxLength(action, Type.MAX_ACTION_NAME_LENGTH, KeyWords.ACTION_NAME_TOO_LONG);
		checkString(action, TYPE_FIELD_INDEX_ACTION_CHECK, KeyWords.INVALID_ACTION_NAME);
	}

	public static void checkFieldParameters(String parameters) {
		checkString(parameters, FIELD_PARAMETERS_CHECK, KeyWords.INVALID_PARAMETERS);
	}

	public static void checkTupleField(String field) {
		checkMaxLength(field, Type.MAX_FIELD_NAME_LENGTH, KeyWords.FIELD_NAME_TOO_LONG);
		checkString(field, TYPE_FIELD_INDEX_ACTION_CHECK, KeyWords.INVALID_FIELD_NAME);
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
	
	public static void checkPermissions(String action, JoinPoint joinPoint) {
		((Module) joinPoint.getTarget()).getPermissions().checkPermissions(action);
	}

	public static void checkPermissions(String type, String action, JoinPoint joinPoint) {
		((Module) joinPoint.getTarget()).getPermissions(type).checkPermissions(type, action);
	}
	
	public static void checkPermissions(String type, String id, String action, JoinPoint joinPoint) {
		((Module) joinPoint.getTarget()).getPermissions(type).checkPermissions(type, id, action);
	}
	
	public static void checkPermissions(String type, String[] objects, String action, JoinPoint joinPoint) {
		((Module) joinPoint.getTarget()).getPermissions(type).checkPermissions(type, objects, action);
	}

	public static void checkPermissions(String[] types, String action, JoinPoint joinPoint) {
		((Module) joinPoint.getTarget()).getPermissions().checkPermissions(types, action);
	}
	
	public static void checkReferencePermissions(NXObject object, JoinPoint joinPoint) {
		Module module = ((Module) joinPoint.getTarget());
		Node nextNode = module.getNextNode();
		String referencingType = object.getType();
		String referencingId = object.getId();
		Permissions permissions = module.getPermissions(referencingType);
		
		for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
			String referencingField = entry.getKey();
			String referencedType = nextNode.getFieldType(referencingType, referencingField);
			
			if (!PT.isPrimitiveType(referencedType)) {
				String referencedId = (String) entry.getValue();
				
				permissions.checkReferencePermissions(referencingType, referencingId, referencingField,
						referencedType, referencedId);
			}
		}
	}
	
	public static void checkReferencePermissions(String type, String id, String field, Object value,
			JoinPoint joinPoint) {
		Module module = ((Module) joinPoint.getTarget());
		Node nextNode = module.getNextNode();
		
		String referencedType = nextNode.getFieldType(type, field);
		
		if (!PT.isPrimitiveType(referencedType)) {
		
			module.getPermissions(type).checkReferencePermissions(type, id, field, referencedType,
				(String) value);
		}
	}
}