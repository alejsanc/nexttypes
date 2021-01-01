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

package com.nexttypes.system;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Action {
	public static final String ADD_FIELD = "add_field";
	public static final String ADD_INDEX = "add_index";
	public static final String ALTER = "alter";
	public static final String ALTER_FIELD = "alter_field";
	public static final String ALTER_FORM = "alter_form";
	public static final String ALTER_INDEX = "alter_index";
	public static final String BACKUP = "backup";
	public static final String CALENDAR = "calendar";
	public static final String CREATE = "create";
	public static final String CREATE_FORM = "create_form";
	public static final String DELETE = "delete";
	public static final String DROP = "drop";
	public static final String DROP_FIELD = "drop_field";
	public static final String DROP_INDEX = "drop_index";
	public static final String EXECUTE_ACTION = "execute_action";
	public static final String EXECUTE_ACTION_FORM = "execute_action_form";
	public static final String EXPORT_OBJECTS = "export_objects";
	public static final String EXPORT_TYPES = "export_types";
	public static final String FILTER_COMPONENT = "filter_component";
	public static final String GET = "get";
	public static final String GET_ELEMENT = "get_element";
	public static final String GET_FIELD = "get_field";
	public static final String GET_FIELD_DEFAULT = "get_field_default";
	public static final String GET_NAMES = "get_names";
	public static final String GET_REFERENCES = "get_references";
	public static final String GET_TYPE = "get_type";
	public static final String GET_TYPES_INFO = "get_types_info";
	public static final String GET_TYPES_NAME = "get_types_name";
	public static final String GET_VERSION = "get_version";
	public static final String IMPORT_OBJECTS = "import_objects";
	public static final String IMPORT_OBJECTS_FORM = "import_objects_form";
	public static final String IMPORT_TYPES = "import_types";
	public static final String IMPORT_TYPES_FORM = "import_types_form";
	public static final String INSERT = "insert";
	public static final String INSERT_FORM = "insert_form";
	public static final String LOGIN = "login";
	public static final String LOGIN_FORM = "login_form";
	public static final String LOGOUT = "logout";
	public static final String PREVIEW = "preview";
	public static final String RENAME = "rename";
	public static final String RENAME_FIELD = "rename_field";
	public static final String RENAME_FORM = "rename_form";
	public static final String RENAME_INDEX = "rename_index";
	public static final String SEARCH = "search";
	public static final String SELECT = "select";
	public static final String SELECT_COMPONENT = "select_component";
	public static final String UPDATE = "update";
	public static final String UPDATE_FIELD = "update_field";
	public static final String UPDATE_FORM = "update_form";
	public static final String UPDATE_ID = "update_id";
	public static final String UPDATE_ID_FORM = "update_id_form";
	public static final String UPDATE_PASSWORD = "update_password";
	public static final String UPDATE_PASSWORD_FORM = "update_password_form";
	
	public String value();
}