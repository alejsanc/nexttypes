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

package com.nexttypes.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.Type;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.enums.Order;
import com.nexttypes.system.Action;
import com.nexttypes.nodes.Node;
 
public aspect NodeSecurity extends Checks {

    before (Type type) : (execution(* Node.create(..))) && args(type) {
    	checkType(type);
    	checkPermissions(type.getName(), Action.CREATE, thisJoinPoint);
    }

    before (Type type) : (execution(* Node.alter(..))) && args(type, ..) {
    	checkType(type);
    	checkPermissions(type.getName(), Action.ALTER, thisJoinPoint);
    }

    before (String type, String field, TypeField typeField) :
    	(execution(* Node.addField(..))) &&
    	args(type, field, typeField) {
    	
    	checkType(type);
    	checkField(field);
    	checkTypeField(typeField);
    	checkPermissions(type, Action.ADD_FIELD, thisJoinPoint);
    }

    before (String type, String field, TypeField typeField) :
    	execution(* Node.alterField(..)) &&
    	args(type, field, typeField) {
    	
    	checkType(type);
    	checkField(field);
    	checkTypeField(typeField);
    	checkPermissions(type, Action.ALTER_FIELD, thisJoinPoint);
    }

    before (String type, String index, TypeIndex typeIndex) :
    	execution(* Node.addIndex(..)) &&
    	args(type, index, typeIndex) {
    	
    	checkType(type);
    	checkIndex(index);
    	checkTypeIndex(typeIndex);
    	checkPermissions(type, Action.ADD_INDEX, thisJoinPoint);
    }

    before (String type, String index, TypeIndex typeIndex) :
    	execution(* Node.alterIndex(..)) &&
    	args(type, index, typeIndex) {
    	
    	checkType(type);
    	checkIndex(index);
    	checkTypeIndex(typeIndex);
    	checkPermissions(type, Action.ALTER_INDEX, thisJoinPoint);
    }

    before (String type, String newName) :
    	execution(* Node.rename(..)) && args(type, newName) {
    	
    	checkType(type);
    	checkType(newName);
    	checkCompositeType(type);
    	checkCompositeType(newName);
    	checkPermissions(type, Action.RENAME, thisJoinPoint);
    }

    before (String type, String field, String newName) :
    	execution(* Node.renameField(..)) && args(type, field, newName) {
    	
    	checkType(type);
    	checkField(field);
    	checkField(newName);
    	checkPermissions(type, Action.RENAME_FIELD, thisJoinPoint);
    }

    before (String type, String index, String newName) :
    	execution(* Node.renameIndex(..)) && args(type, index, newName) {
    	
    	checkType(type);
    	checkIndex(index);
    	checkIndex(newName);
    	checkPermissions(type, Action.RENAME_INDEX, thisJoinPoint);
    }

    before (NXObject object) : (execution(* Node.insert(..))) && args(object) {
    	checkObject(object);
    	checkPermissions(object.getType(), Action.INSERT, thisJoinPoint);
    	checkReferencePermissions(object, thisJoinPoint);
    }

    before (NXObject object) : execution(* Node.update(..)) && args(object, ..) {
    	checkObject(object);
    	checkPermissions(object.getType(), Action.UPDATE, thisJoinPoint);
    	checkReferencePermissions(object, thisJoinPoint);
    }

    before (String type, String id) : execution(* Node.update(..)) && args(type, id, *) {
    	checkType(type);
    	checkId(id);
    	checkPermissions(type, id, Action.UPDATE, thisJoinPoint);
    }

    before (String type, String id, String newId) : execution(* Node.updateId(..))
    	&& args(type, id, newId) {
    	
    	checkType(type);
    	checkId(id);
    	checkId(newId);
    	checkPermissions(type, id, Action.UPDATE_ID, thisJoinPoint);
    }

    before (String type, String id, String field, Object value) : execution(* Node.updateField(..))
    	&& args(type, id, field, value) {
    	
    	checkType(type);
    	checkId(id);
    	checkField(field);
    	checkPermissions(type, id, Action.UPDATE_FIELD, thisJoinPoint);
    	checkReferencePermissions(type, id, field, value, thisJoinPoint);
    }

    before (String type, String id, String field) : execution(* Node.updatePassword(..))
    	&& args(type, id, field, ..) {
    	
    	checkType(type);
    	checkId(id);
    	checkField(field);
    	checkPermissions(type, id, Action.UPDATE_PASSWORD, thisJoinPoint);
    }

    before (String[] types) : execution(* Node.drop(..)) && args(types) {
    	checkTypes(types);
    	checkPermissions(types, Action.DROP, thisJoinPoint);
    }

    before (String type, String field) : execution(* Node.dropField(..)) && args(type, field) {
    	checkType(type);
    	checkField(field);
    	checkPermissions(type, Action.DROP_FIELD, thisJoinPoint);
    }

    before (String type, String index) : execution(* Node.dropIndex(..)) && args(type, index) {
    	checkType(type);
    	checkIndex(index);
    	checkPermissions(type, Action.DROP_INDEX, thisJoinPoint);
    }

    before (String type, String[] objects) : execution(* Node.delete(..)) && args(type, objects) {
    	checkType(type);
    	checkObjects(objects);
    	checkPermissions(type, objects, Action.DELETE, thisJoinPoint);
    }

    before (String[] types, boolean includeObjects) : execution(* Node.exportTypes(..))
    	&& args(types, includeObjects) {
    	
    	checkTypes(types);
    	checkPermissions(types, Action.EXPORT_TYPES, thisJoinPoint);
    }

    before (String[] types, Filter filter) : execution(* Node.exportTypes(..))
    	&& args(types, filter, ..) {
    	
    	checkTypes(types);
    	checkFilter(filter);
    	checkPermissions(types, Action.EXPORT_TYPES, thisJoinPoint);
    }

    before (String[] types, Filter[] filters) : execution(* Node.exportTypes(..))
    	&& args(types, filters, ..) {
    	
    	checkTypes(types);
    	checkFilters(filters);
    	checkPermissions(types, Action.EXPORT_TYPES, thisJoinPoint);
    }

    before () : execution(* Node.backup(..)) {
    	checkPermissions(Action.BACKUP, thisJoinPoint);
    }
    
    before () : execution(* Node.getVersion(..)) {
    	checkPermissions(Action.GET_VERSION, thisJoinPoint);
    }

    before (String type, String[] objects, LinkedHashMap<String, Order> order) :
    	execution(* Node.exportObjects(..))
		&& args(type, objects, order) {
    	
    	checkType(type);
    	checkObjects(objects);
    	checkOrder(order);
    	checkPermissions(type, objects, Action.EXPORT_OBJECTS, thisJoinPoint);
    }

    before () : execution(* Node.importTypes(..)) {
    	checkPermissions(Action.IMPORT_TYPES, thisJoinPoint);
    }

    before () : execution(* Node.importObjects(..)) {
    	checkPermissions(Action.IMPORT_OBJECTS, thisJoinPoint);
    }

    before (String type, String id, String action) : execution(* Node.executeAction(..)) 
    	&& args(type, id, action, ..) {

    	checkType(type);
    	checkId(id);
    	checkAction(action);
    	checkPermissions(type, id, action, thisJoinPoint);
    }

    before (String type, String[] objects, String action) : execution(* Node.executeAction(..)) 
		&& args(type, objects, action, ..) {

    	checkType(type);
    	checkObjects(objects);
    	checkAction(action);
    	checkPermissions(type, objects, action, thisJoinPoint);
    }

    before (String type, String id, String[] fields, String lang) :
		execution(* Node.get(..)) && args(type, id, fields, lang, ..) {
    	
    	checkType(type);
    	checkId(id);
    	checkFields(fields);
    	checkLang(lang);
    	checkPermissions(type, id, Action.GET, thisJoinPoint);
    }

    before (String type, String[] fields, String lang, Filter[] filters, String search,
	    LinkedHashMap<String, Order> order) :
		(execution(* Node.select(..)))
		&& args(type, fields, lang, filters, search, order, ..) {
    	
    	checkType(type);
    	checkFields(fields);
    	checkLang(lang);
    	checkFilters(filters);
    	checkOrder(order);
    	checkPermissions(type, Action.SELECT, thisJoinPoint);
    }

    before (String type, String[] fields, String lang, Filter[] filters, String search,
	    LinkedHashMap<String, Order> order) :
		(execution(* Node.selectStream(..)))
		&& args(type, fields, lang, filters, search, order, ..) {
    	
    	checkType(type);
    	checkFields(fields);
    	checkLang(lang);
    	checkFilters(filters);
    	checkOrder(order);
    	checkPermissions(type, Action.SELECT, thisJoinPoint);
    }

    before (String type, String[] fields, String lang, Filter filter, String search,
	    LinkedHashMap<String, Order> order) :
		(execution(* Node.select(..)))
		&& args(type, fields, lang, filter, search, order, ..) {
    	
    	checkType(type);
    	checkFields(fields);
    	checkLang(lang);
    	checkFilter(filter);
    	checkOrder(order);
    	checkPermissions(type, Action.SELECT, thisJoinPoint);
    }

    before (String type, String[] fields, String lang, Filter filter, String search,
	    LinkedHashMap<String, Order> order) :
		(execution(* Node.selectStream(..)))
		&& args(type, fields, lang, filter, search, order, ..) {
    	
    	checkType(type);
    	checkFields(fields);
    	checkLang(lang);
    	checkFilter(filter);
    	checkOrder(order);
    	checkPermissions(type, Action.SELECT, thisJoinPoint);
    }

    before (String type, StringBuilder sql, ArrayList<Object> parameters, String filters, String search,
	    String[] searchFields, String[] groupFields, String order) : (execution(* Node.select(..)))
		&& args(type, sql, parameters, filters, search, searchFields, groupFields, order, ..) {
    	
    	checkType(type);
    	checkPermissions(type, Action.SELECT, thisJoinPoint);
    }

    before (String type, StringBuilder sql, ArrayList<Object> parameters, String filters, String order) :
		(execution(* Node.select(..))) && args(type, sql, parameters, filters, order) {
    	
    	checkType(type);
    	checkPermissions(type, Action.SELECT, thisJoinPoint);
    }

    before (String type, String id, String lang) : execution(* Node.getName(..)) && args(type, id, lang) {
    	checkType(type);
    	checkId(id);
    	checkLang(lang);
    }

    before (String type, String lang) :	execution(* Node.getObjectsName(..)) && args(type, lang) {
    	checkType(type);
    	checkLang(lang);
    }
    
    before (String referencedType, String referencingType, String referencingAction, 
    		String referencingField, String lang) : execution(* Node.getObjectsName(..))
    	&& args(referencedType, referencingType, referencingAction, referencingField, lang) {
    	checkType(referencedType);
    	checkType(referencingType);
    	checkAction(referencingAction);
    	checkField(referencingField);
    	checkLang(lang);
    }

    before (String type, String index) : execution(* Node.getTypeIndex(..)) && args(type, index) {
    	checkType(type);
    	checkIndex(index);
    }

    before (String type, String[] fields) : execution(* Node.getTypeFields(..)) && args(type, fields) {
    	checkType(type);
    	checkFields(fields);
    }

    before (String type, String[] indexes) : execution(* Node.getTypeIndexes(..)) && args(type, indexes) {
    	checkType(type);
    	checkIndexes(indexes);
    }

    before (String type) : (
	    execution(* Node.getType(..)) ||
	    execution(* Node.getUpReferences(..)) ||
	    execution(* Node.getDownReferences(..)) ||
	    execution(* Node.getTypeFields(..)) ||
	    execution(* Node.getTypeIndexes(..)) ||
	    execution(* Node.getTypeSettings(..)) ||
	    execution(* Node.getTypeActions(..)) ||
	    execution(* Node.getFieldsContentType(..)) ||
	    execution(* Node.getADate(..)) ||
	    execution(* Node.existsType(..)) ||
	    execution(* Node.count(..)) ||
	    execution(* Node.hasObjects(..))
	    ) && args(type) {
    	
    	checkType(type);
    }

    before (String[] types) : (
	    execution(* Node.getObjectsInfo(..)) ||
	    execution(* Node.getUpReferences(..)) ||
	    execution(* Node.getTypes(..))
	    ) && args(types) {
    	
    	checkTypes(types);
    }

    before (String type, String id) : (
	    execution(* Node.getFieldsSize(..)) ||
	    execution(* Node.getFieldsInfo(..)) ||
	    execution(* Node.getUDate(..)) ||
	    execution(* Node.getETag(..)) ||
	    execution(* Node.existsObject(..))
	    ) && args(type, id) {
    	
    	checkType(type);
    	checkId(id);
    }

    before (String type, String field) : (
	    execution(* Node.getTypeField(..)) ||
	    execution(* Node.getFieldType(..)) ||
	    execution(* Node.getFieldContentType(..)) ||
	    execution(* Node.getFieldDefault(..)) ||
	    execution(* Node.getFieldRange(..)) ||
	    execution(* Node.hasNullValues(..)) 
	    )&& args(type, field, ..) {
    	
    	checkType(type);
    	checkField(field);
    }

    before (String type, String id, String field) : (
	    execution(* Node.getField(..)) ||
	    execution(* Node.getStringField(..)) ||
	    execution(* Node.getBinaryField(..)) ||
	    execution(* Node.getImageField(..)) ||
	    execution(* Node.getImageContent(..)) ||
	    execution(* Node.getImageThumbnail(..)) ||
	    execution(* Node.getImageContentType(..)) ||
	    execution(* Node.getDocumentContentType(..)) ||
	    execution(* Node.getCompositeFieldContentType(..)) ||
	    execution(* Node.getHTMLField(..)) ||
	    execution(* Node.getXMLField(..)) ||
	    execution(* Node.getDocumentField(..)) ||
	    execution(* Node.getObjectField(..)) ||
	    execution(* Node.getPasswordField(..)) ||
	    execution(* Node.checkPassword(..)) ||
	    execution(* Node.getFieldContentType(..))
	    ) && args(type, id, field, ..) {
    	
    	checkType(type);
    	checkId(id);
    	checkField(field);
    }

    before (String type, String id, String field, String element) : (
    	execution(* Node.getHTMLElement(..)) ||
    	execution(* Node.getXMLElement(..))
    	) && args(type, id, field, element) {

    	checkType(type);
    	checkId(id);
    	checkField(field);
    	checkElement(element);
    }

    before (String type, String action) : 
    	execution(* Node.getActionFields(..)) && args(type, action) {
    	
    	checkType(type);
    	checkAction(action);
    }
    
    before (String type, String action, String field) : (
    	execution(* Node.getActionField(..)) ||
    	execution(* Node.getActionFieldType(..)) ||
    	execution(* Node.getActionFieldRange(..))
    	)&& args(type, action, field, ..) {
    	
    	checkType(type);
    	checkAction(action);
    	checkField(field);
    }
}