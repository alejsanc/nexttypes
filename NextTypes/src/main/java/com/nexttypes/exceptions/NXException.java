/*
 * Copyright 2015-2026 Alejandro Sánchez <alex@nexttypes.com>
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

package com.nexttypes.exceptions;

import com.nexttypes.settings.LanguageSettings;

public class NXException extends RuntimeException {
	protected static final long serialVersionUID = 1L;
	
	public static final String ACTION_EXECUTION_ERROR = "action_execution_error";
	public static final String ACTION_NAME_TOO_LONG = "action_name_too_long";
	public static final String ACTION_NOT_FOUND = "action_not_found";
	public static final String ALREADY_ALTERED_TYPE = "already_altered_type";
	public static final String ALREADY_UPDATED_OBJECT = "already_updated_object";
	public static final String AUTH_ERRORS_PER_MINUTE_EXCEEDED = "auth_errors_per_minute_exceeded";
	public static final String CERTIFICATE_NOT_FOUND = "certificate_not_found";
	public static final String DISALLOWED_ATTRIBUTE = "disallowed_attribute";
	public static final String DISALLOWED_CONTENT_TYPE = "disallowed_content_type";
	public static final String DISALLOWED_TAG = "disallowed_tag";
	public static final String DUPLICATE_FIELD = "duplicate_field";
	public static final String DUPLICATE_INDEX = "duplicate_index";
	public static final String ELEMENT_NOT_FOUND = "element_not_found";
	public static final String EMPTY_AXES_LIST = "empty_axes_list";
	public static final String EMPTY_CURRENT_PASSWORD = "empty_current_password";
	public static final String EMPTY_FIELD = "empty_field";
	public static final String EMPTY_FIELD_NAME = "empty_field_name";
	public static final String EMPTY_ID = "empty_id";
	public static final String EMPTY_INDEX_FIELDS_LIST = "empty_index_fields_list";
	public static final String EMPTY_INDEX_NAME = "empty_index_name";
	public static final String EMPTY_INPUT = "empty_input";
	public static final String EMPTY_NEW_NAME = "empty_new_name";
	public static final String EMPTY_OBJECTS_LIST = "empty_objects_list";
	public static final String EMPTY_PASSWORD = "empty_password";
	public static final String EMPTY_TYPE_NAME = "empty_type_name";
	public static final String EMPTY_TYPES_LIST = "empty_types_list";
	public static final String EMPTY_USER_NAME = "empty_user_name";
	public static final String FIELD_HAS_NO_ELEMENTS = "field_has_no_elements";
	public static final String FIELD_HAS_NULL_VALUES = "field_has_null_values";
	public static final String FIELD_IS_PART_OF_INDEX = "field_is_part_of_index";
	public static final String FIELD_NAME_TOO_LONG = "field_name_too_long";
	public static final String FIELD_NOT_FOUND = "field_not_found";
	public static final String FIELD_RESERVED_NAME = "field_reserved_name";
	public static final String ID_TOO_LONG = "id_too_long";
	public static final String INDEX_NAME_TOO_LONG = "index_name_too_long";
	public static final String INDEX_NOT_FOUND = "index_not_found";
	public static final String INVALID_ACTION_NAME = "invalid_action_name";
	public static final String INVALID_BOOLEAN = "invalid_boolean";
	public static final String INVALID_CURRENT_PASSWORD = "invalid_current_password";
	public static final String INVALID_ELEMENT = "invalid_element";
	public static final String INVALID_ELEMENT_NAME = "invalid_element_name";
	public static final String INVALID_EMAIL = "invalid_email";
	public static final String INVALID_FIELD = "invalid_field";
	public static final String INVALID_FIELD_NAME = "invalid_field_name";
	public static final String INVALID_FILTER_TYPE = "invalid_filter_type";
	public static final String INVALID_HOST_NAME = "invalid_host_name";
	public static final String INVALID_ID = "invalid_id";
	public static final String INVALID_IMAGE = "invalid_image";
	public static final String INVALID_INDEX_NAME = "invalid_index_name";
	public static final String INVALID_INPUT = "invalid_input";
	public static final String INVALID_LANG = "invalid_lang";
	public static final String INVALID_NUMERIC = "invalid_numeric";
	public static final String INVALID_OBJECT_INPUT_MODE = "invalid_object_input_mode";
	public static final String INVALID_OBJECT_TYPE = "invalid_object_type";
	public static final String INVALID_OBJECTS_INPUT_MODE = "invalid_objects_input_mode";
	public static final String INVALID_PARAMETER_NAME = "invalid_parameter_name";
	public static final String INVALID_FIELD_PARAMETERS = "invalid_field_parameters";
	public static final String INVALID_PASSWORD = "invalid_password";
	public static final String INVALID_ROW_COUNT = "invalid_row_count";
	public static final String INVALID_SERIAL_FORMAT = "invalid_serial_format";
	public static final String INVALID_SESSION = "invalid_session";
	public static final String INVALID_STREAM_FORMAT = "invalid_stream_format";
	public static final String INVALID_TIMEZONE = "invalid_timezone";
	public static final String INVALID_TYPE_NAME = "invalid_type_name";
	public static final String INVALID_TYPE_OR_FIELD_NAME = "invalid_type_or_field_name";
	public static final String INVALID_URL = "invalid_url";
	public static final String INVALID_USER_OR_PASSWORD = "invalid_user_or_password";
	public static final String INVALID_VIEW_NAME = "invalid_view_name";
	public static final String MAX_INSERTS_EXCEEDED = "max_inserts_exceeded";
	public static final String METHOD_NOT_ALLOWED = "method_not_allowed";
	public static final String MISSING_FIELD = "missing_field";
	public static final String NO_OBJECTS_FOUND = "no_objects_found";
	public static final String NO_TYPES_FOUND = "no_types_found";
	public static final String NOT_IMPLEMENTED_METHOD = "not_implemented_method";
	public static final String OBJECT_ALREADY_EXISTS = "object_already_exists";
	public static final String OBJECT_NOT_FOUND = "object_not_found";
	public static final String OUT_OF_RANGE_VALUE = "out_of_range_value";
	public static final String PARAMETER_NAME_TOO_LONG = "parameter_name_too_long";
	public static final String PASSWORD_FIELD_DEFAULT_VALUE = "password_field_default_value";
	public static final String PASSWORD_FIELD_UPDATE = "password_field_update";
	public static final String PASSWORDS_DONT_MATCH = "passwords_dont_match";
	public static final String PRIMITIVE_TYPE_WITH_THE_SAME_NAME = "primitive_type_with_the_same_name";
	public static final String PRINTER_ERROR = "printer_error";
	public static final String SELECT_STRING_NOT_FOUND = "select_string_not_found";	
	public static final String SESSION_EXPIRED = "session_expired";
	public static final String SESSION_PARAMETER_NOT_FOUND = "session_parameter_not_found";
	public static final String SYSTEM_TYPES_CANT_BE_DROPPED = "system_types_cant_be_dropped";
	public static final String TYPE_ALREADY_EXISTS = "type_already_exists";
	public static final String TYPE_ALREADY_HAS_OBJECTS = "type_already_has_objects";
	public static final String TYPE_HAS_NO_BINARY_FIELDS = "type_has_no_binary_fields";
	public static final String TYPE_NAME_TOO_LONG = "type_name_too_long";
	public static final String TYPE_NOT_FOUND = "type_not_found";
	public static final String TYPE_OR_FIELD_NAME_TOO_LONG = "type_or_field_name_too_long";
	public static final String TYPE_RESERVED_NAME = "type_reserved_name";
	public static final String UNAUTHORIZED_ACTION = "unauthorized_action";
	public static final String UNAUTHORIZED_REFERENCE = "unauthorized_reference";
	public static final String UNEXPECTED_TAG = "unexpected_tag";
	public static final String USER_NOT_LOGGED_IN = "user_not_logged_in";
	public static final String VIEW_NOT_FOUND = "view_not_found";
	public static final String VIRUS_FOUND = "virus_found";
	
	protected String type;
	protected String setting;
	
	public NXException(String setting) {
		this(null, setting);
	}

	public NXException(String type, String setting) {
		this.type = type;
		this.setting = setting;
	}
	
	public NXException(String type, String setting, Throwable cause) {
		super(cause);
		this.type = type;
		this.setting = setting;
	}

	public NXException(Throwable cause) {
		super(cause);
	}

	public String getType() {
		return type;
	}

	public String getSetting() {
		return setting;
	}

	public String getMessage(LanguageSettings languageSettings) {
		String message = null;
		Throwable cause = getCause();
		
		if (cause == null) {
			message = languageSettings.gts(type, setting);
		} else {			
			message = getMessage(cause);
		}
		
		return message;
	}

	public static String getMessage(Throwable e) {
		String className = e.getClass().getName();
		String message = e.getMessage();
		
		
		if (message == null) {
			message = className;
		} else {
			message = className + ": " + message;
		}

		return message;
	}
}