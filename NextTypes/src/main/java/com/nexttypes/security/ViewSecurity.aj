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

import java.util.LinkedHashMap;

import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.enums.Order;
import com.nexttypes.system.Action;
import com.nexttypes.views.View;

public aspect ViewSecurity extends Checks {
	
	before() : execution(* View.getVersion(..)) {
    	checkPermissions(Action.GET_VERSION, thisJoinPoint);
    }
	
    before(String lang, String view) : (execution(* View.getTypesName(..))) && args(lang, view) {
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(Action.GET_TYPES_NAME, thisJoinPoint);
    }

    before(String lang, String view) : (execution(* View.getTypesInfo(..))) && args(lang, view) {
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(Action.GET_TYPES_INFO, thisJoinPoint);
    }

    before(String type, String lang, String view) : (execution(* View.getType(..))) && args(type, lang, view) {
    	checkType(type);
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(type, Action.GET_TYPE, thisJoinPoint);
    }

    before(String lang, String view) : (execution(* View.getReferences(..))) && args(lang, view) {
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(Action.GET_REFERENCES, thisJoinPoint);
    }

    before(String lang, String view) : (execution(* View.createForm(..))) && args(lang, view) {
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(Action.CREATE_FORM, thisJoinPoint);
    }

    before(String type, String lang, String view) : (execution(* View.alterForm(..))) && args(type, lang, view) {
    	checkType(type);
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(type, Action.ALTER_FORM, thisJoinPoint);
    }

    before(String type, String lang, String view, FieldReference ref) : 
		(execution(* View.insertForm(..))) && args(type, lang, view, ref) {
    	
    	checkType(type);
    	checkLang(lang);
    	checkView(view);
    	checkRef(ref);
    	checkPermissions(type, Action.INSERT_FORM, thisJoinPoint);
    }

    before(String type, String id, String lang, String view) : 
		(execution(* View.updateForm(..))) && args(type, id, lang, view) {
    	
    	checkType(type);
    	checkId(id);
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(type, id, Action.UPDATE_FORM, thisJoinPoint);
    }

    before(String type, String id, String lang, String view) : 
		(execution(* View.updateIdForm(..))) && args(type, id, lang, view) {
	
    	checkType(type);
    	checkId(id);
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(type, id, Action.UPDATE_ID_FORM, thisJoinPoint);
    }

    before(String type, String id, String field, String lang, String view) : 
	(execution(* View.updatePasswordForm(..))) && args(type, id, field, lang, view) {
	
    	checkType(type);
    	checkId(id);
    	checkField(field);
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(type, id, Action.UPDATE_PASSWORD_FORM, thisJoinPoint);
    }

    before(String type, String id, String action, String lang, String view) : 
		(execution(* View.executeActionForm(..))) && args(type, id, action, lang, view) {
	
    	checkType(type);
    	checkId(id);
    	checkAction(action);
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(type, id, Action.EXECUTE_ACTION_FORM, thisJoinPoint);
    }

    before(String lang, String view) : 
		(execution(* View.importTypesForm(..))) && args(lang, view) {
	
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(Action.IMPORT_TYPES_FORM, thisJoinPoint);
    }

    before(String lang, String view) : 
		(execution(* View.importObjectsForm(..))) && args(lang, view) {
	
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(Action.IMPORT_OBJECTS_FORM, thisJoinPoint);
    }

    before(String lang, String view) : 
    	(execution(* View.loginForm(..))) && args(lang, view) {
	
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(Action.LOGIN_FORM, thisJoinPoint);
    }

    before(String type, String lang, String view) : 
    	(execution(* View.renameForm(..))) && args(type, lang, view) {
	
    	checkType(type);
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(type, Action.RENAME_FORM, thisJoinPoint);
    }

    before(String type, String lang, String view) : 
    	(execution(* View.unauthorized(..))) && args(type, lang, view, ..) {
	
    	checkType(type);
    	checkLang(lang);
    	checkView(view);
    }

    before(String type, String lang, String view) : 
    	(execution(* View.notFound(..))) && args(type, lang, view, ..) {
	
    	checkType(type);
    	checkLang(lang);
    	checkView(view);
    }

    before(String type, String lang, String view, FieldReference ref, Filter[] filters, String search,
    		LinkedHashMap<String, Order> order) : 
		(execution(* View.select(..))) && args(type, lang, view, ref, filters, search, order, ..) {
	
    	checkType(type);
    	checkLang(lang);
    	checkView(view);
    	checkRef(ref);
    	checkFilters(filters);
    	checkOrder(order);
    	checkPermissions(type, Action.SELECT, thisJoinPoint);
    }
    
    before(String type, String lang, String view, FieldReference ref, Filter[] filters, String search,
    		 LinkedHashMap<String, Order> order) : 
    		(execution(* View.selectComponent(..)))
    		&& args(type, lang, view, ref, filters, search, order, ..) {
    	
    	checkType(type);
        checkLang(lang);
        checkView(view);
        checkRef(ref);
        checkFilters(filters);
        checkOrder(order);
        checkPermissions(type, Action.SELECT_COMPONENT, thisJoinPoint);
    }
    
    before(String type, String field, String lang, String view) :
    	execution(* View.filterComponent(..)) && args(type, field, lang, view, ..) {
    	
    	checkType(type);
    	checkField(field);
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(type, Action.FILTER_COMPONENT, thisJoinPoint);
    }
    
    before(String type, String lang, String view, FieldReference ref, Filter[] filters, String search,
    		LinkedHashMap<String, Order> order) : 
		(execution(* View.preview(..))) && args(type, lang, view, ref, filters, search, order, ..) {
	
    	checkType(type);
    	checkLang(lang);
    	checkView(view);
    	checkRef(ref);
    	checkFilters(filters);
    	checkOrder(order);
    	checkPermissions(type, Action.PREVIEW, thisJoinPoint);
    }

    before(String type, String lang, String view, FieldReference ref) : 
		(execution(* View.calendar(..))) && args(type, lang, view, ref, ..) {
	
    	checkType(type);
    	checkLang(lang);
    	checkView(view);
    	checkRef(ref);
    	checkPermissions(type, Action.CALENDAR, thisJoinPoint);
    }

    before(String type, String id, String lang, String view) : 
		(execution(* View.get(..))) && args(type, id, lang, view, ..) {
	
    	checkType(type);
    	checkId(id);
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(type, id, Action.GET, thisJoinPoint);
    }

    before(String type, String id, String field) : 
		(execution(* View.getField(..))) && args(type, id, field, ..) {
	
    	checkType(type);
    	checkId(id);
    	checkField(field);
    	checkPermissions(type, id, Action.GET_FIELD, thisJoinPoint);
    }
    
    before(String type, String field) : 
		(execution(* View.getFieldDefault(..))) && args(type, field) {
	
    	checkType(type);
    	checkField(field);
    	checkPermissions(type, Action.GET_FIELD_DEFAULT, thisJoinPoint);
    }

    before(String type, String id, String field, String element, String lang, String view) : 
		(execution(* View.getElement(..))) && args(type, id, field, element, lang, view, ..) {
	
    	checkType(type);
    	checkId(id);
    	checkField(field);
    	checkElement(element);
    	checkLang(lang);
    	checkView(view);
    	checkPermissions(type, id, Action.GET_ELEMENT, thisJoinPoint);
    }
}