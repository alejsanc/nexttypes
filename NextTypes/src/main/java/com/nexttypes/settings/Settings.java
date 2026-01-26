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

package com.nexttypes.settings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Properties;

import com.nexttypes.datatypes.Tuple;
import com.nexttypes.system.Utils;

public class Settings {

	public static final String LOGGER_SETTINGS = "logger.properties";
	public static final String CONTROLLERS_SETTINGS = "controllers.properties";
	public static final String POSTGRESQL_SETTINGS = "postgresql.properties";
	public static final String HTTP_SETTINGS = "http.properties";
	public static final String SMTP_SETTINGS = "smtp.properties";
	public static final String BACKUP_SETTINGS = "backup.properties";
	public static final String CONSOLE_SETTINGS = "console.properties";
	public static final String TASKS_SETTINGS = "tasks.properties";
	public static final String HTML_SETTINGS = "html.properties";
	public static final String ICALENDAR_SETTINGS = "icalendar.properties";
	public static final String RSS_SETTINGS = "rss.properties";
	public static final String SERIAL_SETTINGS = "serial.properties";
	public static final String WEBDAV_SETTINGS = "webdav.properties";
	public static final String TYPES_SETTINGS = "types.properties";
	public static final String PERMISSIONS_SETTINGS = "permissions.properties";
	public static final String CONTEXT_SETTINGS = "context.properties";
	public static final String CLAMAV_SETTINGS = "clamav.properties";

	public static final String DEFAULT_SETTINGS = "/com/nexttypes/settings/defaults/";
	public static final String SETTINGS_DIRECTORY = "settings_directory";

	public static final String ADD_FILTER = "add_filter";
	public static final String ALLOWED_CONTENT_TYPES = "allowed_content_types";
	public static final String ALTER_TITLE = "alter_title";
	public static final String BACKLOG = "backlog";
	public static final String BASIC_AUTH_REALM = "basic_auth_realm";
	public static final String BASIC_AUTH_USER_AGENTS = "basic_auth_user_agents";
	public static final String BINARY_DEBUG = "binary_debug";
	public static final String BINARY_DEBUG_LIMIT = "binary_debug_limit";
	public static final String BIND_ADDRESS = "bind_address";
	public static final String CALENDAR_TITLE = "calendar_title";
	public static final String CHECK_UNCHECK_ALL = "check_uncheck_all";
	public static final String CONTENT_SECURITY_POLICY = "content_security_policy";
	public static final String CONTROL_PANEL = "control_panel";
	public static final String CREATE_TITLE = "create_title";
	public static final String CREATION_DATE = "creation_date";
	public static final String DATE_FORMAT = "date_format";
	public static final String DATETIME_FORMAT = "datetime_format";
	public static final String DEFAULT_LANG = "default_lang";
	public static final String DEFAULT_VIEW = "default_view";
	public static final String DELETE_REFERENCE = "delete_reference";
	public static final String DELETE_SEARCH = "delete_search";
	public static final String DOCUMENT_SENT_TO_PRINTER = "document_sent_to_printer";
	public static final String DROP_FILTER = "drop_filter";
	public static final String EXECUTE_ACTION_TITLE = "execute_action_title";
	public static final String EXISTING_OBJECTS_ACTIONS = "existing_objects_actions";
	public static final String EXISTING_TYPES_ACTIONS = "existing_types_actions";
	public static final String FULLTEXT_SEARCH_TYPES = "fulltext_search_types";
	public static final String HTML_ALLOWED_TAGS = "html_allowed_tags";
	public static final String HTTPS_PORT = "https_port";
	public static final String ID_INPUT_SIZE = "id_input_size";
	public static final String ID_NAME = "id_name";
	public static final String INDEX_TYPES = "index_types";
	public static final String INPUT_SIZE = "input_size";
	public static final String INSERT_TITLE = "insert_title";
	public static final String JAVASCRIPT_DISABLED_WARNING = "javascript_disabled_warning";
	public static final String LOGIN_TITLE = "login_title";
	public static final String LOGO_SIZES = "logo_sizes";
	public static final String LOGO_SRCSET = "logo_srcset";
	public static final String LOGO_TEXT = "logo_text";
	public static final String LOGO_URL = "logo_url";
	public static final String LONG_OBJECTS_COUNT = "long_objects_count";
	public static final String MAX_AUTH_ERRORS = "max_auth_errors";
	public static final String MAX_CONNECTIONS = "max_connections";
	public static final String MAX_INSERTS = "max_inserts";
	public static final String MAX_REQUESTS = "max_requests";
	public static final String MAX_TIME = "max_time";
	public static final String NEXT_NODE = "next_node";
	public static final String OBJECT_ID_SUCCESSFULLY_UPDATED = "object_id_successfully_updated";
	public static final String OBJECT_INPUT_MODE = "object_input_mode";
	public static final String OBJECT_INPUT_LIMIT = "object_input_limit";
	public static final String OBJECT_SUCCESSFULLY_INSERTED = "object_successfully_inserted";
	public static final String OBJECT_SUCCESSFULLY_UPDATED = "object_successfully_updated";
	public static final String OBJECTS_DELETE_CONFIRMATION = "objects_delete_confirmation";
	public static final String OBJECTS_INPUT_LIMIT = "objects_input_limit";
	public static final String OBJECTS_INPUT_MODE = "objects_input_mode";
	public static final String OBJECTS_INPUT_SIZE = "objects_input_size";
	public static final String OBJECTS_INPUT_NOT_NULL = "objects_input_not_null";
	public static final String OBJECTS_SELECT = "objects_select";
	public static final String OBJECTS_SUCCESSFULLY_DELETED = "objects_successfully_deleted";
	public static final String OBJECTS_SUCCESSFULLY_IMPORTED = "objects_successfully_imported";
	public static final String OFFSET_TEXT_MODE = "offset_text_mode";
	public static final String OTHER_TYPES = "other_types";
	public static final String PASSWORD_SUCCESSFULLY_UPDATED = "password_successfully_updated";
	public static final String POOL = "pool";
	public static final String PORT = "port";
	public static final String PREVIEW_TITLE = "preview_title";
	public static final String PRINT_EVENTS_TITLE = "print_events_title";
	public static final String PRINT_OBJECT_TITLE = "print_object_title";
	public static final String PRINT_OBJECTS_TITLE = "print_objects_title";
	public static final String PRINT_PREVIEW_TITLE = "print_preview_title";
	public static final String PRINT_TYPE_TITLE = "print_type_title";
	public static final String PRINT_TYPES_TITLE = "print_types_title";
	public static final String READ_MORE = "read_more";
	public static final String REFERRER_POLICY = "referrer_policy";
	public static final String RENAME_TITLE = "rename_title";
	public static final String SELECT_TITLE = "select_title";
	public static final String SHOW_CONTROL_PANEL = "show_control_panel";
	public static final String SHOW_DEFAULT = "show_default";
	public static final String SHOW_HEADER = "show_header";
	public static final String SHOW_ID = "show_id";
	public static final String SHOW_INSERT_FORM_BUTTON = "show_insert_form_button";
	public static final String SHOW_PREVIEW = "show_preview";
	public static final String SHOW_PROGRESS = "show_progress";
	public static final String SHOW_RANGE = "show_range";
	public static final String SHOW_TYPE = "show_type";
	public static final String SHOW_VALIDATORS = "show_validators";
	public static final String SUCCESSFUL_LOGIN = "successful_login";
	public static final String SUCCESSFUL_LOGOUT = "successful_logout";
	public static final String TIME_FORMAT = "time_format";
	public static final String TYPE_NAME = "type_name";
	public static final String TYPE_NOT_ALTERED = "type_not_altered";
	public static final String TYPE_SUCCESSFULLY_ALTERED = "type_successfully_altered";
	public static final String TYPE_SUCCESSFULLY_CREATED = "type_successfully_created";
	public static final String TYPE_SUCCESSFULLY_RENAMED = "type_successfully_renamed";
	public static final String TYPES_DROP_CONFIRMATION = "types_drop_confirmation";
	public static final String TYPES_SUCCESSFULLY_DROPPED = "types_successfully_dropped";
	public static final String UPDATE_ID_TITLE = "update_id_title";
	public static final String UPDATE_PASSWORD_TITLE = "update_password_title";
	public static final String UPDATE_TITLE = "update_title";
	public static final String UPDATING_DATE = "updating_date";
	public static final String XML_ALLOWED_TAGS = "xml_allowed_tags";
	
	protected ArrayList<Properties> settings;

	public Settings(ArrayList<Properties> settings) {
		this.settings = settings;
	}

	public String getString(String prefix, String[] settings) {
		String value = null;

		for (String setting : settings) {
			value = getString(prefix + "." + setting);
			if (value != null) {
				break;
			}
		}

		return value;
	}

	public String getString(String setting) {
		String value = null;

		for (Properties file : settings) {
			value = Utils.trim(file.getProperty(setting));

			if (value != null) {
				break;
			}
		}

		return value;
	}

	public String[] getStringArray(String setting) {
		return Utils.split(getString(setting));
	}

	public Short getInt16(String setting) {
		return Tuple.parseInt16(getString(setting));
	}

	public Integer getInt32(String setting) {
		return Tuple.parseInt32(getString(setting));
	}

	public Long getInt64(String setting) {
		return Tuple.parseInt64(getString(setting));
	}

	public Float getFloat32(String setting) {
		return Tuple.parseFloat32(getString(setting));
	}

	public Double getFloat64(String setting) {
		return Tuple.parseFloat64(getString(setting));
	}

	public BigDecimal getNumeric(String setting) {
		return Tuple.parseNumeric(getString(setting));
	}

	public Boolean getBoolean(String setting) {
		return Tuple.parseBoolean(getString(setting));
	}

	public Tuple getTuple(String setting) {
		return Tuple.parseTuple(getString(setting));
	}
}