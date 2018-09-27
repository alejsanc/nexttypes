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

package com.nexttypes.views;

import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.client.utils.URIBuilder;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.nexttypes.datatypes.Anchor;
import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.DocumentPreview;
import com.nexttypes.datatypes.File;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.HTML;
import com.nexttypes.datatypes.HTML.InputGroup;
import com.nexttypes.datatypes.HTMLFragment;
import com.nexttypes.datatypes.Menu;
import com.nexttypes.datatypes.MenuSection;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.ObjectField;
import com.nexttypes.datatypes.ObjectReference;
import com.nexttypes.datatypes.Objects;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.QRCode;
import com.nexttypes.datatypes.Reference;
import com.nexttypes.datatypes.Serial;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.Type;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.datatypes.TypeInfo;
import com.nexttypes.datatypes.TypeReference;
import com.nexttypes.datatypes.XML.Element;
import com.nexttypes.enums.Comparison;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.IndexMode;
import com.nexttypes.enums.Order;
import com.nexttypes.enums.Component;
import com.nexttypes.exceptions.ActionNotFoundException;
import com.nexttypes.exceptions.ElementException;
import com.nexttypes.exceptions.ElementNotFoundException;
import com.nexttypes.exceptions.FieldException;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.exceptions.NotFoundException;
import com.nexttypes.exceptions.ObjectNotFoundException;
import com.nexttypes.exceptions.UnauthorizedActionException;
import com.nexttypes.interfaces.Node;
import com.nexttypes.protocol.http.HTTPHeader;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.protocol.http.HTTPStatus;
import com.nexttypes.security.Security;
import com.nexttypes.settings.Permissions;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.system.Action;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Context;
import com.nexttypes.system.Icon;
import com.nexttypes.system.Loader;
import com.nexttypes.system.Utils;

public class HTMLView extends View {

	//Strings
	public static final String RSS = "RSS";
	public static final String ICALENDAR = "iCalendar";
	public static final String HTML5 = "HTML5";
	public static final String CSS = "CSS";
	public static final String WCAG = "WCAG";
		
	//Data Strings Attributes
	public static final String DATA_STRINGS_ACCEPT = "data-strings-accept";
	public static final String DATA_STRINGS_CANCEL = "data-strings-cancel";
	public static final String DATA_STRINGS_FIELDS = "data-strings-fields";
	public static final String DATA_STRINGS_TYPE = "data-strings-type";
	public static final String DATA_STRINGS_NAME = "data-strings-name";
	public static final String DATA_STRINGS_PARAMETERS = "data-strings-parameters";
	public static final String DATA_STRINGS_NOT_NULL = "data-strings-not-null";
	public static final String DATA_STRINGS_MODE = "data-strings-mode";
	public static final String DATA_STRINGS_DROP_FIELD = "data-strings-drop-field";
	public static final String DATA_STRINGS_DROP_INDEX = "data-strings-drop-index";
	public static final String DATA_STRINGS_TYPES_DROP_CONFIRMATION = "data-strings-types-drop-confirmation";
	public static final String DATA_STRINGS_OBJECTS_DELETE_CONFIRMATION = "data-strings-objects-delete-confirmation";

	//Data Attributes
	public static final String DATA_EDITOR = "data-editor";
	public static final String DATA_SHOW_PROGRESS = "data-show-progress";
	public static final String DATA_MULTI_ORDER = "data-multi-order";
	public static final String DATA_URI = "data-uri";
	public static final String DATA_ID = "data-id";
	public static final String DATA_LANG = "data-lang";
	public static final String DATA_COMPONENT = "data-component";
	public static final String DATA_SIZE = "data-size";
	
	//Elements
	public static final String USER_NAME = "user-name";
	public static final String LOGOUT_BUTTON = "logout-button";
	public static final String TYPE_MENU = "type-menu";
	public static final String CONTROL_PANEL = "control-panel";
	public static final String VALIDATORS = "validators";

	//Classes
	public static final String ADD_FILTER = "add-filter";
	public static final String FILTER_FIELD = "filter-field";
	public static final String OBJECT_RADIO_INPUT = "object-radio-input";
	public static final String OBJECTS_TEXTAREA_INPUT = "objects-textarea-input";
	public static final String REFERENCE_OUTPUT = "reference-output";
	public static final String SEARCH_OUTPUT = "search-output";
	public static final String SELECT_HEADER = "select-header";
	public static final String SELECT_HEADER_ANCHOR = "select-header-anchor";
	public static final String SELECT_MENU = "select-menu";
	public static final String SELECT_BUTTONS = "select-buttons";
	public static final String SELECT_INDEX = "select-index";
	public static final String UNLOAD_CONFIRMATION = "unload-confirmation";
	public static final String ADD_FIELD = "add-field";
	public static final String ADD_INDEX = "add-index";
	public static final String DELETE_ROW = "delete-row";
	public static final String SUBMIT_FORM = "submit-form";
	public static final String CLEAR_BINARY_INPUT = "clear-binary-input";
	public static final String ALL_CHECKBOX = "all-checkbox";
	public static final String ITEM_CHECKBOX = "item-checkbox";
	public static final String MENU_TITLE = "menu-title";
	public static final String REFERENCE_FIELD = "reference-field";
	public static final String BINARY_INPUT_SIZE = "binary-input-size";
	public static final String NULL_FIELD_INPUT = "null-field-input";
	public static final String SMALL_TEXTAREA = "small-textarea";
	public static final String MEDIUM_TEXTAREA = "medium-textarea";
	public static final String ORDER_COLUMN = "order-column";
	public static final String EXPORT_BUTTON = "export-button";
	public static final String SELECTED_OFFSET = "selected-offset";
	public static final String NEAR_SELECTED_OFFSET = "near-selected-offset";
	public static final String FIELD_OUTPUT = "field-output";
	public static final String ICON = "icon";
	public static final String SMALL_ICON = "small-icon";
	public static final String IMAGE = "image";
	public static final String CALENDAR = "calendar";
	public static final String CALENDAR_MONTH = "calendar-month";
	public static final String CALENDAR_DAY = "calendar-day";
	public static final String CALENDAR_TODAY = "calendar-today";
	public static final String YEARS = "years";
	public static final String MONTHS = "months";
	public static final String READONLY = "readonly";

	//Text Editor Modes
	public static final String JSON = "json";
	public static final String XML = "xml";
	public static final String VISUAL = "visual";
	public static final String JAVASCRIPT = "javascript";
	
	//Objects Input Modes
	public static final String MULTIPLE_SELECT = "multiple_select";
		
	protected HTML document;
	protected Element head;
	protected Element main;
	protected Element footer;
	protected DecimalFormat humanReadableBytesFormat;
	protected ArrayList<String> textEditorModes;
	protected Permissions permissions;
	
	public HTMLView(HTMLView parent) {
		document = parent.getDocument();
		nextNode = parent.getNextNode();
		request = parent.getRequest();
		context = parent.getContext();
		settings = parent.getSettings();
		typeSettings = parent.getTypeSettings();
		strings = parent.getStrings();
		user = parent.getUser();
		groups = parent.getGroups();
		permissions = parent.getPermissions();
	}

	public HTMLView(HTTPRequest request) {
		super(request, Settings.HTML_SETTINGS);
		permissions = request.getContext().getPermissions(user, groups);
	}
	
	public Permissions getPermissions() {
		return permissions;
	}

	@Override
	public Content getTypesName(String lang, String view) {
		return getTypesInfo(lang, view);
	}

	@Override
	public Content getTypesInfo(String lang, String view) {
		loadTemplate(null, lang, view);
		setTitle(strings.gts(Constants.TYPES));
		TypeInfo[] types = nextNode.getTypesInfo();

		if (types.length > 0) {
			main.appendElement(typesTable(types, lang, view));
		} else {
			main.appendElement(HTML.P).appendText(strings.gts(Constants.NO_TYPES_FOUND));
		}

		return render();
	}

	@Override
	public Content insertForm(String type, String lang, String view, FieldReference ref) {
		loadTemplate(type, lang, view);
		String title = strings.gts(type, Constants.INSERT_TITLE);
		String typeName = strings.getTypeName(type);

		String[] fields = typeSettings.getActionStringArray(type, Action.INSERT, Constants.FIELDS);
		
		setTitle(Utils.format(title, typeName));

		textEditors();
		main.appendElement(insertForm(type, fields, lang, view, ref));

		return render(type);
	}

	@Override
	public Content createForm(String lang, String view) {
		loadTemplate(null, lang, view);
		setTitle(strings.gts(Constants.CREATE_TYPE));

		Element form = typeForm(null, lang, view);
		main.appendElement(form);

		return render();
	}

	@Override
	public Content alterForm(String type, String lang, String view) {
		loadTemplate(type, lang, view);

		String title = strings.gts(type, Constants.ALTER_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));

		Element form = typeForm(type, lang, view);
		main.appendElement(form);

		return render(type);
	}

	public Element typeForm(String type, String lang, String view) {
		String fields = strings.gts(type, Constants.FIELDS);
		String typeString = strings.gts(type, Constants.TYPE);
		String name = strings.gts(type, Constants.NAME);
		String parameters = strings.gts(type, Constants.PARAMETERS);
		String notNull = strings.gts(type, Constants.NOT_NULL);
		String mode = strings.gts(type, Constants.MODE);
		String dropIndex = strings.getActionName(type, Action.DROP_INDEX);
		String dropField = strings.getActionName(type, Action.DROP_FIELD);
		
		String action = null;
		String icon = null;

		Element typeForm = form(type, lang, view)
				.addClass(UNLOAD_CONFIRMATION)
				.setAttribute(HTML.AUTOCOMPLETE, HTML.OFF)
				.setAttribute(DATA_STRINGS_FIELDS, fields)
				.setAttribute(DATA_STRINGS_TYPE, typeString)
				.setAttribute(DATA_STRINGS_NAME, name)
				.setAttribute(DATA_STRINGS_PARAMETERS, parameters)
				.setAttribute(DATA_STRINGS_NOT_NULL, notNull)
				.setAttribute(DATA_STRINGS_MODE, mode)
				.setAttribute(DATA_STRINGS_DROP_FIELD, dropField)
				.setAttribute(DATA_STRINGS_DROP_INDEX, dropIndex);

		String typeName = strings.gts(Constants.TYPE_NAME);
		typeForm.appendElement(HTML.STRONG).appendText(typeName + ": ");

		if (type != null) {
			action = Action.ALTER;
			icon = Icon.PENCIL;
			
			ZonedDateTime adate = nextNode.getADate(type);
			typeForm.appendElement(input(HTML.HIDDEN, Constants.ADATE, Constants.ADATE, adate));
			
			typeForm.appendText(type);
			
			if (permissions.isAllowed(type, Action.RENAME_FORM)) {
				typeForm.appendElement(iconAnchor(strings.getActionName(type, Action.RENAME),
					uri(type, lang, view) + formParameter(Action.RENAME), Icon.PENCIL));
			}
				
		} else {
			action = Action.CREATE;
			icon = Icon.PLUS;
			
			typeForm.appendElement(input(HTML.TEXT, Constants.TYPE, typeName)
					.setAttribute(HTML.SIZE, Type.MAX_TYPE_NAME_LENGTH)
					.setAttribute(HTML.MAXLENGTH, Type.MAX_TYPE_NAME_LENGTH));
		}
		
		boolean disableAction = !permissions.isAllowed(type, action);
		
		String addFieldActionName = strings.getActionName(type, Action.ADD_FIELD);

		typeForm.appendElement(HTML.H2).appendText(fields + ":");
		typeForm.appendElement(HTML.P)
				.appendElement(button(addFieldActionName, null, Icon.PLUS, ADD_FIELD));
		
		Element fieldsTable = typeForm.appendElement(HTML.TABLE).setAttribute(HTML.ID, Constants.FIELDS);

		Element fieldsHeader = fieldsTable.appendElement(HTML.THEAD).appendElement(HTML.TR);
		fieldsHeader.appendElement(HTML.TH).appendText(typeString);
		fieldsHeader.appendElement(HTML.TH).appendText(name);
		fieldsHeader.appendElement(HTML.TH).appendText(parameters);
		fieldsHeader.appendElement(HTML.TH).appendText(notNull);
		fieldsHeader.appendElement(HTML.TH);
		
		Element fieldsBody = fieldsTable.appendElement(HTML.TBODY);
		
		if (type != null) {
			
			String[] types = (String[]) ArrayUtils.addAll(PT.PRIMITIVE_TYPES, nextNode.getTypesName());
						
			LinkedHashMap<String, TypeField> typeFields = nextNode.getTypeFields(type);
			int x = 0;
			for (Map.Entry<String, TypeField> entry : typeFields.entrySet()) {
				String field = Constants.FIELDS + ":" + x;
				String fieldName = entry.getKey();
				TypeField typeField = entry.getValue();
				String fieldType = typeField.getType();
				
				Element row = fieldsBody.appendElement(HTML.TR);

				row.appendElement(HTML.TD).appendElement(select(field + ":"
						+ Constants.TYPE, strings.gts(type, Constants.TYPE), types, fieldType));
												
				Element nameCell = row.appendElement(HTML.TD);
				nameCell.appendElement(input(HTML.TEXT, field + ":" + Constants.NAME, name, fieldName));
				nameCell.appendElement(input(HTML.HIDDEN, field + ":" + Constants.OLD_NAME, name,
						fieldName));
								
				row.appendElement(HTML.TD).appendElement(input(HTML.TEXT, field + ":"
						+ Constants.PARAMETERS, parameters, typeField.getParameters()));
								
				row.appendElement(HTML.TD).appendElement(booleanInput(field + ":" 
						+ Constants.NOT_NULL, notNull, typeField.isNotNull()));
								
				row.appendElement(HTML.TD).appendElement(smallButton(dropField, Icon.MINUS, DELETE_ROW));
								
				x++;
			}
		}

		String addIndexActionName = strings.getActionName(type, Action.ADD_INDEX);

		typeForm.appendElement(HTML.H2).appendText(strings.gts(type, Constants.INDEXES) + ":");
		typeForm.appendElement(HTML.P)
				.appendElement(button(addIndexActionName, null, Icon.PLUS, ADD_INDEX));
		
		

		Element indexesTable = typeForm.appendElement(HTML.TABLE)
				.setAttribute(HTML.ID, Constants.INDEXES);

		Element indexesHeader = indexesTable.appendElement(HTML.THEAD).appendElement(HTML.TR);
		indexesHeader.appendElement(HTML.TH).appendText(mode);
		indexesHeader.appendElement(HTML.TH).appendText(name);
		indexesHeader.appendElement(HTML.TH).appendText(fields);
		indexesHeader.appendElement(HTML.TH);
		
		Element indexesBody = indexesTable.appendElement(HTML.TBODY);

		if (type != null) {
			LinkedHashMap<String, TypeIndex> typeIndexes = nextNode.getTypeIndexes(type);
			int x = 0;
			for (Map.Entry<String, TypeIndex> entry : typeIndexes.entrySet()) {
				String index = Constants.INDEXES + ":" + x;
				String indexName = entry.getKey();
				TypeIndex typeIndex = entry.getValue();
				Element row = indexesBody.appendElement(HTML.TR);
				
				row.appendElement(HTML.TD).appendElement(select(index + ":" + Constants.MODE, mode,
						IndexMode.getStringValues(), typeIndex.getMode().toString()));
				
				Element nameCell = row.appendElement(HTML.TD);
				nameCell.appendElement(input(HTML.TEXT, index + ":" + Constants.NAME, name, indexName));
				nameCell.appendElement(input(HTML.HIDDEN, index + ":" + Constants.OLD_NAME, name, indexName));
								
				row.appendElement(HTML.TD).appendElement(input(HTML.TEXT, index + ":" + Constants.FIELDS, fields,
						String.join(",", typeIndex.getFields())));
				
				row.appendElement(HTML.TD)
						.appendElement(smallButton(dropIndex, Icon.MINUS, DELETE_ROW));
				
				x++;
			}
		}

		
		String actionName = strings.getActionName(type, action);

		Element actionButton = typeForm.appendElement(HTML.P)
				.appendElement(button(actionName, action, icon, SUBMIT_FORM));
		if (disableAction) {
			actionButton.setAttribute(HTML.DISABLED);
		}
		
		textEditors();

		return typeForm;
	}

	@Override
	public Content renameForm(String type, String lang, String view) {
		loadTemplate(type, lang, view);
		String title = strings.gts(type, Constants.RENAME_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));
		main.appendElement(renameFormElement(type, lang, view));
		return render(type);
	}

	public Element renameFormElement(String type, String lang, String view) {
		String newName = strings.gts(type, Constants.NEW_NAME);

		Element form = form(type, lang, view);
		Element table = form.appendElement(HTML.TABLE);
		Element body = table.appendElement(HTML.TBODY);

		Element row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(strings.gts(type, Constants.TYPE) + ":");
		row.appendElement(HTML.TD).appendText(type);

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(newName + ":");
		row.appendElement(HTML.TD).appendElement(input(HTML.TEXT, Constants.NEW_NAME, newName));

		String actionName = strings.getActionName(type, Action.RENAME);

		Element actionButton = form.appendElement(button(actionName, Action.RENAME, Icon.PENCIL,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, Action.RENAME)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return form;
	}

	@Override
	public Content executeActionForm(String type, String id, String action, String lang, String view) {
		loadTemplate(type, lang, view);

		if (!permissions.isAllowed(type, action)) {
			return unauthorized(type, lang, view, new UnauthorizedActionException(type, action));
		}
		
		LinkedHashMap<String, TypeField> fields = nextNode.getActionFields(type, action);
		if (fields == null) {
			return notFound(type, lang, view, new ActionNotFoundException(type, action));
		}

		String title = strings.gts(type, Constants.EXECUTE_ACTION_TITLE);
		String typeName = strings.getTypeName(type);
		String actionName = strings.getActionName(type, action);
		setTitle(Utils.format(title, actionName, typeName));

		textEditors();
		main.appendElement(executeActionForm(type, id, action, actionName, fields, lang, view));

		return render();
	}

	public Element executeActionForm(String type, String id, String action, String actionName,
			LinkedHashMap<String, TypeField> fields, String lang, String view) {
		
		boolean showType = typeSettings.getActionBoolean(type, action, Constants.SHOW_TYPE);
		boolean showId = typeSettings.getActionBoolean(type, action, Constants.SHOW_ID);
		boolean showHeader = typeSettings.getActionBoolean(type, action, Constants.SHOW_HEADER);
		boolean showProgress = typeSettings.getActionBoolean(type, action, Constants.SHOW_PROGRESS);
		
		return executeActionForm(type, id, action, actionName, fields, lang, view, showType,
				showId, showHeader, showProgress);
	}
	
	public Element executeActionForm(String type, String id, String action, String actionName,
			LinkedHashMap<String, TypeField> fields, String lang, String view, boolean showType,
			boolean showId, boolean showHeader, boolean showProgress) {
		
		Element form = form(type, id, lang, view);

		if (showProgress) {
			form.setAttribute(DATA_SHOW_PROGRESS);
		}

		Element table = form.appendElement(HTML.TABLE);
		Element header = null;

		if (showHeader) {
			header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		}

		Element body = table.appendElement(HTML.TBODY);

		if (showHeader) {
			if (showType) {
				header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.TYPE));
			}

			header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.NAME));
			header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.VALUE));
		}

		Element row = body.appendElement(HTML.TR);

		if (showType) {
			row.appendElement(HTML.TD).appendText(PT.STRING);
		}

		if (id != null) {
			if (showId) {
				row.appendElement(HTML.TD).appendText(strings.getIdName(type));
				row.appendElement(HTML.TD).appendText(id);
			}
		} else {
			String objectsName = strings.getObjectsName(type);
			
			row.appendElement(HTML.TD).appendText(objectsName);
			row.appendElement(HTML.TD).appendElement(actionObjectsInput(type, action, objectsName, lang));
		}

		for (Map.Entry<String, TypeField> entry : fields.entrySet()) {
			String field = entry.getKey();
			TypeField typeField = entry.getValue();

			String fieldName = strings.getActionFieldName(type, action, field);

			row = body.appendElement(HTML.TR);

			if (showType) {
				row.appendElement(HTML.TD).appendText(typeField.getType());
			}
			
			
			row.appendElement(HTML.TD).appendText(fieldName);
			row.appendElement(HTML.TD).appendElement(actionFieldInput(type, action, field, fieldName,
					null, typeField, lang));
		}

		Element actionButton = form.appendElement(button(actionName, action, Icon.CHEVRON_TOP,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, action)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return form;
	}

	@Override
	public Content importTypesForm(String lang, String view) {
		loadTemplate(null, lang, view);

		String title = strings.gts(Action.IMPORT_TYPES);
		String existingTypesAction = strings.gts(Constants.EXISTING_TYPES_ACTION);
		String existingObjectsAction = strings.gts(Constants.EXISTING_OBJECTS_ACTION);
		String file = strings.gts(Constants.FILE);
		boolean showProgress = typeSettings.getActionBoolean(null, Action.IMPORT_TYPES, Constants.SHOW_PROGRESS);

		setTitle(title);

		Element form = multipartForm(lang, view);
		main.appendElement(form);

		if (showProgress) {
			form.setAttribute(DATA_SHOW_PROGRESS);
		}

		Element table = form.appendElement(HTML.TABLE);
		Element body = table.appendElement(HTML.TBODY);

		Element row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(existingTypesAction + ":");
		row.appendElement(HTML.TD).appendElement(select(Constants.EXISTING_TYPES_ACTION, existingTypesAction,
				strings.getTypeTuple(null, Constants.EXISTING_TYPES_ACTIONS)));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(existingObjectsAction + ":");
		row.appendElement(HTML.TD).appendElement(select(Constants.EXISTING_OBJECTS_ACTION, existingObjectsAction,
				strings.getTypeTuple(null, Constants.EXISTING_OBJECTS_ACTIONS)));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(file + ":");
		row.appendElement(HTML.TD)
				.appendElement(binaryInput(Constants.DATA, title, Format.JSON.getContentType(), lang));

		Element actionButton = form.appendElement(button(strings.gts(Constants.IMPORT),
				Action.IMPORT_TYPES, Icon.UNSHARE_BOXED, SUBMIT_FORM));
		if (!permissions.isAllowed(Action.IMPORT_TYPES)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return render();
	}

	@Override
	public Content importObjectsForm(String lang, String view) {
		loadTemplate(null, lang, view);

		String title = strings.gts(Action.IMPORT_OBJECTS);
		String existingObjectsAction = strings.gts(Constants.EXISTING_OBJECTS_ACTION);
		String file = strings.gts(Constants.FILE);
		boolean showProgress = typeSettings.getActionBoolean(null, Action.IMPORT_OBJECTS, Constants.SHOW_PROGRESS);

		setTitle(title);

		Element form = multipartForm(lang, view);
		main.appendElement(form);

		if (showProgress) {
			form.setAttribute(DATA_SHOW_PROGRESS);
		}

		Element table = form.appendElement(HTML.TABLE);
		Element body = table.appendElement(HTML.TBODY);

		Element row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(existingObjectsAction + ":");
		row.appendElement(HTML.TD).appendElement(select(Constants.EXISTING_OBJECTS_ACTION, existingObjectsAction,
				strings.getTypeTuple(null, Constants.EXISTING_OBJECTS_ACTIONS)));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(file + ":");
		row.appendElement(HTML.TD)
				.appendElement(binaryInput(Constants.DATA, title, Format.JSON.getContentType(), lang));

		Element actionButton = form.appendElement(button(strings.gts(Constants.IMPORT),
				Action.IMPORT_OBJECTS, Icon.UNSHARE_BOXED, SUBMIT_FORM));
		if (!permissions.isAllowed(Action.IMPORT_OBJECTS)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return render();
	}

	@Override
	public Content loginForm(String lang, String view) {
		loadTemplate(null, lang, view);

		String user = strings.gts(Constants.USER);
		String password = strings.gts(Constants.PASSWORD);

		setTitle(strings.gts(Constants.LOGIN_TITLE));
		Element form = form(lang, view);
		main.appendElement(form);

		Element table = form.appendElement(HTML.TABLE);
		Element body = table.appendElement(HTML.TBODY);

		Element row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(user + ":");
		row.appendElement(HTML.TD).appendElement(input(HTML.TEXT, Constants.LOGIN_USER, user));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(password + ":");
		row.appendElement(HTML.TD).appendElement(input(HTML.PASSWORD, Constants.LOGIN_PASSWORD, password));

		String actionName = strings.getActionName(null, Action.LOGIN);

		Element actionButton = form.appendElement(button(actionName, Action.LOGIN, Icon.ACCOUNT_LOGIN,
				SUBMIT_FORM));
		if (!permissions.isAllowed(Action.LOGIN)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return render();
	}

	@Override
	public Content getType(String type, String lang, String view) {
		loadTemplate(type, lang, view);
		setTitle(strings.gts(type, Constants.TYPE) + ": " + type);

		Type typeObject = nextNode.getType(type);

		Element pre = main.appendElement(HTML.PRE);
		pre.appendText(new Serial(typeObject, Format.JSON).getString());

		return render(type);
	}

	@Override
	public Content getReferences(String lang, String view) {
		loadTemplate(null, lang, view);
		setTitle(strings.gts(Constants.REFERENCES));

		Reference[] references = nextNode.getReferences();

		Element table = main.appendElement(HTML.TABLE);
		Element header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		Element body = table.appendElement(HTML.TBODY);
		header.appendElement(HTML.TH).appendText(strings.gts(Constants.REFERENCED_TYPE));
		header.appendElement(HTML.TH).appendText(strings.gts(Constants.REFERENCING_TYPE));
		header.appendElement(HTML.TH).appendText(strings.gts(Constants.REFERENCING_FIELD));

		for (Reference reference : references) {
			String referencedType = reference.getReferencedType();
			String referencingType = reference.getReferencingType();
			String referencingField = reference.getReferencingField();
			
			String referencedTypeName = strings.getTypeName(referencedType);
			String referencingTypeName = strings.getTypeName(referencingType);
			String referencingFieldName = strings.getFieldName(referencingType, referencingField);

			Element row = body.appendElement(HTML.TR);
			
			Element referencedTypeCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(referencedType, Action.GET_TYPE)) {
				referencedTypeCell.appendElement(anchor(referencedTypeName, 
						uri(referencedType, lang, view) + "&" + Constants.INFO));
			} else {
				referencedTypeCell.appendText(referencedTypeName);
			}
			
			Element referencingTypeCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(referencingType, Action.GET_TYPE)) {
				referencingTypeCell.appendElement(anchor(referencingTypeName,
						uri(referencingType, lang, view) + "&" + Constants.INFO));
			} else {
				referencingTypeCell.appendText(referencingTypeName);
			}
			
			row.appendElement(HTML.TD).appendText(referencingFieldName);
		}

		return render();
	}

	@Override
	public Content get(String type, String id, String lang, String view, String etag) {
		loadTemplate(type, lang, view);

		String[] fields = typeSettings.getActionStringArray(type, Action.GET, Constants.FIELDS);
		LinkedHashMap<String, TypeField> typeFields = nextNode.getTypeFields(type, fields);

		NXObject object = nextNode.get(type, id, fields, lang, true, false, true, false, true, true);

		if (object == null) {
			return objectNotFound(type, id, lang, view);
		}

		main.appendElement(getElement(object, typeFields, lang, view));

		main.appendElement(downReferences(type, id, lang, view));

		return render(type);
	}

	public Element getElement(NXObject object, LinkedHashMap<String, TypeField> typeFields, String lang, String view) {
		Element article = document.createElement(HTML.ARTICLE);

		String typeName = strings.getTypeName(object.getType());

		article.appendElement(document.createElement(HTML.H1).appendText(typeName + ": " + object.getName()));
		document.getTitle().appendText(typeName + ": " + object.getName());

		for (Entry<String, Object> entry : object.getFields().entrySet()) {
			String field = entry.getKey();
			Object value = entry.getValue();
			TypeField typeField = typeFields.get(field);
			article.appendElement(fieldOutput(object, field, value, typeField, lang, view));
		}

		article.appendElement(dates(object.getType(), object.getCDate(), object.getUDate()));

		return article;
	}

	@Override
	public Content getField(String type, String id, String field, String etag) {
		Content content = null;

		ObjectField objectField = nextNode.getObjectField(type, id, field);

		Object value = objectField.getValue();

		if (value != null) {
			String contentType = objectField.getContentType();
			String fieldType = nextNode.getTypeField(type, field).getType();

			if (!PT.isBinaryType(fieldType)) {
				value = value.toString();
			}
			
			content = new Content(value, contentType);
			content.setHeader(HTTPHeader.ETAG, objectField.getETag());
		}

		return content;
	}
	
	@Override
	public Content getFieldDefault(String type, String field) {
		Content content = null;

		Object value = nextNode.getFieldDefault(type, field);

		if (value != null) {
			String contentType = nextNode.getFieldContentType(type, field);
			String fieldType = nextNode.getTypeField(type, field).getType();

			if (!PT.isBinaryType(fieldType)) {
				
				value = value.toString();
				
			} else if (PT.isFileType(fieldType)) {
				
				if (contentType == null) {
					contentType = ((File) value).getContentType();
				}
				
				value = ((File) value).getContent();
			}
			
			content = new Content(value, contentType);
		}

		return content;
	}

	@Override
	public Content getElement(String type, String id, String field, String element, String lang, String view,
			String etag) {
		Content content = null;

		String fieldType = nextNode.getTypeField(type, field).getType();

		switch (fieldType) {
		case PT.HTML:
			Element htmlElement = nextNode.getHTMLElement(type, id, field, element);
			if (htmlElement != null) {
				content = new Content(htmlElement.toString(), Format.XHTML);
			}
			break;

		case PT.XML:
			Element xmlElement = nextNode.getXMLElement(type, id, field, element);
			if (xmlElement != null) {
				content = new Content(xmlElement.toString(), Format.XML);
			}
			break;

		case PT.IMAGE:
			if (Constants.THUMBNAIL.equals(element)) {
				byte[] thumbnail = nextNode.getImageThumbnail(type, id, field);
				if (thumbnail != null) {
					content = new Content(thumbnail, Format.PNG);
				}
			} else {
				throw new ElementException(type, field, element, Constants.INVALID_ELEMENT);
			}
			break;

		default:
			throw new FieldException(type, field, Constants.FIELD_HAS_NO_ELEMENTS);
		}

		if (content == null) {
			content = notFound(type, lang, view, new ElementNotFoundException(type, id, field, element));
		}

		return content;
	}
	
	@Override
	public Content selectComponent(String type, String lang, String view, FieldReference ref,
			Filter[] filters, String search, LinkedHashMap<String, Order> order, Long offset,
			Long limit, Component component) {
		
		document = new HTML();
		document.setDocType(null);
		
		Element select = selectElement(type, lang, view, ref, filters, search, order, offset, limit,
				component);

		return new Content(select.toString());
	}

	@Override
	public Content select(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {
		
		loadTemplate(type, lang, view);
				
		String title = strings.gts(type, Constants.SELECT_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));

		if (ref != null) {
			main.appendElement(referenceOutput(type, lang, view, ref, filters, search, order));
		}
		
		if (search != null) {
			main.appendElement(searchOutput(type, lang, view, ref, filters, search, order));
		}
		
		LinkedHashMap<String, TypeField> typeFields = nextNode.getTypeFields(type);
		
		main.appendElement(filters(type, filters, typeFields, lang));	
		
		Element select = selectElement(type, typeFields, lang, view, ref, filters, search, order,
				offset, limit, Component.TYPE);

		main.appendElement(select);
		
		if (search != null) {
			String[] searchTypes = typeSettings.getTypeStringArray(type, Constants.FULLTEXT_SEARCH_TYPES);
			if (searchTypes != null) {
				main.appendElement(HTML.H2).appendText(strings.gts(type, Constants.OTHER_TYPES));
				
				for (String searchType : searchTypes) {
					main.appendElement(selectElement(searchType, lang, view, null, null, search, null,
							0L, null, Component.REFERENCE));
				}
			}
		}

		return render(type);
	}
	
	public Element searchOutput(String type, String lang, String view, FieldReference ref,
			Filter[] filters, String search, LinkedHashMap<String, Order> order) {
		Element div = document.createElement(HTML.DIV).addClass(SEARCH_OUTPUT);
		div.appendElement(HTML.STRONG).appendText(strings.gts(type, Constants.SEARCH) + ": ");
		div.appendText(search);
		
		String uri = deleteSearchURI(type, lang, view, ref, filters, order);
		
		div.appendElement(iconAnchor(strings.gts(type, Constants.DELETE_SEARCH), uri, Icon.DELETE));
		return div;
	}
	
	public String deleteSearchURI(String type, String lang, String view, FieldReference ref,
			Filter[] filters, LinkedHashMap<String, Order> order) {
		return uri(type, lang, view) + refParameter(ref) + filtersParameters(filters)
			+ previewParameter(request.isPreview()) + orderParameter(order);
	}

	@Override
	public Content updateForm(String type, String id, String lang, String view) {
		loadTemplate(type, lang, view);
		String[] fields = typeSettings.getActionStringArray(type, Action.UPDATE, Constants.FIELDS);
		
		NXObject object = nextNode.get(type, id, fields, lang, true, false, false, false, false, false);

		if (object == null) {
			return objectNotFound(type, id, lang, view);
		}

		String title = strings.gts(type, Constants.UPDATE_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));

		textEditors();
		main.appendElement(updateForm(object, fields, lang, view));
		main.appendElement(downReferences(type, id, lang, view));

		return render(type);
	}

	@Override
	public Content updateIdForm(String type, String id, String lang, String view) {
		loadTemplate(type, lang, view);
		String title = strings.gts(type, Constants.UPDATE_ID_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));
		main.appendElement(updateIdFormElement(type, id, lang, view));
		return render(type);
	}

	@Override
	public Content updatePasswordForm(String type, String id, String field, String lang, String view) {
		loadTemplate(type, lang, view);
		String title = strings.gts(type, Constants.UPDATE_PASSWORD_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));
		main.appendElement(updatePasswordFormElement(type, id, field, lang, view));
		return render(type);
	}

	public HTML getDocument() {
		return document;
	}

	@Override
	public Node getNextNode() {
		return nextNode;
	}

	public Settings getSettings() {
		return settings;
	}

	public TypeSettings getTypeSettings() {
		return typeSettings;
	}

	public Strings getStrings() {
		return strings;
	}

	public HTTPRequest getRequest() {
		return request;
	}

	public String humanReadableBytes(Integer bytes, String lang) {
		return humanReadableBytes(bytes.longValue(), lang);
	}

	public String humanReadableBytes(Long bytes, String lang) {
		String humanReadableBytes = null;

		if (bytes < 1024) {
			humanReadableBytes = bytes + " B";
		} else {
			if (humanReadableBytesFormat == null) {
				humanReadableBytesFormat = (DecimalFormat) DecimalFormat.getInstance(new Locale(lang));
				humanReadableBytesFormat.applyPattern("#.## ");
			}

			int exponent = (int) (Math.log(bytes) / Math.log(1024));
			String unit = "KMGTPEZY".charAt(exponent - 1) + "iB";
			humanReadableBytes = humanReadableBytesFormat.format(bytes / Math.pow(1024, exponent)) + unit;
		}

		return humanReadableBytes;
	}

	public Element typesTable(TypeInfo[] types, String lang, String view) {
		
		boolean disableDropButton = true;
		boolean disableExportButton = true;
		
		Element form = form(lang, view);
		form.setAttribute(HTML.AUTOCOMPLETE, HTML.OFF).setAttribute(DATA_STRINGS_TYPES_DROP_CONFIRMATION,
				strings.gts(Constants.TYPES_DROP_CONFIRMATION));

		Element table = form.appendElement(HTML.TABLE);
		Element header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		Element body = table.appendElement(HTML.TBODY);

		Element allCheckbox = header.appendElement(HTML.TH).appendElement(allCheckbox());
		
		header.appendElement(HTML.TH).appendText(strings.gts(Constants.NAME));
		header.appendElement(HTML.TH).appendText(strings.gts(Constants.OBJECTS));
		header.appendElement(HTML.TH).appendText(strings.gts(Constants.SIZE));
		header.appendElement(HTML.TH);
		header.appendElement(HTML.TH);
		header.appendElement(HTML.TH);

		for (TypeInfo typeInfo : types) {
			String type = typeInfo.getName();
			String typeName = strings.getTypeName(type);
			
			boolean disableCheckbox = true;
			
			if (permissions.isAllowed(type, Action.DROP)) {
				disableDropButton = false;
				disableCheckbox = false;
			}
			
			if (permissions.isAllowed(type, Action.EXPORT_TYPES)) {
				disableExportButton = false;
				disableCheckbox = false;
			}
			
			Element row = body.appendElement(HTML.TR);
			
			Element checkbox = row.appendElement(HTML.TD)
					.appendElement(input(HTML.CHECKBOX, Constants.TYPES, typeName, type)
					.addClass(ITEM_CHECKBOX));
			if (disableCheckbox) {
				checkbox.setAttribute(HTML.DISABLED);
			}
			
			Element selectCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(type, Action.SELECT)) {
				selectCell.appendElement(anchor(typeName, uri(type, lang, view)));
			} else {
				selectCell.appendText(typeName);
			}			
			
			row.appendElement(HTML.TD).appendText(typeInfo.getObjects() + "");
			row.appendElement(HTML.TD).appendText(humanReadableBytes(typeInfo.getSize(), lang));
			
			Element insertCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(type, Action.INSERT_FORM)) {
				insertCell.appendElement(iconAnchor(strings.getActionName(type, Action.INSERT),
					uri(type, lang, view) + formParameter(Action.INSERT), Icon.PLUS));
			}
			
			Element alterCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(type, Action.ALTER_FORM)) {
				alterCell.appendElement(iconAnchor(strings.getActionName(type, Action.ALTER),
						uri(type, lang, view) + formParameter(Action.ALTER), Icon.PENCIL));
			}
			
			Element infoCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(type, Action.GET_TYPE)) {
				infoCell.appendElement(iconAnchor(strings.gts(type, Constants.TYPE),
						uri(type, lang, view) + "&" + Constants.INFO, Icon.INFO));
			}
		}
		
		if (disableDropButton && disableExportButton) {
			allCheckbox.setAttribute(HTML.DISABLED);
		}

		String actionName = strings.getActionName(null, Action.DROP);

		Element actionButton = form.appendElement(button(actionName, Action.DROP, Icon.MINUS,
				SUBMIT_FORM));
		if (disableDropButton) {
			actionButton.setAttribute(HTML.DISABLED);
		}
		
		form.appendElement(exportButton(disableExportButton));

		return form;
	}
	
	public Element allCheckbox() {
		return allCheckbox(null);
	}
	public Element allCheckbox(String type) {
		return document.createElement(HTML.INPUT)
				.setAttribute(HTML.TYPE, HTML.CHECKBOX)
				.setAttribute(HTML.TITLE, strings.gts(type, Constants.CHECK_UNCHECK_ALL))
				.addClass(ALL_CHECKBOX);
	}
	
	public Element insertForm(String type, String[] fields, String lang, String view,
			FieldReference ref) {
		
		boolean showType = typeSettings.getActionBoolean(type, Action.INSERT, Constants.SHOW_TYPE);
		boolean showId = typeSettings.getActionBoolean(type, Action.INSERT, Constants.SHOW_ID);
		boolean showHeader = typeSettings.getActionBoolean(type, Action.INSERT, Constants.SHOW_HEADER);
		boolean showProgress = typeSettings.getActionBoolean(type, Action.INSERT, Constants.SHOW_PROGRESS);
		
		return insertForm(type, fields, lang, view, ref, showType, showId, showHeader, showProgress);
	}
	
	public Element insertForm(String type, String[] fields, String lang, String view,
			FieldReference ref, boolean showType, boolean showId, boolean showHeader,
			boolean showProgress) {	
		
		LinkedHashMap<String, TypeField> typeFields = nextNode.getTypeFields(type, fields);
		
		Element form = multipartForm(type, lang, view);
		if (showProgress) {
			form.setAttribute(DATA_SHOW_PROGRESS);
		}

		Element table = form.appendElement(HTML.TABLE);

		Element header = null;

		if (showHeader) {
			header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		}

		Element body = table.appendElement(HTML.TBODY);

		if (showHeader) {
			if (showType) {
				header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.TYPE));
			}

			header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.NAME));
			header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.VALUE));
		}

		if (showId) {
			Element row = body.appendElement(HTML.TR);

			if (showType) {
				row.appendElement(HTML.TD).appendText(PT.STRING);
			}
			
			String idName = strings.getIdName(type);
			
			row.appendElement(HTML.TD).appendText(idName);
			row.appendElement(HTML.TD).appendElement(idInput(type, Constants.ID, idName));
		}

		if (ref != null) {
			String field = ref.getField();
			
			if (!ArrayUtils.contains(fields, field)) {
				form.appendElement(input(HTML.HIDDEN, "@" + field, null, ref.getId()));
			}
		}
		
		boolean appendFieldAnchor = permissions.isAllowed(type, Action.GET_FIELD_DEFAULT);
		
		for (Entry<String, TypeField> entry : typeFields.entrySet()) {
			String field = entry.getKey();
			TypeField typeField = entry.getValue();

			String fieldName = strings.getFieldName(type, field);

			Element row = body.appendElement(HTML.TR);

			if (showType) {
				row.appendElement(HTML.TD).appendText(typeField.getType());
			}
			
			Object value = nextNode.getFieldDefault(type, field);
			
			if (value instanceof byte[]) {
				value = ((byte[]) value).length;
			} else if (value instanceof File) {
				value = ((File) value).getContent().length;
			}

			Element fieldCell = row.appendElement(HTML.TD);
			
			if (appendFieldAnchor && value != null) {
				fieldCell.appendElement(anchor(fieldName, "/" + type + "/id/" + field + "?" 
						+ Constants.DEFAULT));
			} else {
				fieldCell.appendText(fieldName);
			}

			row.appendElement(insertFormCell(type, field, fieldName, value, typeField, ref, lang));
		}
		
		String actionName = strings.getActionName(type, Action.INSERT);

		Element actionButton = form.appendElement(button(actionName, Action.INSERT, Icon.PLUS,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, Action.INSERT)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return form;
	}

	public Element multipartForm(String lang, String view) {
		return multipartForm(null, null, null, lang, view);
	}

	public Element multipartForm(String type, String lang, String view) {
		return multipartForm(type, null, null, lang, view);
	}

	public Element multipartForm(String type, String id, String lang, String view) {
		return multipartForm(type, id, null, lang, view);
	}

	public Element multipartForm(String type, String id, String field, String lang, String view) {
		return form(type, id, field, lang, view).setAttribute(HTML.ENCTYPE, HTML.MULTIPART_FORM_DATA);
	}

	public Element form(String lang, String view) {
		return form(null, null, null, lang, view);
	}

	public Element form(String type, String lang, String view) {
		return form(type, null, null, lang, view);
	}

	public Element form(String type, String id, String lang, String view) {
		return form(type, id, null, lang, view);
	}

	public Element form(String type, String id, String field, String lang, String view) {
		return form(uri(type, id, field, lang, view))
				.setAttribute(DATA_STRINGS_ACCEPT, strings.gts(type, Constants.ACCEPT))
				.setAttribute(DATA_STRINGS_CANCEL, strings.gts(type, Constants.CANCEL));
	}

	public Element form(String action) {
		Element form = document.createElement(HTML.FORM).setAttribute(HTML.ACTION, action);

		if (request.isSecure()) {
			form.appendElement(input(HTML.HIDDEN, Constants.SESSION, Constants.SESSION, request.getSessionToken()));
		}

		return form;
	}

	public Element multipartForm(String action) {
		return form(action).setAttribute(HTML.ENCTYPE, HTML.MULTIPART_FORM_DATA);
	}

	public void loadTemplate(String type, String lang, String view) {
		loadTemplate(type, lang, view, null);
	}

	public void loadTemplate(String type, String lang, String view, String template) {
		if (template == null) {
			template = strings.gts(type, Constants.TEMPLATE);
			
			if (template == null) {
				template = typeSettings.gts(type, Constants.TEMPLATE);
			}
		}

		document = context.getTemplate(template, lang);

		head = document.getHead();
		main = document.getMain();
		footer = document.getFooter();

		initTemplate(type, lang, view);
	}

	public void initTemplate(String type, String lang, String view) {
		head(type, lang, view);
		logo(type, lang, view);
		user(type, lang, view);
		langs(lang);
		search(type, lang, view, request.getRef(), request.getSearch(), request.getOrder(),
				request.getOffset(), request.getLimit());
		menu(type, lang, view);
		typeMenu(type, request.getId(), lang, view, request.getRef(), request.getSearch(), 
				request.getComponent());
		rss(type, lang);
		actions(type, request.getId(), lang, view);
		qrcode(type, request.getId());
		validators(type);
		footer(type);
	}

	public void setTitle(String title) {
		main.appendElement(HTML.H1).appendText(title);
		document.getTitle().appendText(title);
	}
	
	public Element selectElement(String type, String lang, String view, FieldReference ref,
			Filter[] filters, String search, LinkedHashMap<String, Order> order, Long offset,
			Long limit, Component component) {
	
		return selectElement(type, nextNode.getTypeFields(type), lang, view, ref, filters, search,
				order, offset, limit, component);
	}

	public Element selectElement(String type, LinkedHashMap<String, TypeField> typeFields, String lang,
			String view, FieldReference ref, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit, Component component) {

		Filter[] refAndFilters = null;
		
		if (ref != null) {
			Filter refFilter = new Filter(ref.getField(), Comparison.EQUAL, ref.getId(), false);
			
			if (filters != null) {
				refAndFilters = (Filter[]) ArrayUtils.add(filters, refFilter);
			} else {
				refAndFilters = new Filter[] { refFilter };
			}
		} else {
			refAndFilters = filters;
		}
		
		Element select = document.createElement(HTML.DIV);
		select.addClass(HTML.SELECT);

		String[] fields = typeSettings.getActionStringArray(type, Action.SELECT, Constants.FIELDS);
		Objects result = nextNode.select(type, fields, lang, refAndFilters, search, order, offset, limit);
		NXObject[] objects = result.getItems();
		Long count = result.getCount();
		offset = result.getOffset();
		limit = result.getLimit();
		
		HTMLView htmlView = null;

		if (Component.REFERENCE.equals(component)) {
			htmlView = getHTMLView(type, view);
			if (ref != null) {
				ref.setType(typeFields.get(ref.getField()).getType());
			}
			
			select.appendElement(htmlView.referenceSelectHeader(type, lang, view, ref, search, count, 
					component));
		} else {
			htmlView = this;
			select.appendElement(HTML.P).appendText(" " + count + " " + strings.gts(type, Constants.OBJECTS));		
		}

		if (objects != null && objects.length > 0) {
			select.appendElement(htmlView.selectTable(type, objects, typeFields, lang, view, ref,
					filters, search, order, count, offset, limit, result.getMinLimit(), result.getMaxLimit(),
					result.getLimitIncrement(), component));
		}

		return select;
	}
	
	public Element referenceSelectHeader(String type, String lang, String view, FieldReference ref,
			String search, Long count, Component component) {
		
		Element selectHeader = document.createElement(HTML.DIV)
				.addClass(SELECT_HEADER);
		
		String referenceName = strings.getReferenceName(type, ref);
		
		selectHeader.appendElement(HTML.STRONG).appendText(referenceName);
		selectHeader.appendText(" " + count + " " + strings.gts(type, Constants.OBJECTS));
		selectHeader.appendElement(HTML.NAV).addClass(SELECT_MENU)
				.appendElements(typeMenuElements(type, null, lang, view, ref, search, component));
		
		return selectHeader;
	}
	
	@Override
	public Content filterComponent(String type, String field, String lang, String view, int count) {
		document = new HTML();
		document.setDocType(null);
		
		if (field == null) {
			field = Constants.ID;
		}
		
		Element filter = filter(type, new Filter(field, Comparison.EQUAL, null, true), count,
				nextNode.getTypeFields(type), lang);
		
		return new Content(filter.toString());
	}
	
	public Element filters(String type, Filter[] filters, LinkedHashMap<String, TypeField> typeFields,
			String lang) {
		
		Element div = document.createElement(HTML.DIV);
		
		div.appendElement(HTML.STRONG).appendText(strings.gts(type, Constants.FILTERS) + ": ");
		div.appendElement(button(strings.gts(type, Constants.ADD_FILTER), ADD_FILTER));
		div.appendElement(document.createElement(HTML.BUTTON))
			.setAttribute(HTML.TYPE, HTML.SUBMIT)
			.setAttribute(HTML.FORM, Constants.SEARCH)
			.appendText(strings.gts(type, Constants.SEARCH));
		
		Element table = div.appendElement(HTML.TABLE).setAttribute(HTML.ID, Constants.FILTERS);
		Element header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.FIELD));
		header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.COMPARISON));
		header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.VALUE));
		header.appendElement(HTML.TH);
		
		Element body = table.appendElement(HTML.TBODY);
		
		for (int x = 0; x < filters.length; x++) {
			body.appendElement(filter(type, filters[x], x, typeFields, lang));			
		}
		
		return div;
	}
	
	public Element filter(String type, Filter filter, int count, LinkedHashMap<String, TypeField> typeFields,
			String lang) {
		
		Element row = document.createElement(HTML.TR);
		
		String filterField = filter.getField();
		
		Element fieldSelect = row.appendElement(HTML.TD).appendElement(HTML.SELECT)
				.setAttribute(HTML.CLASS, FILTER_FIELD)
				.setAttribute(HTML.NAME, Constants.FILTERS + ":" + count + ":" + Constants.FIELD)
				.setAttribute(HTML.FORM, Constants.SEARCH);
				
		Element option = fieldSelect.appendElement(HTML.OPTION)
				.setAttribute(HTML.VALUE, Constants.ID)
				.appendText(strings.getIdName(type));
		
		if (Constants.ID.equals(filterField)) {
			option.setAttribute(HTML.SELECTED);
		}
	
		for (Map.Entry<String, TypeField> entry : typeFields.entrySet()) {
			String fieldType = entry.getValue().getType();
			
			if (!PT.isPrimitiveType(fieldType) || PT.isFilterType(fieldType)) {
				String field = entry.getKey();
				option = fieldSelect.appendElement(HTML.OPTION)
						.setAttribute(HTML.VALUE, field)
						.appendText(strings.getFieldName(type, field));
			
				if (field.equals(filterField)) {
					option.setAttribute(HTML.SELECTED);
				}
			}
		}
		
		Element comparisonSelect = row.appendElement(HTML.TD).appendElement(HTML.SELECT)
				.setAttribute(HTML.NAME, Constants.FILTERS + ":" + count + ":" + Constants.COMPARISON)
				.setAttribute(HTML.FORM, Constants.SEARCH);
	
		for (Comparison comparison : Comparison.values()) {
			option = comparisonSelect.appendElement(HTML.OPTION)
				.setAttribute(HTML.VALUE, comparison.toString())
				.appendText(strings.getComparisonName(type, comparison.toString()));
			
			if (comparison.equals(filter.getComparison())) {
				option.setAttribute(HTML.SELECTED);
			}
		}
		
		Element valueInput = null;
		TypeField typeField = typeFields.get(filterField);
		String valueName = Constants.FILTERS + ":" + count + ":" + Constants.VALUE;
		
		if (Constants.ID.equals(filterField)) {
			valueInput = filterObjectInput(valueName, strings.getIdName(type),
					filter.getValue(), type, true, lang);
		} else {
			if (PT.isTextType(typeField.getType())) {
				typeField = new TypeField(PT.STRING, null, null, null, typeField.isNotNull());
			}
			
			String filterFieldName = strings.getFieldName(type, filterField);
			valueInput = fieldInput(type, filterField, filterFieldName, filter.getValue(), typeField,
					lang).setAttribute(HTML.NAME, valueName);
		}
		
		valueInput.setAttribute(HTML.FORM, Constants.SEARCH);
		
		row.appendElement(HTML.TD).appendElement(valueInput);
		
		String dropFilter = strings.gts(type, Constants.DROP_FILTER);
		
		row.appendElement(HTML.TD).appendElement(smallButton(dropFilter, Icon.MINUS, DELETE_ROW))
			.setAttribute(HTML.FORM, Constants.SEARCH);
		
		return row;
	}

	public Element referenceOutput(String type, String lang, String view, FieldReference ref,
			Filter[] filters, String search, LinkedHashMap<String, Order> order) {
		
		String refType = nextNode.getTypeField(type, ref.getField()).getType();

		Element div = document.createElement(HTML.DIV).addClass(REFERENCE_OUTPUT);
		div.appendElement(HTML.STRONG).appendText(strings.getFieldName(type, ref.getField()) + ": ");
		div.appendElement(anchor(nextNode.getName(refType, ref.getId(), lang), uri(refType, ref.getId(), lang, view)));
		
		String uri = uri(type, lang, view) + filtersParameters(filters) + searchParameter(search)
			+ orderParameter(order);
		div.appendElement(iconAnchor(strings.gts(type, Constants.DELETE_REFERENCE), uri, Icon.DELETE));
		
		return div;
	}

	public Element downReferences(String refType, String refId, String lang, String view) {
		Element references = document.createElement(HTML.DIV);

		for (TypeReference downReference : nextNode.getDownReferences(refType)) {
			FieldReference ref = new FieldReference(downReference.getField(), refType, refId);
			references.appendElement(selectElement(downReference.getType(), lang, view, ref, null, null,
					null, 0L, null, Component.REFERENCE));
		}

		return references;
	}
	
	public Element updateForm(NXObject object, String[] fields, String lang, String view) {
				
		String type = object.getType();
		
		boolean showType = typeSettings.getActionBoolean(type, Action.UPDATE, Constants.SHOW_TYPE);
		boolean showId = typeSettings.getActionBoolean(type, Action.UPDATE, Constants.SHOW_ID);
		boolean showHeader = typeSettings.getActionBoolean(type, Action.UPDATE, Constants.SHOW_HEADER);
		boolean showProgress = typeSettings.getActionBoolean(type, Action.UPDATE, Constants.SHOW_PROGRESS);
		
		return updateForm(object, fields, lang, view, showType, showId, showHeader, showProgress);
	}
	
	public Element updateForm(NXObject object, String[] fields, String lang, String view,
			boolean showType, boolean showId, boolean showHeader, boolean showProgress) {
		
		String type = object.getType();
		LinkedHashMap<String, TypeField> typeFields = nextNode.getTypeFields(type, fields);
		
		Element form = multipartForm(type, object.getId(), lang, view).addClass(UNLOAD_CONFIRMATION)
				.setAttribute(HTML.AUTOCOMPLETE, HTML.OFF);

		if (showProgress) {
			form.setAttribute(DATA_SHOW_PROGRESS);
		}

		Element table = form.appendElement(HTML.TABLE);
		Element header = null;

		if (showHeader) {
			header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		}

		Element body = table.appendElement(HTML.TBODY);

		if (showHeader) {
			if (showType) {
				header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.TYPE));
			}

			header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.NAME));
			header.appendElement(HTML.TH).appendText(strings.gts(type, Constants.VALUE));
		}

		form.appendElement(input(HTML.HIDDEN, Constants.UDATE, Constants.UDATE, object.getUDate()));

		Element row = body.appendElement(HTML.TR);

		if (showType) {
			row.appendElement(HTML.TD).appendText(PT.STRING);
		}

		if (showId) {
			row.appendElement(HTML.TD).appendText(strings.getIdName(type));
			
			Element idCell = row.appendElement(HTML.TD);
			idCell.appendText(object.getId());
			
			if (permissions.isAllowed(type, Action.UPDATE_ID_FORM)) {
				idCell.appendText(" ");
				idCell.appendElement(iconAnchor(strings.getActionName(type, Action.UPDATE_ID),
						uri(object.getType(), object.getId(), lang, view)
							+ formParameter(Action.UPDATE_ID), Icon.PENCIL));
			}
		}
		
		boolean appendFieldAnchor = permissions.isAllowed(type, Action.GET_FIELD);

		for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
			String field = entry.getKey();
			Object value = entry.getValue();
			TypeField typeField = typeFields.get(field);

			String fieldName = strings.getFieldName(object.getType(), field);

			row = body.appendElement(HTML.TR);

			if (showType) {
				row.appendElement(HTML.TD).appendText(typeField.getType());
			}

			Element fieldCell = row.appendElement(HTML.TD);
			
			if (appendFieldAnchor) {
				fieldCell.appendElement(anchor(fieldName, "/" + object.getType() + "/" + object.getId() + "/" + field));
			} else {
				fieldCell.appendText(fieldName);
			}
			
			row.appendElement(updateFormCell(object, field, fieldName, value, typeField, lang, view));
		}

		String actionName = strings.getActionName(object.getType(), Action.UPDATE);

		Element actionButton = form.appendElement(button(actionName, Action.UPDATE, Icon.PENCIL,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, Action.UPDATE)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return form;
	}

	public Element updateIdFormElement(String type, String id, String lang, String view) {
		String newId = strings.gts(type, Constants.NEW_ID);

		Element form = form(type, id, lang, view);
		Element table = form.appendElement(HTML.TABLE);
		Element body = table.appendElement(HTML.TBODY);

		Element row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(strings.getIdName(type) + ":");
		row.appendElement(HTML.TD).appendText(id);

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(newId + ":");
		row.appendElement(HTML.TD).appendElement(idInput(type, Constants.NEW_ID, newId));

		String actionName = strings.getActionName(type, Action.UPDATE_ID);

		Element actionButton = form.appendElement(button(actionName, Action.UPDATE_ID, Icon.PENCIL,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, Action.UPDATE_ID)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return form;
	}
	
	public Element updatePasswordFormElement(String type, String id, String field, String lang, String view) {
		Element form = form(type, id, field, lang, view);
		Element table = form.appendElement(HTML.TABLE);
		Element body = table.appendElement(HTML.TBODY);

		String currentPassword = strings.gts(type, Constants.CURRENT_PASSWORD);
		String newPassword = strings.gts(type, Constants.NEW_PASSWORD);
		String repeatNewPassword = strings.gts(type, Constants.NEW_PASSWORD_REPEAT);

		Element row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(strings.getIdName(type) + ":");
		row.appendElement(HTML.TD).appendText(id);

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(currentPassword + ":");
		row.appendElement(HTML.TD).appendElement(input(HTML.PASSWORD, Constants.CURRENT_PASSWORD, currentPassword));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(newPassword + ":");
		row.appendElement(HTML.TD).appendElement(input(HTML.PASSWORD, Constants.NEW_PASSWORD, newPassword));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(repeatNewPassword + ":");
		row.appendElement(HTML.TD)
				.appendElement(input(HTML.PASSWORD, Constants.NEW_PASSWORD_REPEAT, repeatNewPassword));

		String actionName = strings.getActionName(type, Action.UPDATE_PASSWORD);

		Element actionButton = form.appendElement(button(actionName, Action.UPDATE_PASSWORD, Icon.PENCIL,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, Action.UPDATE_PASSWORD)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return form;
	}

	public Element insertFormCell(String type, String field, String title, Object value, 
			TypeField typeField, FieldReference ref, String lang) {
		Element cell = document.createElement(HTML.TD);
		Element input = null;

		if (ref != null && field.equals(ref.getField())) {
			input = document.createElement(HTML.DIV)
					.appendElement(input(HTML.HIDDEN, "@" + ref.getField(), title, ref.getId()));
			cell.addClass(REFERENCE_FIELD);
			cell.appendText(nextNode.getName(typeField.getType(), ref.getId(), lang));
		} else {
			input = fieldInput(type, field, title, value, typeField, lang);
		}

		cell.appendElement(input);
		return cell;
	}

	public Element updateFormCell(NXObject object, String field, String title, Object value, TypeField typeField,
			String lang, String view) {
		Element cell = document.createElement(HTML.TD);

		if (typeField.getType().equals(PT.PASSWORD)) {
			cell.appendElement(passwordFieldOutput(object.getType(), object.getId(), field, lang, view));
		} else {
			cell.appendElement(fieldInput(object.getType(), field, title, value, typeField, lang));
		}

		return cell;
	}

	public Element passwordFieldOutput(String type, String id, String field, String lang, String view) {
		Element password = document.createElement(HTML.SPAN);
		password.appendText(Security.HIDDEN_PASSWORD + " ");
		
		if (permissions.isAllowed(type, Action.UPDATE_PASSWORD_FORM)) {
			password.appendElement(iconAnchor(strings.getActionName(type, Action.UPDATE_PASSWORD),
				uri(type, id, field, lang, view) + formParameter(Action.UPDATE_PASSWORD), Icon.PENCIL));
		}
		
		return password;
	}

	public Element fieldInput(String type, String field, String title, Object value, TypeField typeField,
			String lang) {
		
		Integer size = typeSettings.getFieldInt32(type, field, Constants.INPUT_SIZE);
		
		return fieldInput(type, null, field, title, value, typeField, size, lang);
	}
	
	public Element actionFieldInput(String type, String action, String field, String title, Object value,
			TypeField typeField, String lang) {
		
		Integer size = typeSettings.getActionFieldInt32(type, action, field, Constants.INPUT_SIZE);

		return fieldInput(type, action, field, title, value, typeField, size, lang);
	}

	public Element fieldInput(String type, String action, String field, String title, Object value,
			TypeField typeField, Integer size, String lang) {

		Element input = null;
		
		switch (typeField.getType()) {
		case PT.STRING:
		case PT.INT16:
		case PT.INT32:
		case PT.INT64:
		case PT.FLOAT32:
		case PT.FLOAT64:
		case PT.NUMERIC:
		case PT.URI:
		case PT.EMAIL:
		case PT.DATE:
		case PT.TEL:
		case PT.TIME:
		case PT.DATETIME:
		case PT.COLOR:
			input = fieldInput(field, title, value, typeField, size);
			break;
		case PT.TEXT:
		case PT.HTML:
		case PT.JSON:
		case PT.XML:
			input = textareaFieldInput(type, action, field, title, value, typeField);
			break;
		case PT.BINARY:
		case PT.FILE:
		case PT.IMAGE:
		case PT.DOCUMENT:
		case PT.AUDIO:
		case PT.VIDEO:
			input = binaryFieldInput(type, action, field, title, value, typeField, lang);
			break;
		case PT.TIMEZONE:
			input = timezoneFieldInput(field, title, value, typeField);
			break;
		case PT.BOOLEAN:
			input = booleanFieldInput(field, title, value);
			break;
		case PT.PASSWORD:
			input = passwordFieldInput(type, field, title);
			break;
		default:
			input = objectFieldInput(type, action, field, title, value, typeField, size, lang);
		}

		return input;
	}

	public Element binaryFieldInput(String type, String action, String field, String title, Object value,
			TypeField typeField, String lang) {
		
		String allowedContentTypes = null;

		if (action != null) {
			allowedContentTypes = typeSettings.getActionFieldString(type, action, field,
					Constants.ALLOWED_CONTENT_TYPES);
		} else {
			allowedContentTypes = typeSettings.getFieldString(type, field, Constants.ALLOWED_CONTENT_TYPES);
		}

		if (allowedContentTypes == null && PT.IMAGE.equals(typeField.getType())) {
			allowedContentTypes = Format.IMAGES.getContentType();
		}

		Element input = binaryInput("@" + field, title, value, allowedContentTypes, lang);
		Element clearAnchor = iconAnchor(strings.gts(type, Constants.CLEAR), null, Icon.DELETE)
				.addClasses(CLEAR_BINARY_INPUT, HTML.HIDDEN);
		
		input.appendElement(clearAnchor);
		
		if (action == null && !typeField.isNotNull()) {
			input.appendElement(nullFieldInput(type, field, value));
		}
		
		return input;
	}
	
	public Element nullFieldInput(String type, String field, Object value) {
		Element nullFieldInput = document.createElement(HTML.SPAN).addClass(NULL_FIELD_INPUT);
		
		String nullName = strings.gts(type, Constants.NULL);
		
		nullFieldInput.appendText(" | " + nullName + ":");
	
		nullFieldInput.appendElement(booleanInput("@" + field + Constants._NULL, nullName, false))
			.addClass(Constants.NULL);
		
		if (value == null) {
			nullFieldInput.addClass(HTML.HIDDEN);
		} 
		
		return nullFieldInput;
	}

	public Element binaryInput(String name, String title, String allowedContentTypes, String lang) {
		return binaryInput(name, title, null, allowedContentTypes, lang);
	}

	public Element binaryInput(String name, String title, Object value, String allowedContentTypes,
			String lang) {
		Element binaryInput = document.createElement(HTML.SPAN);

		Element input = binaryInput.appendElement(input(HTML.FILE, name, title)).addClass(PT.BINARY);

		if (allowedContentTypes != null) {
			input.setAttribute(HTML.ACCEPT, allowedContentTypes);
		}

		Element binaryInputSize = binaryInput.appendElement(HTML.SPAN).addClass(BINARY_INPUT_SIZE);

		if (value == null) {
			value = 0;
		}

		String size = humanReadableBytes((Integer) value, lang);
		
		binaryInputSize.appendText(size);
		binaryInputSize.setAttribute(DATA_SIZE, size);
		
		return binaryInput;
	}

	public Element passwordFieldInput(String type, String field, String title) {
		Element input = document.createElement(HTML.SPAN);

		input.appendElement(input(HTML.PASSWORD, "@" + field, title))
			.setAttribute(HTML.MAXLENGTH, Security.BCRYPT_MAX_PASSWORD_LENGTH);

		input.appendText(strings.gts(type, Constants.REPEAT) + ": ");

		input.appendElement(input(HTML.PASSWORD, "@" + field + Constants._REPEAT, title))
			.setAttribute(HTML.MAXLENGTH, Security.BCRYPT_MAX_PASSWORD_LENGTH);

		return input;
	}

	public Element fieldInput(String field, String title, Object value, TypeField typeField,
			Integer size) {
		
		String inputType = null;

		switch (typeField.getType()) {
		case PT.STRING:
			inputType = HTML.TEXT;
			break;
		case PT.URI:
			inputType = HTML.URL;
			break;
		case PT.EMAIL:
			inputType = HTML.EMAIL;
			break;
		case PT.DATE:
			inputType = HTML.DATE;
			break;
		case PT.TEL:
			inputType = HTML.TEL;
			break;
		case PT.TIME:
			inputType = HTML.TIME;
			break;
		case PT.DATETIME:
			inputType = HTML.DATETIME_LOCAL;
			break;
		case PT.COLOR:
			inputType = HTML.COLOR;
			break;
		case PT.INT16:
		case PT.INT32:
		case PT.INT64:
		case PT.FLOAT32:
		case PT.FLOAT64:
		case PT.NUMERIC:
			inputType = HTML.NUMBER;
			break;
		}

		return fieldInput(field, title, value, typeField, size, inputType);
	}

	public Element fieldInput(String field, String title, Object value, TypeField typeField,
			Integer size, String inputType) {
		
		Element input = input(inputType, "@" + field, title, value);

		if (inputType.equals(HTML.NUMBER)) {
			setMaxMinValues(input, typeField);
		}

		if (PT.isStringType(typeField.getType())) {
			setMaxLength(input, typeField);
			setSize(input, size);
		}

		if (!inputType.equals(HTML.COLOR)) {
			setRequired(input, typeField);
		}

		return input;
	}

	public Element input(String inputType, String name, String title) {
		return input(inputType, name, title, (Object) null);
	}

	public Element input(String inputType, String name, String title, Object value) {
		Element input = document.createElement(HTML.INPUT).setAttribute(HTML.TYPE, inputType)
				.setAttribute(HTML.NAME, name);

		if (title == null) {
			title = name;
		}

		input.setAttribute(HTML.TITLE, title);

		if (value != null) {
			input.setAttribute(HTML.VALUE, value.toString());
		}

		return input;
	}

	public void setMaxLength(Element input, TypeField typeField) {
		Long maxLength = typeField.getLength();
		if (maxLength != null) {
			input.setAttribute(HTML.MAXLENGTH, maxLength);
		}
	}
	
	public void setSize(Element input, Integer size) {
		if (size != null) {
			input.setAttribute(HTML.SIZE, size);
		}
	}

	public void setMaxMinValues(Element input, TypeField typeField) {
		Object min = null;
		Object max = null;

		switch (typeField.getType()) {
		case PT.INT16:
			min = Short.MIN_VALUE;
			max = Short.MAX_VALUE;
			break;
		case PT.INT32:
			min = Integer.MIN_VALUE;
			max = Integer.MAX_VALUE;
			break;
		case PT.INT64:
			min = Long.MIN_VALUE;
			max = Long.MAX_VALUE;
			break;
		case PT.FLOAT32:
			min = Float.MIN_VALUE;
			max = Float.MAX_VALUE;
			break;
		case PT.FLOAT64:
			min = Double.MIN_VALUE;
			max = Double.MAX_VALUE;
			break;
		case PT.NUMERIC:
			Long precision = typeField.getPrecision();
			Long scale = typeField.getScale();
			double numericMax = Math.pow(10, precision - scale) - Math.pow(10, -scale);
			max = numericMax;
			min = -numericMax - 1;
			break;
		}

		input.setAttribute(HTML.MIN, min.toString());
		input.setAttribute(HTML.MAX, max.toString());
		input.setAttribute(HTML.STEP, HTML.ANY);
	}

	public void setRequired(Element input, TypeField typeField) {
		if (typeField.isNotNull()) {
			input.setAttribute(HTML.REQUIRED);
		}
	}

	public Element textareaOutput(Object value, boolean preview) {
		String textareaClass = null;
		if (preview) {
			textareaClass = SMALL_TEXTAREA;
			value = value + " ...";
		} else {
			textareaClass = MEDIUM_TEXTAREA;
		}

		Element textarea = document.createElement(HTML.TEXTAREA);
		textarea.appendText(value.toString());
		textarea.addClass(textareaClass);
		return textarea;
	}

	public Element documentFieldOutput(String type, String id, String field, Object value, String lang, boolean preview) {

		DocumentPreview docPrev = (DocumentPreview) value;

		Element span = document.createElement(HTML.SPAN);
		span.appendElement(textareaOutput(docPrev.getText(), preview));
		span.appendElement(binaryFieldOutput(type, id, field, docPrev.getSize(), lang));

		return span;
	}
	
	public Element textareaFieldInput(String type, String action, String field, String title, Object value,
			TypeField typeField) {
		return textareaFieldInput(type, action, field, title, value, typeField.getType());
	}

	public Element textareaFieldInput(String type, String action, String field, String title,
			Object value, String fieldType) {
		Element textarea = document.createElement(HTML.TEXTAREA).setAttribute(HTML.NAME, "@" + field)
				.setAttribute(HTML.TITLE, title);

		if (value != null) {
			textarea.appendText(value.toString());
		}

		if (fieldType != null) {
			textarea.addClass(fieldType);
		}

		String[] modes = null;
		
		if (action != null) {
			modes = typeSettings.getActionFieldStringArray(type, action, field, Constants.EDITOR);
		} else {
			modes = typeSettings.getFieldStringArray(type, field, Constants.EDITOR);
		}

		if (modes == null || modes.length == 0) {
			switch (fieldType) {
			case PT.JSON:
				modes = new String[] { JSON };
				break;

			case PT.XML:
			case PT.HTML:
				modes = new String[] { XML };
				break;
			}
		}

		if (modes != null && modes.length > 0) {
			textarea.setAttribute(DATA_EDITOR, modes[0]);

			for (String mode : modes) {
				if (!VISUAL.equals(mode)) {
					if (textEditorModes == null) {
						textEditorModes = new ArrayList<>();
					}

					if (!textEditorModes.contains(mode)) {
						textEditorModes.add(mode);
						head.appendElement(textEditorMode(mode));
					}
				}
			}
		}

		return textarea;
	}
	
	public Element actionObjectsInput(String type, String action, String title, String lang) {
		Boolean notNull = typeSettings.getActionBoolean(type, action, Constants.OBJECTS_INPUT_NOT_NULL);
		String mode = typeSettings.getActionString(type, action, Constants.OBJECTS_INPUT_MODE);
		Integer size = typeSettings.getActionInt32(type, action, Constants.OBJECTS_INPUT_SIZE);
		
		return objectsInput(Constants.OBJECTS, title, null, type, notNull, mode, size, lang);
	}
	
	public Element objectFieldInput(String type, String action, String field, String title, Object value,
			TypeField typeField, Integer size, String lang) {
		
		String mode = null;
		
		if (action != null) {
			mode = typeSettings.getActionFieldString(type, action, field, Constants.OBJECT_INPUT_MODE);
		} else {
			mode = typeSettings.getFieldString(type, field, Constants.OBJECT_INPUT_MODE);
		}
		
		return objectInput("@" + field, title, value, typeField.getType(), typeField.isNotNull(), mode,
				size, lang);
	}
	
	public Element filterObjectInput(String name, String title, Object value, String type,
			boolean notNull, String lang) {
		
		String mode = typeSettings.gts(type, Constants.OBJECT_INPUT_MODE);
		Integer size = typeSettings.getTypeInt32(type, Constants.ID_INPUT_SIZE);
		
		return objectInput(name, title, value, type, notNull, mode, size, lang);
	}
	
	public Element objectInput(String name, String title, Object value, String type, 
			boolean notNull, String mode, Integer size, String lang) {

		Element input = null;
				
		switch (mode) {
		case HTML.SELECT:			
			input = objectSelectInput(name, title, value, type, notNull, lang);
			break;
			
		case HTML.TEXT:		
			input = objectTextInput(name, title, value, size);
			break;
			
		case HTML.RADIO:			
			input = objectRadioInput(name, title, value, type, notNull, lang);
			break;
			
		default:
			throw new InvalidValueException(Constants.INVALID_OBJECT_INPUT_MODE, mode);
		}		
		
		return input;
	}
	
	public Element objectsInput(String name, String title, Object value, String type, 
			boolean notNull, String mode, Integer size, String lang) {

		Element input = null;
		
		switch(mode) {
		case HTML.TEXTAREA:			
			input = objectsTextareaInput(name, title);
			break;
			
		case MULTIPLE_SELECT:
			input = objectsMultipleSelectInput(name, title, size, type, lang);
			break;
			
		case HTML.SELECT:			
			input = objectSelectInput(name, title, value, type, notNull, lang);
			break;
			
		case HTML.TEXT:		
			input = objectTextInput(name, title, value, size);
			break;
			
		case HTML.RADIO:			
			input = objectRadioInput(name, title, value, type, notNull, lang);
			break;
			
		default:
			throw new InvalidValueException(Constants.INVALID_OBJECTS_INPUT_MODE, mode);
		}
		
		return input;
	}
	
	public Element idInput(String type, String name, String title) {
		Integer size = typeSettings.getTypeInt32(type, Constants.ID_INPUT_SIZE);

		return objectTextInput(name, title, null, size);
	}
	
	public Element objectTextInput(String name, String title, Object value, Integer size) {
		Element input = input(HTML.TEXT, name, title, value)
				.setAttribute(HTML.MAXLENGTH, Type.MAX_ID_LENGTH);
		
		if (size != null) {
			input.setAttribute(HTML.SIZE, size);
		}
		
		return input;
	}
	
	public Element objectsTextareaInput(String name, String title) {
		
		return document.createElement(HTML.TEXTAREA)
				.setAttribute(HTML.NAME, name)
				.setAttribute(HTML.TITLE, title)
				.addClass(OBJECTS_TEXTAREA_INPUT);
	}
	
	public Element objectsMultipleSelectInput(String name, String title, Integer size, 
			String type, String lang) {
		
		Element input = document.createElement(HTML.SELECT).setAttribute(HTML.NAME, name)
				.setAttribute(HTML.TITLE, title);

		LinkedHashMap<String, String> names = nextNode.getObjectsName(type, lang);

		for (Entry<String, String> entry : names.entrySet()) {
			String objectId = entry.getKey();
			String objectName = entry.getValue();

			input.appendElement(HTML.OPTION).setAttribute(HTML.VALUE, objectId).appendText(objectName);
		}
		
		input.setAttribute(HTML.MULTIPLE).setAttribute(HTML.SIZE, size);
		
		return input;
		
	}
	
	public Element objectSelectInput(String name, String title, Object value, String type,
			boolean notNull, String lang) {
		
		Element input = document.createElement(HTML.SELECT).setAttribute(HTML.NAME, name)
				.setAttribute(HTML.TITLE, title);

		if (!notNull) {
			input.appendElement(HTML.OPTION);
		}

		LinkedHashMap<String, String> names = nextNode.getObjectsName(type, lang);

		for (Entry<String, String> entry : names.entrySet()) {
			String objectId = entry.getKey();
			String objectName = entry.getValue();

			Element option = input.appendElement(HTML.OPTION).setAttribute(HTML.VALUE, objectId);
			if (objectId.equals(value)) {
				option.setAttribute(HTML.SELECTED);
			}
			option.appendText(objectName);
		}
		
		return input;
	}
	
	public Element objectRadioInput(String name, String title, Object value, String type, 
			boolean notNull, String lang) {
		
		InputGroup inputGroup = document.createInputGroup();
		inputGroup.addClass(OBJECT_RADIO_INPUT);
		
		if (!notNull) {
			String nullName = strings.gts(type, Constants.NULL);
			
			inputGroup.appendInput(input(HTML.RADIO, name, nullName, ""));
			
			inputGroup.appendText(nullName);
			
		}
		
		LinkedHashMap<String, String> names = nextNode.getObjectsName(type, lang);
		
		for (Entry<String, String> entry : names.entrySet()) {
			String objectId = entry.getKey();
			String objectName = entry.getValue();

			Element input = inputGroup.appendInput(input(HTML.RADIO, name, objectName,
					objectId));
			
			if (objectId.equals(value)) {
				input.setAttribute(HTML.CHECKED);
			}
			
			inputGroup.appendText(objectName);
		}
		
		if (value == null) {
			Element[] inputs = inputGroup.getInputs();
			
			if (inputs != null && inputs.length > 0) {
				inputs[0].setAttribute(HTML.CHECKED);
			}
		}
		
		return inputGroup;
	}
	
	public Element timezoneFieldInput(String field, String title, Object value, TypeField typeField) {
		return timezoneInput("@" + field, title, value, typeField.isNotNull());
	}

	public Element timezoneInput(String name, String title, Object value, boolean notNull) {
		Element select = document.createElement(HTML.SELECT).setAttribute(HTML.NAME, name);
		
		if (!notNull) {
			select.appendElement(HTML.OPTION);
		}

		if (title == null) {
			title = name;
		}

		select.setAttribute(HTML.TITLE, title);

		ZoneId.getAvailableZoneIds().stream().sorted().forEach(timezoneId -> {
			ZoneId timezone = ZoneId.of(timezoneId);
			ZoneOffset offset = timezone.getRules().getOffset(Instant.now());

			Element option = select.appendElement(HTML.OPTION).setAttribute(HTML.VALUE, timezoneId)
					.appendText(offset + " - " + timezoneId);
			if (timezone.equals(value)) {
				option.setAttribute(HTML.SELECTED);
			}
		});

		return select;
	}

	public Element select(String name, String title, Tuple values) {
		Element select = document.createElement(HTML.SELECT).setAttribute(HTML.NAME, name);

		if (title == null) {
			title = name;
		}

		select.setAttribute(HTML.TITLE, title);

		for (Map.Entry<String, Object> entry : values.getFields().entrySet()) {
			select.appendElement(HTML.OPTION).setAttribute(HTML.VALUE, entry.getKey())
					.appendText(entry.getValue().toString());

		}
		return select;
	}

	public Element select(String name, String title, String[] values, String selectedValue) {
		Element select = document.createElement(HTML.SELECT).setAttribute(HTML.NAME, name);

		if (title == null) {
			title = name;
		}

		select.setAttribute(HTML.TITLE, title);

		for (String value : values) {
			Element option = select.appendElement(HTML.OPTION).setAttribute(HTML.VALUE, value)
					.appendText(value);

			if (value.equals(selectedValue)) {
				option.setAttribute(HTML.SELECTED);
			}
		}

		return select;
	}

	public Element time(Object time) {
		Element timeElement = document.createElement(HTML.TIME);
		if (time != null) {
			timeElement.appendText(time.toString());
		}
		return timeElement;
	}

	public Element colorOutput(Object color) {
		return document.createElement(HTML.SPAN)
				.setAttribute(HTML.STYLE, HTML.BACKGROUND_COLOR + ": " + color)
				.appendText(color.toString());
	}

	public Element selectTableHeaderCell(String type, String field, String lang, String view,
			FieldReference ref, Filter[] filters, String search, LinkedHashMap<String, Order> order,
			Long offset, Long limit, Component component) {

		Element cell = document.createElement(HTML.TH);
		String fieldName = Constants.ID.equals(field) ? strings.getIdName(type) : strings.getFieldName(type, field);
		String orderLinkString = "";
		LinkedHashMap<String, Order> fieldOrder = new LinkedHashMap<>();
		StringBuilder multiOrderParameter = new StringBuilder();
		int index = 1;

		if (order != null && !order.isEmpty()) {
			for (Map.Entry<String, Order> entry : order.entrySet()) {
				String key = entry.getKey();
				Order value = entry.getValue();

				if (key.equals(field)) {
					if (value.equals(Order.ASC)) {
						value = Order.DESC;
						orderLinkString = "▴";
						fieldOrder.put(key, value);
					} else {
						value = null;
						orderLinkString = "▾";
					}

					if (order.entrySet().size() > 1) {
						orderLinkString = "(" + index + orderLinkString + ")";
					} else {
						orderLinkString = " " + orderLinkString;
					}

					cell.addClass(ORDER_COLUMN);
				}

				if (value != null) {
					multiOrderParameter.append(key + ":" + value + ",");
				}

				index++;
			}

			if (multiOrderParameter.length() > 0) {
				multiOrderParameter.deleteCharAt(multiOrderParameter.length() - 1);
			}
		}

		if (order == null || !order.containsKey(field)) {
			fieldOrder.put(field, Order.ASC);
			if (multiOrderParameter.length() > 0) {
				multiOrderParameter.append(",");
			}
			multiOrderParameter.append(field + ":" + Order.ASC);
		}

		cell.appendElement(selectTableAnchor(fieldName + orderLinkString, type, lang, view, ref, filters,
				search, fieldOrder, offset, limit, component).addClass(SELECT_HEADER_ANCHOR)
					.setAttribute(DATA_MULTI_ORDER,	multiOrderParameter.toString()));

		return cell;
	}

	public Element selectTable(String type, NXObject[] objects, LinkedHashMap<String, TypeField> typeFields,
			String lang, String view, Long count, Long offset, Long limit, Long minLimit, Long maxLimit,
			Long limitIncrement, String search, LinkedHashMap<String, Order> order, Component component) {

		return selectTable(type, objects, typeFields, lang, view, null, null, search, order, count,
				offset, limit, minLimit, maxLimit, limitIncrement, component);
	}

	public Element selectTable(String type, NXObject[] objects, LinkedHashMap<String, TypeField> typeFields,
			String lang, String view, FieldReference ref, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long count, Long offset, Long limit, Long minLimit,
			Long maxLimit, Long limitIncrement, Component component) {

		boolean appendUpdateAnchor = permissions.isAllowed(type, Action.UPDATE_FORM);
		
		Element form = form(type, lang, view).setAttribute(HTML.AUTOCOMPLETE, HTML.OFF)
				.setAttribute(DATA_URI, request.getURIRoot()
								+ selectTableURI(type, lang, view, ref, filters, search, order,
										offset, limit))
				.setAttribute(DATA_STRINGS_OBJECTS_DELETE_CONFIRMATION,
						strings.gts(type, Constants.OBJECTS_DELETE_CONFIRMATION));

		form.appendElement(input(HTML.HIDDEN, Constants.ORDER, Constants.ORDER, orderString(order)));

		Element index = selectTableIndex(type, lang, view, ref, filters, search, order, count,
				offset, limit, minLimit, maxLimit, limitIncrement, component);
		Element indexHeader = index;
		Element indexFooter = index.clone();
		form.appendElement(indexHeader);

		Element table = form.appendElement(HTML.TABLE);

		Element header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		Element body = table.appendElement(HTML.TBODY);

		header.appendElement(HTML.TH).appendElement(allCheckbox(type));

		header.appendElement(selectTableHeaderCell(type, Constants.ID, lang, view, ref, filters, 
				search, order, offset, limit, component));

		for (Map.Entry<String, Object> entry : objects[0].getFields().entrySet()) {
			header.appendElement(selectTableHeaderCell(type, entry.getKey(), lang, view, ref, filters, 
					search, order, offset, limit, component));
		}

		if (appendUpdateAnchor) {
			header.appendElement(HTML.TH);
		}

		for (NXObject object : objects) {
			Element row = body.appendElement(HTML.TR);

			row.appendElement(HTML.TD).appendElement(
					input(HTML.CHECKBOX, Constants.OBJECTS, strings.getObjectsName(type), object.getId())
							.addClass(ITEM_CHECKBOX));

			String idString = null;
			if (object.getId().length() >= 25) {
				idString = object.getId().substring(0, 22) + "...";
			} else {
				idString = object.getId();
			}

			Element idCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(type, Action.GET)) {
				idCell.appendElement(HTML.A).appendText(idString)
					.setAttribute(HTML.HREF, uri(object.getType(), object.getId(), lang, view));
			} else {
				idCell.appendText(idString);
			}

			for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
				String field = entry.getKey();
				Object value = entry.getValue();
				TypeField typeField = typeFields.get(field);
				row.appendElement(HTML.TD)
						.appendElement(fieldOutput(object, field, value, typeField, lang, view, true));
			}

			if (appendUpdateAnchor) {
				String updateActionName = strings.getActionName(object.getType(), Action.UPDATE);
				
				row.appendElement(HTML.TD).appendElement(iconAnchor(updateActionName,
					uri(object.getType(), object.getId(), lang, view)
					+ formParameter(Action.UPDATE), Icon.PENCIL));
			}
		}

		form.appendElement(indexFooter);

		Element div = form.appendElement(HTML.DIV);
		div.addClass(SELECT_BUTTONS);

		String actionName = strings.getActionName(type, Action.DELETE);

		Element actionButton = button(actionName, Action.DELETE, Icon.MINUS, SUBMIT_FORM);
		if (!permissions.isAllowed(type, Action.DELETE)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		if (component != null) {
			actionButton.setAttribute(DATA_COMPONENT, component.toString());
		}
		
		div.appendElement(actionButton);

		div.appendElement(exportButton(type, !permissions.isAllowed(type, Action.EXPORT_OBJECTS)));

		return form;
	}
	
	public Element exportButton(boolean disabled) {
		return exportButton(null, disabled);
	}

	public Element exportButton(String type, boolean disabled) {
		String action = null;
		Element buttons = document.createElement(HTML.DIV);
		buttons.addClass(EXPORT_BUTTON);
		
		if (type == null) {
			action = Action.EXPORT_TYPES;
			
			String includeObjects = strings.gts(Constants.INCLUDE_OBJECTS);
			buttons.appendText(includeObjects);
			
			Element includeObjectsCheckbox = buttons.appendElement(
					booleanInput(Constants.INCLUDE_OBJECTS, includeObjects, false));
			if (disabled) {
				includeObjectsCheckbox.setAttribute(HTML.DISABLED);
			}
			
			buttons.appendElement(HTML.BR);
		} else {
			action = Action.EXPORT_OBJECTS;			
		}

		Element actionButton = buttons.appendElement(button(strings.gts(type, Constants.EXPORT), 
				action, Icon.SHARE_BOXED, Constants.EXPORT));
		if (disabled) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return buttons;
	}

	public Element selectTableIndex(String type, String lang, String view, String search,
			LinkedHashMap<String, Order> order, Long count, Long selectedOffset, Long limit,
			Long minLimit, Long maxLimit, Long limitIncrement, Component component) {
		
		return selectTableIndex(type, lang, view, null, null, search, order, count, selectedOffset, limit,
				minLimit, maxLimit, limitIncrement, component);
	}

	public Element selectTableIndex(String type, String lang, String view, FieldReference ref, 
			Filter[] filters, String search, LinkedHashMap<String, Order> order, Long count,
			Long selectedOffset, Long limit, Long minLimit, Long maxLimit, Long limitIncrement,
			Component component) {

		Element index = document.createElement(HTML.DIV).addClass(SELECT_INDEX);

		if (selectedOffset == null) {
			selectedOffset = 0L;
		}

		if (count > limit) {
			Long offsets = count / limit;
			Long longObjectsCount = typeSettings.getTypeInt64(type, Constants.LONG_OBJECTS_COUNT);

			if (offsets < longObjectsCount) {
				index.appendElements(shortSelectTableIndex(type, lang, view, ref, filters, search,
						order, count, selectedOffset, limit, component));
			} else {
				index.appendElements(longSelectTableIndex(type, lang, view, ref, filters, search,
						order, count, selectedOffset, limit, component));
			}
		}

		if (count > minLimit) {

			index.appendElement(
					selectTableLimitSelect(type, count, limit, minLimit, maxLimit, limitIncrement, component));
		}

		return index;
	}

	public Element[] shortSelectTableIndex(String type, String lang, String view, FieldReference ref,
			Filter[] filters, String search, LinkedHashMap<String, Order> order, Long count,
			Long selectedOffset, Long limit, Component component) {

		ArrayList<Element> index = new ArrayList<>();

		String offsetTextMode = typeSettings.gts(type, Constants.OFFSET_TEXT_MODE);

		for (Long offset = 0L; offset < count; offset += limit) {
			String text = selectTableIndexOffsetText(offsetTextMode, count, offset, limit);

			if (offset != selectedOffset) {
				index.add(selectTableAnchor(text, type, lang, view, ref, filters, search, order, offset,
						limit, component).addClass(Constants.OFFSET));
			} else {
				index.add(document.createElement(HTML.SPAN).addClass(SELECTED_OFFSET).appendText(text));
			}
		}

		return index.toArray(new Element[] {});
	}

	public Element[] longSelectTableIndex(String type, String lang, String view, FieldReference ref,
			Filter[] filters, String search, LinkedHashMap<String, Order> order, Long count,
			Long selectedOffset, Long limit, Component component) {

		ArrayList<Element> index = new ArrayList<>();

		String offsetTextMode = typeSettings.gts(type, Constants.OFFSET_TEXT_MODE);

		long offsets = count / limit;
		long lastOffset = count % limit == 0 ? (offsets - 1) * limit : offsets * limit;
		long rightOffset = 0;
		long leftOffset = 0;

		String text = selectTableIndexOffsetText(offsetTextMode, count, selectedOffset, limit);

		index.add(document.createElement(HTML.SPAN).addClass(SELECTED_OFFSET).appendText(text));

		for (int x = 1; x <= 3; x++) {
			rightOffset = selectedOffset + (limit * x);
			leftOffset = selectedOffset - (limit * x);

			if (rightOffset <= lastOffset) {
				text = selectTableIndexOffsetText(offsetTextMode, count, rightOffset, limit);

				index.add(selectTableAnchor(text, type, lang, view, ref, filters, search, order,
						rightOffset, limit, component).addClass(NEAR_SELECTED_OFFSET));
			}

			if (leftOffset >= 0) {
				text = selectTableIndexOffsetText(offsetTextMode, count, leftOffset, limit);

				index.add(0, selectTableAnchor(text, type, lang, view, ref, filters, search, order,
						leftOffset, limit, component).addClass(NEAR_SELECTED_OFFSET));
			}
		}

		for (int x = 1; rightOffset < lastOffset; x *= 2) {
			rightOffset = rightOffset + (limit * x);

			if (rightOffset > lastOffset) {
				rightOffset = lastOffset;
			}

			text = selectTableIndexOffsetText(offsetTextMode, count, rightOffset, limit);

			index.add(selectTableAnchor(text, type, lang, view, ref, filters, search, order,
					rightOffset, limit, component).addClass(Constants.OFFSET));
		}

		for (int x = 1; leftOffset > 0; x *= 2) {
			leftOffset = leftOffset - (limit * x);

			if (leftOffset < 0) {
				leftOffset = 0;
			}

			text = selectTableIndexOffsetText(offsetTextMode, count, leftOffset, limit);

			index.add(0, selectTableAnchor(text, type, lang, view, ref, filters, search, order,
					leftOffset, limit, component).addClass(Constants.OFFSET));
		}

		return index.toArray(new Element[] {});
	}

	public String selectTableIndexOffsetText(String mode, Long count, Long offset, Long limit) {
		String text = null;

		switch (mode) {
		case "offset":
			text = offset + "";
			break;

		case "offset+1":
			text = offset + 1 + "";
			break;

		case "number":
			text = (offset / limit) + "";
			break;

		case "number+1":
			text = (offset / limit) + 1 + "";
			break;
		}

		return text;
	}

	public Element selectTableLimitSelect(String type, Long count, Long limit, Long minLimit, Long maxLimit,
			Long limitIncrement, Component component) {
		Element select = document.createElement(HTML.SELECT).addClass(Constants.LIMIT);

		if (Component.REFERENCE.equals(component)) {
			select.setAttribute(DATA_COMPONENT, component.toString());
		}

		for (Long x = minLimit; x <= maxLimit; x += limitIncrement) {
			Element option = select.appendElement(HTML.OPTION).appendText(x);
			if (x == limit) {
				option.setAttribute(HTML.SELECTED);
			}
		}

		return select;
	}

	public Element selectTableAnchor(String text, String type, String lang, String view,
			FieldReference ref, Filter[] filters, String search, LinkedHashMap<String, Order> order,
			Long offset, Long limit, Component component) {

		Element anchor = document.createElement(HTML.A)
				.setAttribute(HTML.HREF, selectTableURI(type, lang, view, ref, filters, search,
						order, offset, limit))
				.appendText(text);

		if (Component.REFERENCE.equals(component)) {
			anchor.setAttribute(DATA_COMPONENT, component.toString());
		}

		return anchor;
	}

	public String selectTableURI(String type, String lang, String view, FieldReference ref, 
			Filter[] filters, String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		return uri(type, lang, view) + refParameter(ref) + filtersParameters(filters)
			+ searchParameter(search) + orderParameter(order) + parameter(Constants.OFFSET, offset)
			+ parameter(Constants.LIMIT, limit) + previewParameter(request.isPreview());
	}

	public String parameter(String name, Object value) {
		return value != null ? "&" + name + "=" + value.toString() : "";
	}

	public String refParameter(FieldReference ref) {
		return ref != null ? "&" + Constants.REF + "=" + refString(ref) : "";
	}
	
	public String refString(FieldReference ref) {
		return ref.getField() + ":" + ref.getId();
	}

	public String formParameter(String action) {
		return parameter(Constants.FORM, action);
	}
	
	public String previewParameter(boolean preview) {
		return preview ? previewParameter() : "";
	}
	
	public String previewParameter() {
		return "&" + Action.PREVIEW;
	}
	
	public String searchParameter(String search) {
		return parameter(Constants.SEARCH, search);
	}
	
	public String orderParameter(LinkedHashMap<String, Order> order) {
		return parameter(Constants.ORDER, orderString(order));
	}

	public String orderString(LinkedHashMap<String, Order> order) {
		String orderParameter = null;

		if (order != null && !order.isEmpty()) {
			StringBuilder orderParameterBuilder = new StringBuilder();
			for (Map.Entry<String, Order> entry : order.entrySet()) {
				orderParameterBuilder.append(entry.getKey() + ":" + entry.getValue() + ",");
			}
			orderParameterBuilder.deleteCharAt(orderParameterBuilder.length() - 1);
			orderParameter = orderParameterBuilder.toString();
		}

		return orderParameter;
	}
	
	public String filtersParameters(Filter[] filters) {
		StringBuilder parameters = new StringBuilder();
		
		if (filters != null) {
			for (int x = 0; x < filters.length; x++) {
				Filter filter = filters[x];
				String parameterRoot = "&" + Constants.FILTERS + ":" + x + ":";
			
				parameters.append(parameterRoot + Constants.FIELD + "=" + filter.getField());
			
				parameters.append(parameterRoot + Constants.COMPARISON + "=" + filter.getComparison());
			
				parameters.append(parameterRoot + Constants.VALUE + "=");
				
				Object value = filter.getValue();
				
				if (value != null) {
					parameters.append(value);
				}
			}
		}
		
		return parameters.toString();
	}
	
	public Element fieldOutput(String label, Object... elements) {
		Element output = document.createElement(HTML.DIV);
		output.addClass(FIELD_OUTPUT);
		output.appendElement(HTML.STRONG).appendText(label + ": ");

		for (Object element : elements) {
			if (element instanceof String) {
				output.appendText((String) element);
			} else if (element instanceof Element) {
				output.appendElement((Element) element);
			} else if (element instanceof HTMLFragment) {
				output.appendFragment((HTMLFragment) element);
			}
		}

		return output;
	}
	
	public Element listFieldOutput(String type, String label, Object[] objects, String objectsType,
			String lang, String view) {
		return listFieldOutput(type, label, objects, id -> uri(objectsType, id, lang, view));
	}
	
	public Element listFieldOutput(String type, String label, Object[] objects,
			Function<String, String> uriFunction) {
		
		boolean tuple = objects instanceof Tuple[];
		
		Element output = document.createElement(HTML.DIV);
		output.addClass(FIELD_OUTPUT);
		
		output.appendElement(HTML.STRONG).appendText(label + ": ");
		
		String id, name = null;
		
		if (objects != null && objects.length > 0) {
			
			int last = objects.length - 1;
			
			for (int x = 0; x < objects.length; x++) {
				if (tuple) {
					id = ((Tuple)objects[x]).getString(Constants.ID);
					name = ((Tuple)objects[x]).getString(Constants.NAME);
				} else {
					id = ((String[])objects[x])[0];
					name = ((String[])objects[x])[1];
				}
				
				output.appendElement(anchor(name, uriFunction.apply(id)));
			
				if (x < last) {
					output.appendText(" | ");
				}
			}
		}
				
		return output;
	}

	public Element fieldOutput(NXObject object, String field, Object value, TypeField typeField, String lang,
			String view) {
		Element output = document.createElement(HTML.DIV);
		output.addClass(FIELD_OUTPUT);
		String fieldName = strings.getFieldName(object.getType(), field);
		output.appendElement(HTML.STRONG).appendText(fieldName + ": ");
		output.appendElement(fieldOutput(object, field, value, typeField, lang, view, false));
		return output;
	}

	public Element fieldOutput(NXObject object, String field, Object value, TypeField typeField, String lang,
			String view, boolean preview) {
		Element fieldElement = null;

		if (value != null) {

			switch (typeField.getType()) {
			case PT.INT16:
			case PT.INT32:
			case PT.INT64:
			case PT.FLOAT32:
			case PT.FLOAT64:
			case PT.NUMERIC:
			case PT.STRING:
			case PT.TIMEZONE:
				fieldElement = textOutput(value);
				break;
			case PT.BOOLEAN:
				fieldElement = booleanOutput(value);
				break;
			case PT.DATE:
			case PT.TIME:
			case PT.DATETIME:
				fieldElement = time(value);
				break;
			case PT.COLOR:
				fieldElement = colorOutput(value);
				break;
			case PT.URI:
			case PT.EMAIL:
			case PT.TEL:
				fieldElement = fieldAnchor(value, typeField);
				break;
			case PT.HTML:
				fieldElement = htmlOutput(value);
				break;
			case PT.BINARY:
			case PT.FILE:
				fieldElement = binaryFieldOutput(object.getType(), object.getId(), field, value, lang);
				break;
			case PT.IMAGE:
				fieldElement = imageFieldOutput(object.getType(), object.getId(), field, value);
				break;
			case PT.DOCUMENT:
				fieldElement = documentFieldOutput(object.getType(), object.getId(), field, value, lang, preview);
				break;
			case PT.AUDIO:
				fieldElement = audioFieldOutput(object.getType(), object.getId(), field, value);
				break;
			case PT.VIDEO:
				fieldElement = videoFieldOutput(object.getType(), object.getId(), field, value);
				break;
			case PT.TEXT:
			case PT.JSON:
			case PT.XML:
				fieldElement = textareaOutput(value, preview);
				break;
			case PT.PASSWORD:
				fieldElement = passwordOutput();
				break;
			default:
				fieldElement = referenceAnchor(typeField.getType(), value, lang, view);
			}
		} else {
			fieldElement = document.createElement(HTML.SPAN);
		}

		return fieldElement;
	}

	public Element referenceAnchor(String fieldType, Object value, String lang, String view) {
		Element anchor = null;
		
		ObjectReference reference = (ObjectReference) value;
		
		if (permissions.isAllowed(fieldType, Action.GET)) {
			anchor = anchor(reference.getName(), uri(fieldType, reference.getId(), lang, view));
		} else {
			anchor = document.createElement(HTML.SPAN).appendText(reference.getName());
		}
		
		return anchor;
	}

	public Element htmlOutput(Object value) {
		Element html = document.createElement(HTML.DIV);

		if (value instanceof HTMLFragment) {
			html.appendFragment((HTMLFragment) value);
		} else {
			html.appendText(value + " ...");
		}
		return html;
	}

	public Element binaryFieldOutput(String type, String id, String field, Object value, String lang) {
		Element binary = document.createElement(HTML.SPAN);
		binary.appendElement(anchor(humanReadableBytes((Integer) value, lang), uri(type, id, field, null, null)));
		return binary;
	}

	public Element imageFieldOutput(String type, String id, String field, Object value) {
		Element fieldElement = null;

		if (value != null) {
			fieldElement = imageAnchor(id, "/" + type + "/" + id + "/" + field, type, id, field);
		} else {
			fieldElement = document.createElement(HTML.SPAN);
		}

		return fieldElement;
	}

	public Element mediaFieldOutput(String type, String id, String field, Object value, String fieldType) {
		Element fieldElement = null;

		if (value != null) {
			fieldElement = document.createElement(fieldType)
					.setAttribute(HTML.SRC, "/" + type + "/" + id + "/" + field)
					.setAttribute(HTML.CONTROLS);
		} else {
			fieldElement = document.createElement(HTML.SPAN);
		}

		return fieldElement;
	}

	public Element audioFieldOutput(String type, String id, String field, Object value) {
		return mediaFieldOutput(type, id, field, value, HTML.AUDIO);
	}

	public Element videoFieldOutput(String type, String id, String field, Object value) {
		return mediaFieldOutput(type, id, field, value, HTML.VIDEO);
	}

	public Element textOutput(Object value) {
		Element textElement = document.createElement(HTML.SPAN);
		textElement.appendText(value.toString());
		return textElement;
	}

	public Element passwordOutput() {
		Element password = document.createElement(HTML.SPAN);
		password.appendText(Security.HIDDEN_PASSWORD);
		return password;
	}

	public Element fieldAnchor(Object value, TypeField typeField) {
		String href = value.toString();

		switch (typeField.getType()) {
		case PT.EMAIL:
			href = HTML.MAILTO + ":" + href;
			break;
		case PT.TEL:
			href = HTML.TEL + ":" + href;
			break;
		}

		return anchor(value.toString(), href);
	}

	public Element anchor(String text, URIBuilder href) {
		return anchor(text, href.toString());
	}

	public Element anchor(String href) {
		return anchor(href, href);
	}

	public Element anchor(String text, String href) {
		return document.createElement(HTML.A).setAttribute(HTML.HREF, href).appendText(text);
	}

	public Element imageAnchor(String text, String href, String type, String id, String field) {
		Element anchor = document.createElement(HTML.A).setAttribute(HTML.HREF, href);
		anchor.appendElement(image(text, type, id, field));
		return anchor;
	}

	public Element imageAnchor(String text, String href, String src) {
		Element anchor = document.createElement(HTML.A).setAttribute(HTML.HREF, href);
		anchor.appendElement(image(text, src));
		return anchor;
	}

	public Element iconAnchor(String text, String href, String icon) {
		return imageAnchor(text, href, "/static/icons/" + icon + ".svg").addClass(ICON);
	}

	public Element logoAnchor(String type, String lang, String view) {
		return imageAnchor(strings.gts(type, Constants.LOGO_TEXT),
				href_uri(typeSettings.gts(type, Constants.LOGO_URI), lang, view),
				typeSettings.gts(type, Constants.LOGO));
	}

	public Element image(String text, String type, String id, String field) {
		return image(text, "/" + type + "/" + id + "/" + field + "/" + Constants.THUMBNAIL);
	}

	public Element image(String text, String src) {
		return document.createElement(HTML.IMG).setAttribute(HTML.SRC, src).setAttribute(HTML.ALT, text)
				.setAttribute(HTML.TITLE, text);
	}

	public Element smallButton(String text, String image, String buttonClass) {
		Element button = document.createElement(HTML.BUTTON).setAttribute(HTML.TYPE, HTML.BUTTON);

		button.appendElement(smallIcon(text, image));

		if (buttonClass != null) {
			button.addClass(buttonClass);
		}

		return button;
	}

	public Element smallIcon(String text, String image) {
		return icon(text, image).addClass(SMALL_ICON);
	}

	public Element normalIcon(String text, String image) {
		return icon(text, image).addClass(ICON);
	}

	public Element icon(String text, String image) {
		return image(text, "/static/icons/" + image + ".svg");
	}

	public Element button(String text, String buttonClass) {
		return button(text, null, null, buttonClass);
	}

	public Element button(String text, String value, String buttonClass) {
		return button(text, value, null, buttonClass);
	}
	
	public Element button(String text, String value, String image, String buttonClass) {
		Element button = document.createElement(HTML.BUTTON).setAttribute(HTML.TYPE, HTML.BUTTON);

		if (buttonClass != null) {
			button.addClass(buttonClass);
		}

		if (value != null) {
			button.setAttribute(HTML.VALUE, value);
		}

		if (image != null) {
			button.appendElement(normalIcon(text, image));
		}

		if (text != null) {
			button.appendText(text);
		}

		if (!request.isSecure()) {
			button.setAttribute(HTML.DISABLED);
		}

		return button;
	}

	public Element[] typeMenuElements(String type, String id, String lang, String view, FieldReference ref,
			String search, Component component) {
		String form = request.getForm();

		ArrayList<Element> elements = new ArrayList<>();

		String refParameter = refParameter(ref);
		String searchParameter = parameter(Constants.SEARCH, search);

		if (id != null && form != null && permissions.isAllowed(type, Action.GET)) {
			elements.add(iconAnchor(strings.gts(type, Constants.VIEW), uri(type, id, lang, view)
					+ refParameter, Icon.MAGNIFYING_GLASS));
		}

		if (!Action.INSERT.equals(form) && permissions.isAllowed(type, Action.INSERT_FORM)) {
			elements.add(iconAnchor(strings.getActionName(type, Action.INSERT),
					uri(type, lang, view) + formParameter(Action.INSERT) + refParameter, Icon.PLUS));
		}

		if (permissions.isAllowed(type, Action.SELECT) && (id != null || form != null
				|| component != null || request.isInfo() || request.isPreview() 
				|| request.isCalendar())) {

			String uri = uri(type, lang, view) + refParameter + searchParameter;

			elements.add(iconAnchor(strings.gts(type, Constants.LIST), uri, Icon.LIST));
		}

		if (typeSettings.getTypeBoolean(type, Constants.SHOW_PREVIEW) && !request.isPreview()
				&& permissions.isAllowed(type,  Action.PREVIEW)) {
			
			String uri = uri(type, lang, view) + previewParameter() + searchParameter;
							
			elements.add(iconAnchor(strings.getActionName(type, Action.PREVIEW), uri, Icon.LIST_RICH));
		}

		if (id == null && !Action.ALTER.equals(form) && permissions.isAllowed(type, Action.ALTER_FORM)) {
			elements.add(iconAnchor(strings.getActionName(type, Action.ALTER),
					uri(type, lang, view) + formParameter(Action.ALTER), Icon.PENCIL));
		}

		if (id != null && !Action.UPDATE.equals(form)
				&& permissions.isAllowed(type, Action.UPDATE_FORM)) {
			elements.add(iconAnchor(strings.getActionName(type, Action.UPDATE),
					uri(type, id, lang, view) + formParameter(Action.UPDATE), Icon.PENCIL));
		}

		if (!request.isInfo() && permissions.isAllowed(type, Action.GET_TYPE)) {
			elements.add(iconAnchor(strings.gts(type, Constants.TYPE), uri(type, lang, view) + "&" + Constants.INFO,
					Icon.INFO));
		}

		String rssSelect = typeSettings.gts(type, Constants.RSS_SELECT);

		if (rssSelect != null) {
			elements.add(rssIconAnchor(type, lang, ref));
		}

		String icalSelect = typeSettings.gts(type, Constants.ICAL_SELECT);

		if (icalSelect != null) {
			elements.add(icalIconAnchor(type, lang, ref));
		}

		if (!request.isCalendar() && permissions.isAllowed(type, Action.CALENDAR)) {
			String calendarSelect = typeSettings.gts(type, Constants.CALENDAR_SELECT);

			if (calendarSelect != null) {
				String uri = uri(type, lang, view) + "&" + Action.CALENDAR + refParameter;

				elements.add(iconAnchor(strings.getActionName(type, Action.CALENDAR), uri, Icon.CALENDAR));
			}
		}

		return elements.toArray(new Element[] {});
	}
	
	public Element rssIconAnchor(String type, String lang, FieldReference ref) {
		return iconAnchor(RSS, rssURI(type, lang, ref), Icon.RSS);
	}
	
	public String rssURI(String type, String lang, FieldReference ref) {
		return uri(type, lang, Constants.RSS) + refParameter(ref);
	}
	
	public Element icalIconAnchor(String type, String lang, FieldReference ref) {
		return iconAnchor(ICALENDAR, icalURI(type, lang, ref), Icon.FILE);
	}
	
	public String icalURI(String type, String lang, FieldReference ref) {
		return uri(type, lang, Constants.ICAL) + refParameter(ref);
	}

	public Element booleanOutput(Object value) {
		Element input = document.createElement(HTML.INPUT)
				.setAttribute(HTML.TYPE, HTML.CHECKBOX)
				.setAttribute(HTML.DISABLED);

		if (value != null && (boolean) value) {
			input.setAttribute(HTML.CHECKED);
		}

		return input;
	}

	public Element booleanInput(String name, String title, Boolean value) {
		Element input = input(HTML.CHECKBOX, name, title);

		if (value) {
			input.setAttribute(HTML.CHECKED);
		}

		return input;
	}

	public Element booleanFieldInput(String field, String title, Object value) {
		InputGroup group = document.createInputGroup();

		Element input = group.appendInput(input(HTML.CHECKBOX, "@" + field, title, Constants.TRUE));

		if (value != null && (boolean) value) {
			input.setAttribute(HTML.CHECKED);
		}

		group.appendInput(input(HTML.HIDDEN, "@" + field, title, Constants.FALSE));

		return group;
	}

	public Element dates(String type, ZonedDateTime cdate, ZonedDateTime udate) {
		Element dates = document.createElement(HTML.DIV);
		Element creation = dates.appendElement(HTML.P);
		creation.addClass(Constants.DATE);
		creation.appendElement(HTML.STRONG).appendText(strings.gts(type, Constants.CREATION_DATE) + ": ");
		creation.appendElement(time(cdate));

		Element updating = dates.appendElement(HTML.P);
		updating.addClass(Constants.DATE);
		updating.appendElement(HTML.STRONG).appendText(strings.gts(type, Constants.UPDATING_DATE) + ": ");
		updating.appendElement(time(udate));

		return dates;
	}

	public Content objectNotFound(String type, String id, String lang, String view) {
		return notFound(type, lang, view, new ObjectNotFoundException(type, id));
	}

	@Override
	public Content notFound(String type, String lang, String view, NotFoundException e) {
		Content content = null;

		String message = e.getMessage(strings);

		if (document != null) {
			document.getTitle().appendText(message);
			main.appendElement(HTML.P).addClass(Constants.MESSAGE).appendText(message);
			content = render(type);
			content.setStatus(HTTPStatus.NOT_FOUND);
		} else {
			content = new Content(message, Format.TEXT, HTTPStatus.NOT_FOUND);
		}

		return content;
	}

	@Override
	public Content unauthorized(String type, String lang, String view, UnauthorizedActionException e) {
		Content content = null;

		String message = e.getMessage(strings);

		if (request.getField() == null && request.getElement() == null) {

			if (document == null) {
				loadTemplate(type, lang, view);
			}

			document.getTitle().appendText(message);
			main.appendElement(HTML.P).addClass(Constants.MESSAGE).appendText(message);
			content = render(type);
			content.setStatus(HTTPStatus.UNAUTHORIZED);
		} else {
			content = new Content(message, Format.TEXT, HTTPStatus.UNAUTHORIZED);
		}

		return content;
	}

	public String uri(String lang, String view) {
		return uri(null, null, null, lang, view);
	}

	public String uri(String type, String lang, String view) {
		return uri(type, null, null, lang, view);
	}

	public String uri(String type, String id, String lang, String view) {
		return uri(type, id, null, lang, view);
	}

	public String uri(String type, String id, String field, String lang, String view) {
		String typeParameter = type != null ? type : "";
		String idParameter = id != null ? "/" + id : "";
		String fieldParameter = field != null ? "/" + field : "";

		return href_uri("/" + typeParameter + idParameter + fieldParameter, lang, view);
	}

	public String href_uri(String href, String lang, String view) {
		try {
			URIBuilder uri = new URIBuilder(href);

			if (lang != null) {
				uri.addParameter(Constants.LANG, lang);
			}

			if (view != null) {
				uri.addParameter(Constants.VIEW, view);
			}

			return uri.toString();
		} catch (URISyntaxException e) {
			throw new NXException(e);
		}
	}

	public Content render() {
		return render(null);
	}

	public Content render(String type) {
		images();

		Content content = new Content(document.toString(), Format.XHTML);
		content.setHeader(HTTPHeader.CONTENT_SECURITY_POLICY,
				typeSettings.gts(type, Constants.CONTENT_SECURITY_POLICY));

		return content;
	}

	public void close() {
		if (nextNode != null) {
			nextNode.close();
		}
	}

	protected HTMLView getHTMLView(String type, String view) {
		if (type == null) {
			throw new NXException(Constants.EMPTY_TYPE_NAME);
		}

		HTMLView htmlView = null;

		String className = typeSettings.getView(type, view);

		if (className != null) {
			htmlView = Loader.loadHTMLView(className, this);
		} else {
			htmlView = this;
		}

		return htmlView;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String[] getGroups() {
		return groups;
	}

	@Override
	public void setUser(String user) {
		this.user = user;
		nextNode.setUser(user);
	}

	@Override
	public void setGroups(String[] groups) {
		this.groups = groups;
		nextNode.setGroups(groups);
	}

	public void head(String type, String lang, String view) {
		if (head != null) {
			head.appendElement(HTML.META).setAttribute(HTML.CHARSET, Constants.UTF_8_CHARSET);

			head.appendElement(HTML.META).setAttribute(HTML.NAME, HTML.VIEWPORT)
				.setAttribute(HTML.CONTENT, "width=device-width, initial-scale=1");

			String description = strings.gts(type, Constants.DESCRIPTION);
			if (description != null) {
				head.appendElement(HTML.META).setAttribute(HTML.NAME, HTML.DESCRIPTION)
					.setAttribute(HTML.CONTENT, description);
			}

			head.appendElement(HTML.SCRIPT).setAttribute(HTML.SRC, "/static/javascript/nexttypes.js");

			head.appendElement(HTML.LINK).setAttribute(HTML.REL, HTML.STYLESHEET)
				.setAttribute(HTML.TYPE, "text/css")
				.setAttribute(HTML.HREF, typeSettings.gts(type, Constants.STYLE));

			head.appendElement(HTML.LINK).setAttribute(HTML.REL, HTML.SHORTCUT_ICON)
				.setAttribute(HTML.HREF, "/static/images/logo.ico");
		}
	}

	public void logo(String type, String lang, String view) {
		Element logo = document.getElementById(Constants.LOGO);
		if (logo != null) {
			logo.appendElement(logoAnchor(type, lang, view));
		}
	}

	public void menu(String type, String lang, String view) {
		Element menuElement = document.getElementById(Constants.MENU);

		if (menuElement != null) {
			
			String file = typeSettings.gts(type, Constants.MENU);
			
			if (file != null) {
				Menu menu = context.getMenu(typeSettings.gts(type, Constants.MENU));

				for (MenuSection section : menu.getSections()) {
					menuElement.appendElement(menuTitle(type, section.getTitle()));

					Element ul = menuElement.appendElement(HTML.UL);

					for (Anchor anchor : section.getAnchors()) {
						ul.appendElement(menuListItem(type, anchor.getText(), anchor.getHref(), lang, view));
					}
				}
			}
			
			if (typeSettings.getTypeBoolean(type, Constants.SHOW_CONTROL_PANEL)) {
				menuElement.appendElement(controlPanel(type, lang, view));
			}
		}
	}
	
	public Element controlPanel(String type, String lang, String view) {
		Element section = document.createElement(HTML.SPAN).addClass(CONTROL_PANEL);
	
		section.appendElement(menuTitle(type, Constants.CONTROL_PANEL));
		
		Element ul = section.appendElement(HTML.UL);
		
		if (permissions.isAllowed(type, Action.LOGIN_FORM)) {
			ul.appendElement(menuListItem(type, Action.LOGIN, "/?form=login", lang, view));
		}
		
		if (permissions.isAllowed(type, Action.GET_TYPES_INFO)) {
			ul.appendElement(menuListItem(type, Constants.TYPES, "/?info", lang, view));
		}
		
		if (permissions.isAllowed(type, Action.CREATE_FORM)) {
			ul.appendElement(menuListItem(type, Constants.CREATE_TYPE, "/?form=create", lang, view));
		}
		
		if (permissions.isAllowed(type, Action.IMPORT_TYPES_FORM)) {
			ul.appendElement(menuListItem(type, Action.IMPORT_TYPES, "/?form=import_types", lang, view));
		}
		
		if (permissions.isAllowed(type, Action.IMPORT_OBJECTS_FORM)) {
			ul.appendElement(menuListItem(type, Action.IMPORT_OBJECTS, "/?form=import_objects", lang, view));
		}
		
		if (permissions.isAllowed(type, Action.GET_REFERENCES)) {
			ul.appendElement(menuListItem(type, Constants.REFERENCES, "/?references", lang, view));
		}
		
		return section;
	}
	
	public Element menuTitle(String type, String title) {
		return document.createElement(HTML.DIV).addClass(MENU_TITLE)
				.appendText(strings.gts(type, title) + ":");
	}
	
	public Element menuListItem(String type, String text, String href, String lang, String view) {
		Element li = document.createElement(HTML.LI);
		li.appendElement(anchor(strings.gts(type, text), href_uri(href, lang, view)));
		return li;
	}

	public void langs(String lang) {
		Element langsElement = document.getElementById(Constants.LANGS);

		if (langsElement != null) {
			Tuple langs = settings.getTuple(Constants.LANGS);

			if (langs != null) {
				Element select = langsElement.appendElement(HTML.SELECT).addClass(Constants.LANGS);

				for (Map.Entry<String, Object> entry : langs.getFields().entrySet()) {
					String id = entry.getKey();
					String name = (String) entry.getValue();

					Element option = select.appendElement(HTML.OPTION).setAttribute(HTML.VALUE, id)
							.appendText(name);

					if (id.equals(lang)) {
						option.setAttribute(HTML.SELECTED);
					}
				}
			}
		}
	}

	public void user(String type, String lang, String view) {

		Element userElement = document.getElementById(Constants.USER);

		if (userElement != null) {
			String user = request.getUser();

			if (Auth.GUEST.equals(user)) {
				userElement.addClass(HTML.HIDDEN);
			}

			Element form = userElement.appendElement(form(lang, view));

			form.appendText(strings.gts(type, Constants.USER) + ": ");

			Element userNameSpan = form.appendElement(HTML.SPAN).setId(USER_NAME);
			userNameSpan.appendText(user);

			Element button = button(strings.gts(type, Constants.LOGOUT), Action.LOGOUT, Icon.ACCOUNT_LOGOUT,
					SUBMIT_FORM).setId(LOGOUT_BUTTON);
			
			form.appendElement(button);

			if (!request.isLoginUser()) {
				button.addClass(HTML.HIDDEN);
			}
		}
	}

	public void search(String type, String lang, String view, FieldReference ref, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		if (type != null) {
			Element form = document.getElementById(Constants.SEARCH);

			if (form != null) {
				form.setAttribute(HTML.ACTION, "/" + type).setAttribute(HTML.METHOD, HTML.GET);

				String searchName = strings.gts(type, Constants.SEARCH);
				
				form.appendElement(input(HTML.HIDDEN, Constants.LANG, Constants.LANG, lang));
				form.appendElement(input(HTML.HIDDEN, Constants.VIEW, Constants.VIEW, view));
				form.appendElement(input(HTML.SEARCH, Constants.SEARCH, searchName, search));
				
				if (request.isPreview()) {
					form.appendElement(input(HTML.HIDDEN, Action.PREVIEW, Action.PREVIEW, Action.PREVIEW));
				}
				
				if (ref != null) {
					form.appendElement(input(HTML.HIDDEN, Constants.REF, Constants.REF, refString(ref)));
				}
				
				if (order != null) {
					form.appendElement(input(HTML.HIDDEN, Constants.ORDER, Constants.ORDER,
							orderString(order)));
				}
				
				if (offset != null) {
					form.appendElement(input(HTML.HIDDEN, Constants.OFFSET, Constants.OFFSET, offset));
				}
				
				if (limit != null) {
					form.appendElement(input(HTML.HIDDEN, Constants.LIMIT, Constants.LIMIT, limit));
				}

				form.appendElement(document.createElement(HTML.BUTTON)
						.setAttribute(HTML.TYPE, HTML.SUBMIT)
						.appendText(searchName));
			}
		}
	}

	public void typeMenu(String type, String id, String lang, String view, FieldReference ref,
			String search, Component component) {
		if (type != null) {
			Element typeMenu = document.getElementById(TYPE_MENU);

			if (typeMenu != null) {
				typeMenu.appendElements(typeMenuElements(type, id, lang, view, ref, search, component));
			}
		}
	}

	public void actions(String type, String id, String lang, String view) {
		Element actionsElement = document.getElementById(Constants.ACTIONS);

		if (actionsElement != null) {
			String form = request.getForm();

			if (permissions.isAllowed(type, Action.EXECUTE_ACTION_FORM) && type != null
					&& !Action.INSERT.equals(form) && !Action.ALTER.equals(form)
					&& !Action.RENAME.equals(form) && !request.isInfo()) {

				LinkedHashMap<String, LinkedHashMap<String, TypeField>> actions = nextNode.getTypeActions(type);

				if (actions != null && actions.size() > 0) {
					String requestAction = request.getAction();

					if (requestAction != null) {
						actions.remove(requestAction);
					}
					
					for (String action : actions.keySet()) {
						if (!permissions.isAllowed(type, action)) {
							actions.remove(action);
						}
					}

					if (actions.size() > 0) {

						for (Map.Entry<String, LinkedHashMap<String, TypeField>> entry : actions.entrySet()) {
							String action = entry.getKey();

							String actionName = strings.getActionName(type, action);

							String actionParameters = formParameter(Action.EXECUTE_ACTION) + "&"
									+ Constants.TYPE_ACTION + "=" + action;

							actionsElement.appendElement(anchor(actionName, uri(type, id, lang, view)
									+ actionParameters));
						}

					} else {
						actionsElement.remove();
					}
				} else {
					actionsElement.remove();
				}

			} else {
				actionsElement.remove();
			}
		}
	}

	public void textEditors() {
		if (head != null) {
			head.appendElement(HTML.SCRIPT)
				.setAttribute(HTML.SRC, "/static/lib/codemirror/lib/codemirror.js");

			head.appendElement(HTML.SCRIPT)
				.setAttribute(HTML.SRC, "/static/lib/tinymce/js/tinymce/tinymce.min.js");

			head.appendElement(HTML.SCRIPT).setAttribute(HTML.SRC, "/static/javascript/texteditors.js");

			head.appendElement(HTML.LINK).setAttribute(HTML.REL, HTML.STYLESHEET)
				.setAttribute(HTML.TYPE, "text/css")
				.setAttribute(HTML.HREF, "/static/lib/codemirror/lib/codemirror.css");
		}
	}

	public Element textEditorMode(String mode) {
		if (JSON.equals(mode)) {
			mode = JAVASCRIPT;
		}

		return document.createElement(HTML.SCRIPT)
				.setAttribute(HTML.SRC, "/static/lib/codemirror/mode/" + mode + "/" + mode + ".js");
	}

	public void images() {
		Element[] images = document.getElementsByClassName(IMAGE);

		if (images.length > 0) {
			Map<String, List<Element>> imagesById = java.util.stream.Stream.of(images).collect(
					Collectors.groupingBy(div -> div.getAttribute(DATA_ID) + ":" + div.getAttribute(DATA_LANG)));

			String sql =
					"select"
							+ " il.id,"
							+ " ill.language,"
							+ " ill.description,"
							+ " ill.alt,"
							+ " coalesce(ill.link, il.link) as link,"
							+ " coalesce(ill.title, il.title) as title,"
							+ " case"
								+ " when ill.image is null then 'image_link'"
								+ " else 'image_link_language'"
							+ " end as image_type,"
							+ " case"
								+ " when ill.image is null then il.id"
								+ " else ill.id"
							+ " end as image_id"

					+ " from"
						+ " image_link il join image_link_language ill on il.id = ill.image_link"

					+ " where"
						+ " il.id || ':' || ill.language in(?)";

			Tuple[] tuples = nextNode.query(sql, new Object[] { imagesById.keySet().toArray() });

			for (Tuple tuple : tuples) {
				for (Element div : imagesById.get(tuple.getString(Constants.ID) + ":" + tuple.getString(Constants.LANGUAGE))) {
					Element container = div;

					Element image = document.createElement(HTML.IMG)
							.setAttribute(HTML.SRC, "/" + tuple.getString(Constants.IMAGE_TYPE)
								+ "/" + tuple.getString(Constants.IMAGE_ID) + "/" + IMAGE)
							.setAttribute(HTML.ALT, tuple.getString(HTML.ALT));

					String link = tuple.getString(Constants.LINK);
					if (link != null) {
						Element anchor = document.createElement(HTML.A).setAttribute(HTML.HREF, link);
						String title = tuple.getString(HTML.TITLE);
						if (title != null) {
							anchor.setAttribute(HTML.TITLE, title);
						}
						anchor.appendElement(image);
						image = anchor;
					}

					String description = tuple.getString(Constants.DESCRIPTION);
					if (description != null) {
						Element figure = div.appendElement(HTML.FIGURE);
						figure.appendElement(image);
						Element figcaption = figure.appendElement(HTML.FIGCAPTION);
						figcaption.appendText(description);
						container = figure;
					}

					container.prependElement(image);
				}
			}
		}
	}

	public void qrcode(String type, String id) {
		if (id != null) {
			Element qrcodeElement = document.getElementById(Constants.QRCODE);

			if (qrcodeElement != null) {
				String objectURI = request.getHost() + "/" + type + "/" + id;

				QRCode objectURIQrcode = new QRCode(objectURI, 80, ErrorCorrectionLevel.L);
				qrcodeElement.appendElement(image(objectURI, objectURIQrcode.getBase64()));
			}
		}
	}

	public void rss(String type, String lang) {
		String rssSelect = typeSettings.gts(type, Constants.RSS_SELECT);

		if (rssSelect != null && head != null) {
			head.appendElement(HTML.LINK).setAttribute(HTML.REL, HTML.ALTERNATE)
					.setAttribute(HTML.TYPE, Format.RSS.getContentType())
					.setAttribute(HTML.TITLE, RSS)
					.setAttribute(HTML.HREF, uri(type, lang, Constants.RSS));
		}
	}

	public void validators(String type) {
		Boolean showValidators = typeSettings.getTypeBoolean(type, Constants.SHOW_VALIDATORS);

		if (showValidators) {

			Element validators = document.getElementById(VALIDATORS);

			if (validators != null) {
				validators.appendElement(
						imageAnchor(HTML5, "http://validator.w3.org/check?uri=referer", "/static/images/html5.png"));
				validators.appendElement(
						imageAnchor(CSS, "http://jigsaw.w3.org/css-validator/check/referer", "/static/images/css.gif"));
				validators.appendElement(imageAnchor(WCAG,
						"http://achecker.ca/checker/index.php?uri=referer&gid=WCAG2-AAA", "/static/images/wcag.jpeg"));
			}
		}
	}

	public void footer(String type) {

		if (footer != null) {
			footer.appendFragment(strings.gts(type, Constants.FOOTER));
		}
	}

	@Override
	public Content calendar(String type, String lang, String view, FieldReference ref, Year year, Month month) {
		loadTemplate(type, lang, view);

		String title = strings.gts(type, Constants.CALENDAR_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));

		ZoneId timezone = nextNode.getTimezone("select timezone from \"user\" where id=?", request.getUser());

		LocalDate today = timezone != null ? LocalDate.now(timezone) : LocalDate.now();

		if (year == null) {
			year = Year.of(today.getYear());
		}

		if (month == null) {
			month = today.getMonth();
		}

		LocalDate date = LocalDate.of(year.getValue(), month, 1);

		LocalDate firstDate = calendarFirstDate(date);
		LocalDate lastDate = calendarLastDate(date);

		StringBuilder sql = new StringBuilder(typeSettings.gts(type, Constants.CALENDAR_SELECT));
		StringBuilder filters = new StringBuilder("date >= ? and date <= ?");
		ArrayList<Object> parameters = new ArrayList<>();

		parameters.add(firstDate);
		parameters.add(lastDate);

		if (ref != null) {
			main.appendElement(referenceOutput(type, lang, view, ref, null, null, null));

			filters.append(" and # = ?");
			parameters.add(ref.getField());
			parameters.add(ref.getId());
		}

		Tuple[] events = nextNode.select(type, sql, parameters, filters.toString(), "date, start_time");

		main.appendElement(calendar(type, lang, view, ref, month, today, date, events, firstDate));

		return render(type);
	}

	public Element calendar(String type, String lang, String view, FieldReference ref, Month month, LocalDate today,
			LocalDate date, Tuple[] events, LocalDate firstDate) {

		Element calendar = document.createElement(HTML.DIV).addClass(CALENDAR);

		Map<LocalDate, List<Tuple>> eventsByDate = Arrays.stream(events)
				.collect(Collectors.groupingBy(event -> event.getDate(Constants.DATE)));

		calendar.appendElement(dateSelect(type, lang, view, ref, date));
		calendar.appendElement(month(type, lang, view, month, today, firstDate, eventsByDate));

		return calendar;
	}

	public Element month(String type, String lang, String view, Month month, LocalDate today, LocalDate date,
			Map<LocalDate, List<Tuple>> eventsByDate) {

		Element monthElement = document.createElement(HTML.TABLE).addClass(CALENDAR_MONTH);

		monthElement.appendElement(monthHead(lang));
		Element tbody = monthElement.appendElement(HTML.TBODY);

		while (true) {
			Element tr = tbody.appendElement(HTML.TR);

			for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
				tr.appendElement(day(type, lang, view, month, today, date, eventsByDate.get(date)));
				date = date.plusDays(1);
			}

			if (!date.getMonth().equals(month)) {
				break;
			}
		}

		return monthElement;
	}

	public Element dateSelect(String type, String lang, String view, FieldReference ref, LocalDate date) {
		Element navigator = document.createElement(HTML.DIV);

		Locale locale = new Locale(lang);

		int currentYear = date.getYear();
		Month currentMonth = date.getMonth();

		LocalDate before = date.minusMonths(1);
		LocalDate after = date.plusMonths(1);

		navigator.appendElement(anchor("<<", uri(type, lang, view) + "&" + Action.CALENDAR + "&" + Constants.YEAR + "="
				+ before.getYear() + "&" + Constants.MONTH + "=" + before.getMonthValue()
					+ refParameter(ref)));

		Element yearSelect = navigator.appendElement(HTML.SELECT).addClass(YEARS);
		for (int year = date.minusYears(10).getYear(); year <= date.plusYears(10).getYear(); year++) {
			Element option = yearSelect.appendElement(HTML.OPTION)
					.setAttribute(HTML.VALUE, year).appendText(year);

			if (year == currentYear) {
				option.setAttribute(HTML.SELECTED);
			}

		}

		Element monthSelect = navigator.appendElement(HTML.SELECT).addClass(MONTHS);
		for (Month month : Month.values()) {
			Element option = monthSelect.appendElement(HTML.OPTION)
					.setAttribute(HTML.VALUE, month.getValue())
					.appendText(month.getDisplayName(TextStyle.FULL, locale));

			if (month.equals(currentMonth)) {
				option.setAttribute(HTML.SELECTED);
			}

		}

		navigator.appendElement(anchor(">>", uri(type, lang, view) + "&" + Action.CALENDAR + "&"
				+ Constants.YEAR + "=" + after.getYear() + "&" + Constants.MONTH + "=" 
				+ after.getMonthValue() + refParameter(ref)));

		return navigator;
	}

	public Element monthHead(String lang) {
		Element head = document.createElement(HTML.THEAD);

		Locale locale = new Locale(lang);

		Element tr = head.appendElement(HTML.TR);

		for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
			tr.appendElement(HTML.TH).appendText(dayOfWeek.getDisplayName(TextStyle.FULL, locale));
		}

		return head;
	}

	public Element day(String type, String lang, String view, Month month, LocalDate today, LocalDate date,
			List<Tuple> events) {

		Element day = document.createElement(HTML.TD);

		day.appendText(String.valueOf(date.getDayOfMonth()));

		if (date.isEqual(today)) {
			day.addClass(CALENDAR_TODAY);
		} else if (date.getMonth().equals(month)) {
			day.addClass(CALENDAR_DAY);
		}

		if (events != null) {
			for (Tuple event : events) {
				day.appendElement(event(type, lang, view, event));
			}
		}

		return day;
	}

	public Element event(String type, String lang, String view, Tuple event) {
		Element div = document.createElement(HTML.DIV)
				.setAttribute(HTML.STYLE, HTML.BACKGROUND_COLOR + ": " + event.getColor(Constants.COLOR));

		div.appendElement(anchor(event.getTime(Constants.START_TIME) + " " + event.getString(Constants.SUMMARY),
				uri(type, event.getString(Constants.ID), lang, view)));

		return div;
	}

	public LocalDate calendarFirstDate(LocalDate date) {
		date = date.withDayOfMonth(1);
		date = date.minusDays(date.getDayOfWeek().getValue() - 1);
		return date;
	}

	public LocalDate calendarLastDate(LocalDate date) {
		int day = date.getMonth().equals(Month.FEBRUARY) && !date.isLeapYear() ? 28 : date.getMonth().maxLength();
		date = date.withDayOfMonth(day);
		date = date.plusDays(7 - date.getDayOfWeek().getValue());
		return date;
	}

	@Override
	public Context getContext() {
		return context;
	}
}