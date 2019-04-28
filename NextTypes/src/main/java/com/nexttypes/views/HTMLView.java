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

package com.nexttypes.views;

import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.client.utils.URIBuilder;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.nexttypes.datatypes.Anchor;
import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.DocumentPreview;
import com.nexttypes.datatypes.File;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.FieldRange;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.HTML;
import com.nexttypes.datatypes.HTML.InputGroup;
import com.nexttypes.datatypes.HTMLFragment;
import com.nexttypes.datatypes.Menu;
import com.nexttypes.datatypes.MenuSection;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.Names;
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
import com.nexttypes.datatypes.URL;
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
import com.nexttypes.exceptions.UnauthorizedException;
import com.nexttypes.nodes.Node;
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
import com.nexttypes.system.KeyWords;
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
	public static final String DATA_STRINGS_PREVIOUS = "data-strings-previous";
	public static final String DATA_STRINGS_NEXT = "data-strings-next";
	
	//Data Attributes
	public static final String DATA_EDITOR = "data-editor";
	public static final String DATA_SHOW_PROGRESS = "data-show-progress";
	public static final String DATA_MULTI_ORDER = "data-multi-order";
	public static final String DATA_URL = "data-url";
	public static final String DATA_ID = "data-id";
	public static final String DATA_LANG = "data-lang";
	public static final String DATA_COMPONENT = "data-component";
	public static final String DATA_SIZE = "data-size";
	public static final String DATA_LIMIT = "data-limit";
	public static final String DATA_OFFSET = "data-offset";
	public static final String DATA_NOT_NULL = "data-not-null";
	
	//Elements
	public static final String USER_NAME = "user-name";
	public static final String LOGOUT_BUTTON = "logout-button";
	public static final String TYPE_MENU = "type-menu";
	public static final String CONTROL_PANEL = "control-panel";
	public static final String VALIDATORS = "validators";

	//Classes
	public static final String ADD_FILTER = "add-filter";
	public static final String FILTER_FIELD = "filter-field";
	public static final String FILTER_COMPARISON = "filter-comparison";
	public static final String FILTER_INPUT = "filter-input";
	public static final String FILTER_TEXT_INPUT = "filter-text-input";
	public static final String FIELD_RANGE = "field-range";
	public static final String OBJECT_RADIO_INPUT = "object-radio-input";
	public static final String OBJECT_LIST_INPUT = "object-list-input";
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
	
	public HTMLView(String type, HTMLView parent) {
		document = parent.getDocument();
		nextNode = parent.getNextNode();
		request = parent.getRequest();
		context = parent.getContext();
		settings = parent.getSettings();
		typeSettings = parent.getTypeSettings();
		strings = parent.getStrings();
		auth = parent.getAuth();
		permissions = context.getPermissions(type, this);
	}

	public HTMLView(HTTPRequest request) {
		super(request, Settings.HTML_SETTINGS);
		permissions = getPermissions(request.getType());
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
		setTitle(strings.gts(KeyWords.TYPES));
		
		TreeMap<String, TypeInfo> types = nextNode.getTypesInfoOrderByName();

		if (types.size() > 0) {
			main.appendElement(typesTable(types, lang, view));
		} else {
			main.appendElement(HTML.P).appendText(strings.gts(KeyWords.NO_TYPES_FOUND));
		}

		return render();
	}

	@Override
	public Content insertForm(String type, String lang, String view, FieldReference ref) {
		loadTemplate(type, lang, view);
		String title = strings.gts(type, KeyWords.INSERT_TITLE);
		String typeName = strings.getTypeName(type);

		String[] fields = typeSettings.getActionStringArray(type, Action.INSERT, KeyWords.FIELDS);
		
		setTitle(Utils.format(title, typeName));

		textEditors();
		main.appendElement(insertForm(type, fields, lang, view, ref));

		return render(type);
	}

	@Override
	public Content createForm(String lang, String view) {
		loadTemplate(null, lang, view);
		setTitle(strings.gts(KeyWords.CREATE_TYPE));

		Element form = typeForm(null, lang, view);
		main.appendElement(form);

		return render();
	}

	@Override
	public Content alterForm(String type, String lang, String view) {
		loadTemplate(type, lang, view);

		String title = strings.gts(type, KeyWords.ALTER_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));

		Element form = typeForm(type, lang, view);
		main.appendElement(form);

		return render(type);
	}

	public Element typeForm(String type, String lang, String view) {
		String fields = strings.gts(type, KeyWords.FIELDS);
		String typeString = strings.gts(type, KeyWords.TYPE);
		String name = strings.gts(type, KeyWords.NAME);
		String parameters = strings.gts(type, KeyWords.PARAMETERS);
		String notNull = strings.gts(type, KeyWords.NOT_NULL);
		String mode = strings.gts(type, KeyWords.MODE);
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

		String typeName = strings.gts(KeyWords.TYPE_NAME);
		typeForm.appendElement(HTML.STRONG).appendText(typeName + ": ");

		if (type != null) {
			action = Action.ALTER;
			icon = Icon.PENCIL;
			
			ZonedDateTime adate = nextNode.getADate(type);
			typeForm.appendElement(input(HTML.HIDDEN, KeyWords.ADATE, KeyWords.ADATE, adate));
			
			typeForm.appendText(type);
			
			if (permissions.isAllowed(type, Action.RENAME_FORM)) {
				typeForm.appendElement(iconAnchor(strings.getActionName(type, Action.RENAME),
					url(type, lang, view) + formParameter(Action.RENAME), Icon.PENCIL));
			}
				
		} else {
			action = Action.CREATE;
			icon = Icon.PLUS;
			
			typeForm.appendElement(input(HTML.TEXT, KeyWords.TYPE, typeName)
					.setAttribute(HTML.SIZE, Type.MAX_TYPE_NAME_LENGTH)
					.setAttribute(HTML.MAXLENGTH, Type.MAX_TYPE_NAME_LENGTH));
		}
		
		boolean disableAction = !permissions.isAllowed(type, action);
		
		String addFieldActionName = strings.getActionName(type, Action.ADD_FIELD);

		typeForm.appendElement(HTML.H2).appendText(fields + ":");
		Element addFieldButton = typeForm.appendElement(HTML.P)
				.appendElement(button(addFieldActionName, null, Icon.PLUS, ADD_FIELD));
		
		if (!permissions.isAllowed(type, Action.ADD_FIELD)) {
			addFieldButton.setAttribute(HTML.DISABLED);
		}
		
		Element fieldsTable = typeForm.appendElement(HTML.TABLE).setAttribute(HTML.ID, KeyWords.FIELDS);

		Element fieldsHeader = fieldsTable.appendElement(HTML.THEAD).appendElement(HTML.TR);
		fieldsHeader.appendElement(HTML.TH).appendText(typeString);
		fieldsHeader.appendElement(HTML.TH).appendText(name);
		fieldsHeader.appendElement(HTML.TH).appendText(parameters);
		fieldsHeader.appendElement(HTML.TH).appendText(notNull);
		fieldsHeader.appendElement(HTML.TH);
		
		Element fieldsBody = fieldsTable.appendElement(HTML.TBODY);
		
		if (type != null) {
			
			boolean dropFieldAllowed = permissions.isAllowed(type, Action.DROP_FIELD);
			
			String[] types = (String[]) ArrayUtils.addAll(PT.PRIMITIVE_TYPES, nextNode.getTypesName());
						
			LinkedHashMap<String, TypeField> typeFields = nextNode.getTypeFields(type);
			int x = 0;
			for (Map.Entry<String, TypeField> entry : typeFields.entrySet()) {
				String field = KeyWords.FIELDS + ":" + x;
				String fieldName = entry.getKey();
				TypeField typeField = entry.getValue();
				String fieldType = typeField.getType();
				
				Element row = fieldsBody.appendElement(HTML.TR);

				row.appendElement(HTML.TD).appendElement(select(field + ":"
						+ KeyWords.TYPE, strings.gts(type, KeyWords.TYPE), types, fieldType));
												
				Element nameCell = row.appendElement(HTML.TD);
				nameCell.appendElement(input(HTML.TEXT, field + ":" + KeyWords.NAME, name, fieldName));
				nameCell.appendElement(input(HTML.HIDDEN, field + ":" + KeyWords.OLD_NAME, name,
						fieldName));
								
				row.appendElement(HTML.TD).appendElement(input(HTML.TEXT, field + ":"
						+ KeyWords.PARAMETERS, parameters, typeField.getParameters()));
								
				row.appendElement(HTML.TD).appendElement(booleanInput(field + ":" 
						+ KeyWords.NOT_NULL, notNull, typeField.isNotNull()));
								
				Element dropFieldButton = row.appendElement(HTML.TD)
						.appendElement(smallButton(dropField, Icon.MINUS, DELETE_ROW));
				
				if (!dropFieldAllowed) {
					dropFieldButton.setAttribute(HTML.DISABLED);
				}
								
				x++;
			}
		}

		String addIndexActionName = strings.getActionName(type, Action.ADD_INDEX);

		typeForm.appendElement(HTML.H2).appendText(strings.gts(type, KeyWords.INDEXES) + ":");
		Element addIndexButton = typeForm.appendElement(HTML.P)
				.appendElement(button(addIndexActionName, null, Icon.PLUS, ADD_INDEX));
		
		if (!permissions.isAllowed(type, Action.ADD_INDEX)) {
			addIndexButton.setAttribute(HTML.DISABLED);
		}

		Element indexesTable = typeForm.appendElement(HTML.TABLE)
				.setAttribute(HTML.ID, KeyWords.INDEXES);

		Element indexesHeader = indexesTable.appendElement(HTML.THEAD).appendElement(HTML.TR);
		indexesHeader.appendElement(HTML.TH).appendText(mode);
		indexesHeader.appendElement(HTML.TH).appendText(name);
		indexesHeader.appendElement(HTML.TH).appendText(fields);
		indexesHeader.appendElement(HTML.TH);
		
		Element indexesBody = indexesTable.appendElement(HTML.TBODY);

		if (type != null) {
			
			boolean dropIndexAllowed = permissions.isAllowed(type, Action.DROP_INDEX);
			
			LinkedHashMap<String, TypeIndex> typeIndexes = nextNode.getTypeIndexes(type);
			int x = 0;
			for (Map.Entry<String, TypeIndex> entry : typeIndexes.entrySet()) {
				String index = KeyWords.INDEXES + ":" + x;
				String indexName = entry.getKey();
				TypeIndex typeIndex = entry.getValue();
				Element row = indexesBody.appendElement(HTML.TR);
				
				row.appendElement(HTML.TD).appendElement(select(index + ":" + KeyWords.MODE, mode,
						IndexMode.getStringValues(), typeIndex.getMode().toString()));
				
				Element nameCell = row.appendElement(HTML.TD);
				nameCell.appendElement(input(HTML.TEXT, index + ":" + KeyWords.NAME, name, indexName));
				nameCell.appendElement(input(HTML.HIDDEN, index + ":" + KeyWords.OLD_NAME, name, indexName));
								
				row.appendElement(HTML.TD).appendElement(input(HTML.TEXT, index + ":" + KeyWords.FIELDS, fields,
						String.join(",", typeIndex.getFields())));
				
				Element dropIndexButton = row.appendElement(HTML.TD)
						.appendElement(smallButton(dropIndex, Icon.MINUS, DELETE_ROW));
				
				if (!dropIndexAllowed) {
					dropIndexButton.setAttribute(HTML.DISABLED);
				}
				
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
		String title = strings.gts(type, KeyWords.RENAME_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));
		main.appendElement(renameFormElement(type, lang, view));
		return render(type);
	}

	public Element renameFormElement(String type, String lang, String view) {
		String newName = strings.gts(type, KeyWords.NEW_NAME);

		Element form = form(type, lang, view);
		Element table = form.appendElement(HTML.TABLE);
		Element body = table.appendElement(HTML.TBODY);

		Element row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.TYPE) + ":");
		row.appendElement(HTML.TD).appendText(type);

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(newName + ":");
		row.appendElement(HTML.TD).appendElement(input(HTML.TEXT, KeyWords.NEW_NAME, newName));

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

		if (!permissions.isAllowed(type, id, action + "_" + KeyWords.FORM)) {
			return unauthorized(type, lang, view, new UnauthorizedActionException(type, action));
		}
		
		LinkedHashMap<String, TypeField> fields = nextNode.getActionFields(type, action);
		if (fields == null) {
			return notFound(type, lang, view, new ActionNotFoundException(type, action));
		}

		String title = strings.gts(type, KeyWords.EXECUTE_ACTION_TITLE);
		String typeName = strings.getTypeName(type);
		String actionName = strings.getActionName(type, action);
		setTitle(Utils.format(title, actionName, typeName));

		textEditors();
		main.appendElement(executeActionForm(type, id, action, actionName, fields, lang, view));

		return render();
	}

	public Element executeActionForm(String type, String id, String action, String actionName,
			LinkedHashMap<String, TypeField> fields, String lang, String view) {
		
		boolean showType = typeSettings.getActionBoolean(type, action, KeyWords.SHOW_TYPE);
		boolean showId = typeSettings.getActionBoolean(type, action, KeyWords.SHOW_ID);
		boolean showHeader = typeSettings.getActionBoolean(type, action, KeyWords.SHOW_HEADER);
		boolean showProgress = typeSettings.getActionBoolean(type, action, KeyWords.SHOW_PROGRESS);
		boolean showRange = typeSettings.getActionBoolean(type, action, KeyWords.SHOW_RANGE);
		
		return executeActionForm(type, id, action, actionName, fields, lang, view, showType,
				showId, showHeader, showProgress, showRange);
	}
	
	public Element executeActionForm(String type, String id, String action, String actionName,
			LinkedHashMap<String, TypeField> fields, String lang, String view, boolean showType,
			boolean showId, boolean showHeader, boolean showProgress, boolean showRange) {
		
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
				header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.TYPE));
			}

			header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.NAME));
			header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.VALUE));
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
			Element cell = row.appendElement(HTML.TD);
			cell.appendElement(fieldInput(type, action, field, fieldName, null, typeField, lang));
			
			if (showRange) {
				appendFieldRange(cell, typeField);
			}
		}

		Element actionButton = form.appendElement(button(actionName, action, Icon.CHEVRON_TOP,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, id, action)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return form;
	}
	
	@Override
	public Content importTypesForm(String lang, String view) {
		loadTemplate(null, lang, view);

		String title = strings.gts(Action.IMPORT_TYPES);
		String existingTypesAction = strings.gts(KeyWords.EXISTING_TYPES_ACTION);
		String existingObjectsAction = strings.gts(KeyWords.EXISTING_OBJECTS_ACTION);
		String file = strings.gts(KeyWords.FILE);
		boolean showProgress = typeSettings.getActionBoolean(null, Action.IMPORT_TYPES, KeyWords.SHOW_PROGRESS);

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
		row.appendElement(HTML.TD).appendElement(select(KeyWords.EXISTING_TYPES_ACTION, existingTypesAction,
				strings.getTypeTuple(null, KeyWords.EXISTING_TYPES_ACTIONS)));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(existingObjectsAction + ":");
		row.appendElement(HTML.TD).appendElement(select(KeyWords.EXISTING_OBJECTS_ACTION, existingObjectsAction,
				strings.getTypeTuple(null, KeyWords.EXISTING_OBJECTS_ACTIONS)));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(file + ":");
		row.appendElement(HTML.TD)
				.appendElement(binaryInput(KeyWords.DATA, title, Format.JSON.getContentType(), lang));

		Element actionButton = form.appendElement(button(strings.gts(KeyWords.IMPORT),
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
		String existingObjectsAction = strings.gts(KeyWords.EXISTING_OBJECTS_ACTION);
		String file = strings.gts(KeyWords.FILE);
		boolean showProgress = typeSettings.getActionBoolean(null, Action.IMPORT_OBJECTS, KeyWords.SHOW_PROGRESS);

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
		row.appendElement(HTML.TD).appendElement(select(KeyWords.EXISTING_OBJECTS_ACTION, existingObjectsAction,
				strings.getTypeTuple(null, KeyWords.EXISTING_OBJECTS_ACTIONS)));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(file + ":");
		row.appendElement(HTML.TD)
				.appendElement(binaryInput(KeyWords.DATA, title, Format.JSON.getContentType(), lang));

		Element actionButton = form.appendElement(button(strings.gts(KeyWords.IMPORT),
				Action.IMPORT_OBJECTS, Icon.UNSHARE_BOXED, SUBMIT_FORM));
		if (!permissions.isAllowed(Action.IMPORT_OBJECTS)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return render();
	}

	@Override
	public Content loginForm(String lang, String view) {
		loadTemplate(null, lang, view);

		String user = strings.gts(KeyWords.USER);
		String password = strings.gts(KeyWords.PASSWORD);

		setTitle(strings.gts(KeyWords.LOGIN_TITLE));
		Element form = form(lang, view);
		main.appendElement(form);

		Element table = form.appendElement(HTML.TABLE);
		Element body = table.appendElement(HTML.TBODY);

		Element row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(user + ":");
		row.appendElement(HTML.TD).appendElement(input(HTML.TEXT, KeyWords.LOGIN_USER, user));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(password + ":");
		row.appendElement(HTML.TD).appendElement(input(HTML.PASSWORD, KeyWords.LOGIN_PASSWORD, password));

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
		setTitle(strings.gts(type, KeyWords.TYPE) + ": " + type);

		Type typeObject = nextNode.getType(type);

		Element pre = main.appendElement(HTML.PRE);
		pre.appendText(new Serial(typeObject, Format.JSON).getString());

		return render(type);
	}

	@Override
	public Content getReferences(String lang, String view) {
		
		loadTemplate(null, lang, view);
		setTitle(strings.gts(KeyWords.REFERENCES));

		TreeMap<String, TreeMap<String, TreeMap<String, Reference>>> references = nextNode
				.getReferencesOrderByNames();

		Element table = main.appendElement(HTML.TABLE);
		Element header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		Element body = table.appendElement(HTML.TBODY);
		header.appendElement(HTML.TH).appendText(strings.gts(KeyWords.REFERENCED_TYPE));
		header.appendElement(HTML.TH).appendText(strings.gts(KeyWords.REFERENCING_TYPE));
		header.appendElement(HTML.TH).appendText(strings.gts(KeyWords.REFERENCING_FIELD));
				
		for (Map.Entry<String, TreeMap<String, TreeMap<String, Reference>>> referencedTypeEntry 
				: references.entrySet()) {
			
			String referencedTypeName = referencedTypeEntry.getKey();
			TreeMap<String, TreeMap<String, Reference>> referencingTypes = referencedTypeEntry.getValue();
			
			for (Map.Entry<String, TreeMap<String, Reference>> referencingTypeEntry
					: referencingTypes.entrySet()) {
				
				String referencingTypeName = referencingTypeEntry.getKey();
				TreeMap<String, Reference> referencingFields = referencingTypeEntry.getValue();
				
				for (Map.Entry<String, Reference> referencingFieldEntry : referencingFields.entrySet()) {
					
					String referencingFieldName = referencingFieldEntry.getKey();
					Reference reference = referencingFieldEntry.getValue();
					String referencedType = reference.getReferencedType();
					String referencingType = reference.getReferencingType();
														
					Element row = body.appendElement(HTML.TR);
					
					Element referencedTypeCell = row.appendElement(HTML.TD);
					
					if (permissions.isAllowed(referencedType, Action.GET_TYPE)) {
						referencedTypeCell.appendElement(anchor(referencedTypeName, 
								url(referencedType, lang, view) + parameter(KeyWords.INFO)));
					} else {
						referencedTypeCell.appendText(referencedTypeName);
					}
					
					Element referencingTypeCell = row.appendElement(HTML.TD);
					
					if (permissions.isAllowed(referencingType, Action.GET_TYPE)) {
						referencingTypeCell.appendElement(anchor(referencingTypeName,
								url(referencingType, lang, view) + parameter(KeyWords.INFO)));
					} else {
						referencingTypeCell.appendText(referencingTypeName);
					}
					
					row.appendElement(HTML.TD).appendText(referencingFieldName);
				}
			}
		}

		return render();
	}

	@Override
	public Content get(String type, String id, String lang, String view, String etag) {
		loadTemplate(type, lang, view);

		String[] fields = typeSettings.getActionStringArray(type, Action.GET, KeyWords.FIELDS);
		LinkedHashMap<String, TypeField> typeFields = nextNode.getTypeFields(type, fields);

		NXObject object = nextNode.get(type, id, fields, lang, true, false, true, false, true, true);

		if (object == null) {
			return objectNotFound(type, id, lang, view);
		}

		main.appendElement(getElement(object, typeFields, lang, view));

		main.appendElement(downReferences(type, id, lang, view));

		return render(type);
	}

	public Element getElement(NXObject object, LinkedHashMap<String, TypeField> typeFields, String lang,
			String view) {
		Element article = document.createElement(HTML.ARTICLE);

		String typeName = strings.getTypeName(object.getType());

		article.appendElement(HTML.H1).appendText(typeName + ": " + object.getName());
		document.getTitle().appendText(typeName + ": " + object.getName());

		for (Entry<String, Object> entry : object.getFields().entrySet()) {
			String field = entry.getKey();
			Object value = entry.getValue();
			TypeField typeField = typeFields.get(field);
			article.appendElement(fieldWithLabelOutput(object, field, value, typeField, lang, view));
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
	public Content getElement(String type, String id, String field, String element, String lang,
			String view, String etag) {
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
			if (KeyWords.THUMBNAIL.equals(element)) {
				byte[] thumbnail = nextNode.getImageThumbnail(type, id, field);
				if (thumbnail != null) {
					content = new Content(thumbnail, Format.PNG);
				}
			} else {
				throw new ElementException(type, field, element, KeyWords.INVALID_ELEMENT);
			}
			break;

		default:
			throw new FieldException(type, field, KeyWords.FIELD_HAS_NO_ELEMENTS);
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
				
		String title = strings.gts(type, KeyWords.SELECT_TITLE);
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
			String[] searchTypes = typeSettings.getTypeStringArray(type, KeyWords.FULLTEXT_SEARCH_TYPES);
			if (searchTypes != null) {
				main.appendElement(HTML.H2).appendText(strings.gts(type, KeyWords.OTHER_TYPES));
				
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
		div.appendElement(HTML.STRONG).appendText(strings.gts(type, KeyWords.SEARCH) + ": ");
		div.appendText(search);
		
		String url = deleteSearchURL(type, lang, view, ref, filters, order);
		
		div.appendElement(iconAnchor(strings.gts(type, KeyWords.DELETE_SEARCH), url, Icon.DELETE));
		return div;
	}
	
	public String deleteSearchURL(String type, String lang, String view, FieldReference ref,
			Filter[] filters, LinkedHashMap<String, Order> order) {
		return url(type, lang, view) + refParameter(ref) + filtersParameters(filters)
			+ previewParameter(request.isPreview()) + orderParameter(order);
	}

	@Override
	public Content updateForm(String type, String id, String lang, String view) {
		loadTemplate(type, lang, view);
		String[] fields = typeSettings.getActionStringArray(type, Action.UPDATE, KeyWords.FIELDS);
		
		NXObject object = nextNode.get(type, id, fields, lang, true, false, false, false, false, false);

		if (object == null) {
			return objectNotFound(type, id, lang, view);
		}

		String title = strings.gts(type, KeyWords.UPDATE_TITLE);
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
		String title = strings.gts(type, KeyWords.UPDATE_ID_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));
		main.appendElement(updateIdFormElement(type, id, lang, view));
		return render(type);
	}

	@Override
	public Content updatePasswordForm(String type, String id, String field, String lang, String view) {
		loadTemplate(type, lang, view);
		String title = strings.gts(type, KeyWords.UPDATE_PASSWORD_TITLE);
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

	public Element typesTable(TreeMap<String, TypeInfo> types, String lang, String view) {
		
		boolean disableDropButton = true;
		boolean disableExportButton = true;
		
		Element form = form(lang, view);
		form.setAttribute(HTML.AUTOCOMPLETE, HTML.OFF).setAttribute(DATA_STRINGS_TYPES_DROP_CONFIRMATION,
				strings.gts(KeyWords.TYPES_DROP_CONFIRMATION));

		Element table = form.appendElement(HTML.TABLE);
		Element header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		Element body = table.appendElement(HTML.TBODY);

		Element allCheckbox = header.appendElement(HTML.TH).appendElement(allCheckbox());
		
		header.appendElement(HTML.TH).appendText(strings.gts(KeyWords.NAME));
		header.appendElement(HTML.TH).appendText(strings.gts(KeyWords.OBJECTS));
		header.appendElement(HTML.TH).appendText(strings.gts(KeyWords.SIZE));
		header.appendElement(HTML.TH);
		header.appendElement(HTML.TH);
		header.appendElement(HTML.TH);
		
		for (Map.Entry<String,TypeInfo> entry : types.entrySet()) {
			String typeName = entry.getKey();
			TypeInfo typeInfo = entry.getValue();
			String type = typeInfo.getName();
			
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
					.appendElement(input(HTML.CHECKBOX, KeyWords.TYPES, typeName, type)
					.addClass(ITEM_CHECKBOX));
			if (disableCheckbox) {
				checkbox.setAttribute(HTML.DISABLED);
			}
			
			Element selectCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(type, Action.SELECT)) {
				selectCell.appendElement(anchor(typeName, url(type, lang, view)));
			} else {
				selectCell.appendText(typeName);
			}			
			
			row.appendElement(HTML.TD).appendText(typeInfo.getObjects() + "");
			row.appendElement(HTML.TD).appendText(humanReadableBytes(typeInfo.getSize(), lang));
			
			Element insertCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(type, Action.INSERT_FORM)) {
				insertCell.appendElement(iconAnchor(strings.getActionName(type, Action.INSERT),
					url(type, lang, view) + formParameter(Action.INSERT), Icon.PLUS));
			}
			
			Element alterCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(type, Action.ALTER_FORM)) {
				alterCell.appendElement(iconAnchor(strings.getActionName(type, Action.ALTER),
						url(type, lang, view) + formParameter(Action.ALTER), Icon.PENCIL));
			}
			
			Element infoCell = row.appendElement(HTML.TD);
			
			if (permissions.isAllowed(type, Action.GET_TYPE)) {
				infoCell.appendElement(iconAnchor(strings.gts(type, KeyWords.TYPE),
						url(type, lang, view) + parameter(KeyWords.INFO), Icon.INFO));
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
				.setAttribute(HTML.TITLE, strings.gts(type, KeyWords.CHECK_UNCHECK_ALL))
				.addClass(ALL_CHECKBOX);
	}
	
	public Element insertForm(String type, String[] fields, String lang, String view,
			FieldReference ref) {
		
		boolean showType = typeSettings.getActionBoolean(type, Action.INSERT, KeyWords.SHOW_TYPE);
		boolean showId = typeSettings.getActionBoolean(type, Action.INSERT, KeyWords.SHOW_ID);
		boolean showHeader = typeSettings.getActionBoolean(type, Action.INSERT, KeyWords.SHOW_HEADER);
		boolean showProgress = typeSettings.getActionBoolean(type, Action.INSERT, KeyWords.SHOW_PROGRESS);
		boolean showRange = typeSettings.getActionBoolean(type, Action.INSERT, KeyWords.SHOW_RANGE);
		
		return insertForm(type, fields, lang, view, ref, showType, showId, showHeader, showProgress,
				showRange);
	}
	
	public Element insertForm(String type, String[] fields, String lang, String view,
			FieldReference ref, boolean showType, boolean showId, boolean showHeader,
			boolean showProgress, boolean showRange) {	
		
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
				header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.TYPE));
			}

			header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.NAME));
			header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.VALUE));
		}

		if (showId) {
			Element row = body.appendElement(HTML.TR);

			if (showType) {
				row.appendElement(HTML.TD).appendText(PT.STRING);
			}
			
			String idName = strings.getIdName(type);
			
			row.appendElement(HTML.TD).appendText(idName);
			row.appendElement(HTML.TD).appendElement(idInput(type, KeyWords.ID, idName));
		}

		if (ref != null) {
			String field = ref.getReferencingField();
			
			if (!ArrayUtils.contains(fields, field)) {
				form.appendElement(input(HTML.HIDDEN, "@" + field, null, ref.getReferencedId()));
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
						+ KeyWords.DEFAULT));
			} else {
				fieldCell.appendText(fieldName);
			}

			row.appendElement(insertFormCell(type, field, fieldName, value, typeField, lang, ref,
					showRange));
		}
		
		String actionName = strings.getActionName(type, Action.INSERT);

		Element actionButton = form.appendElement(button(actionName, Action.INSERT, Icon.PLUS,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, Action.INSERT) ||
				(ref != null && !permissions.isAllowedToMakeReference(type, null, ref))) {
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
		return form(url(type, id, field, lang, view))
				.setAttribute(DATA_STRINGS_ACCEPT, strings.gts(type, KeyWords.ACCEPT))
				.setAttribute(DATA_STRINGS_CANCEL, strings.gts(type, KeyWords.CANCEL));
	}

	public Element form(String action) {
		Element form = document.createElement(HTML.FORM).setAttribute(HTML.ACTION, action);

		if (request.isSecure()) {
			form.appendElement(input(HTML.HIDDEN, KeyWords.SESSION, KeyWords.SESSION, request.getSessionToken()));
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
			template = strings.gts(type, KeyWords.TEMPLATE);
			
			if (template == null) {
				template = typeSettings.gts(type, KeyWords.TEMPLATE);
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
			Filter refFilter = new Filter(ref.getReferencingField(), Comparison.EQUAL,
					ref.getReferencedId(), false);
			
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

		String[] fields = typeSettings.getActionStringArray(type, Action.SELECT, KeyWords.FIELDS);
		Objects result = nextNode.select(type, fields, lang, refAndFilters, search, order, offset, limit);
		NXObject[] objects = result.getItems();
		Long count = result.getCount();
		offset = result.getOffset();
		limit = result.getLimit();
		
		HTMLView htmlView = null;

		if (Component.REFERENCE.equals(component)) {
			htmlView = getHTMLView(type, view);
			select.appendElement(htmlView.referenceSelectHeader(type, lang, view, ref, search, count, 
					component));
		} else {
			htmlView = this;
			select.appendElement(HTML.P).appendText(" " + count + " " + strings.gts(type, KeyWords.OBJECTS));		
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
		selectHeader.appendText(" " + count + " " + strings.gts(type, KeyWords.OBJECTS));
		selectHeader.appendElement(HTML.NAV).addClass(SELECT_MENU)
				.appendElements(typeMenuElements(type, null, lang, view, ref, search, component));
		
		return selectHeader;
	}
	
	@Override
	public Content filterComponent(String type, String field, String lang, String view, int count) {
		document = new HTML();
		document.setDocType(null);
		
		if (field == null) {
			field = KeyWords.ID;
		}
		
		Element filter = filter(type, new Filter(field, Comparison.EQUAL, null, true), count,
				nextNode.getTypeFields(type), lang);
		
		return new Content(filter.toString());
	}	
	
	public Element filters(String type, Filter[] filters, LinkedHashMap<String, TypeField> typeFields,
			String lang) {
		
		Element div = document.createElement(HTML.DIV);
		
		div.appendElement(HTML.STRONG).appendText(strings.gts(type, KeyWords.FILTERS) + ": ");
		div.appendElement(button(strings.gts(type, KeyWords.ADD_FILTER), ADD_FILTER));
		div.appendElement(submitButton(strings.gts(type, KeyWords.SEARCH)))
				.setAttribute(HTML.FORM, KeyWords.SEARCH);
		
		Element table = div.appendElement(HTML.TABLE).setAttribute(HTML.ID, KeyWords.FILTERS);
		Element header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.FIELD));
		header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.COMPARISON));
		header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.VALUE));
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
		Object filterValue = filter.getValue();
		
		Element fieldSelect = row.appendElement(HTML.TD).appendElement(HTML.SELECT)
				.addClass(FILTER_FIELD)
				.setAttribute(HTML.NAME, KeyWords.FILTERS + ":" + count + ":" + KeyWords.FIELD)
				.setAttribute(HTML.FORM, KeyWords.SEARCH);
				
		Element option = fieldSelect.appendElement(HTML.OPTION)
				.setAttribute(HTML.VALUE, KeyWords.ID)
				.appendText(strings.getIdName(type));
		
		if (KeyWords.ID.equals(filterField)) {
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
				.addClass(FILTER_COMPARISON)
				.setAttribute(HTML.NAME, KeyWords.FILTERS + ":" + count + ":" + KeyWords.COMPARISON)
				.setAttribute(HTML.FORM, KeyWords.SEARCH);
	
		for (Comparison comparison : Comparison.values()) {
			option = comparisonSelect.appendElement(HTML.OPTION)
				.setAttribute(HTML.VALUE, comparison)
				.appendText(strings.getComparisonName(type, comparison.toString()));
			
			if (comparison.equals(filter.getComparison())) {
				option.setAttribute(HTML.SELECTED);
			}
		}
		
		Element filterInput = null;
		Element filterTextInput = null;
		TypeField typeField = typeFields.get(filterField);
		String valueName = KeyWords.FILTERS + ":" + count + ":" + KeyWords.VALUE;
				
		if (KeyWords.ID.equals(filterField)) {
			String idName = strings.getIdName(type);
			
			filterInput = filterObjectInput(valueName, idName, filterValue, type, true, lang);
			
			filterTextInput = filterObjectTextInput(valueName, idName, filterValue, type);
		} else {
			String fieldType = typeField.getType();
						
			if (PT.isTextType(fieldType)) {
				typeField = typeField.getStringTypeField();
			} 
			
			String filterFieldName = strings.getFieldName(type, filterField);
			filterInput = fieldInput(type, Action.SEARCH, filterField, filterFieldName,
					filterValue, typeField, lang).setAttribute(HTML.NAME, valueName);
			
			if (!PT.STRING.equals(fieldType) && !PT.TEL.equals(fieldType) && !PT.isTextType(fieldType)) {
								
				typeField = typeField.getStringTypeField();
				
				filterTextInput = fieldInput(type, Action.SEARCH, filterField, filterFieldName,
						filterValue, typeField, lang).setAttribute(HTML.NAME, valueName);
			}
		}
		
		filterInput.addClass(FILTER_INPUT);
		filterInput.setAttribute(HTML.FORM, KeyWords.SEARCH);
		
		Element inputCell = row.appendElement(HTML.TD);
		inputCell.appendElement(filterInput);
				
		if (filterTextInput != null) {
			filterTextInput.addClass(FILTER_TEXT_INPUT);
			filterTextInput.setAttribute(HTML.FORM, KeyWords.SEARCH);
			
			inputCell.appendElement(filterTextInput);
			
			if (Comparison.LIKE.equals(filter.getComparison())
				|| Comparison.NOT_LIKE.equals(filter.getComparison())) {
				
				filterInput.addClass(HTML.HIDDEN);
				filterInput.setAttribute(HTML.DISABLED);
				filterInput.removeAttribute(HTML.VALUE);
				
			} else {	
				filterTextInput.addClass(HTML.HIDDEN);
				filterTextInput.setAttribute(HTML.DISABLED);
				filterTextInput.removeAttribute(HTML.VALUE);
			}
		}
		
		String dropFilter = strings.gts(type, KeyWords.DROP_FILTER);
		
		row.appendElement(HTML.TD).appendElement(smallButton(dropFilter, Icon.MINUS, DELETE_ROW))
			.setAttribute(HTML.FORM, KeyWords.SEARCH);
		
		return row;
	}

	public Element referenceOutput(String type, String lang, String view, FieldReference ref,
			Filter[] filters, String search, LinkedHashMap<String, Order> order) {
		
		String referencedType = nextNode.getTypeField(type, ref.getReferencingField()).getType();

		Element div = document.createElement(HTML.DIV).addClass(REFERENCE_OUTPUT);
		div.appendElement(HTML.STRONG).appendText(strings.getFieldName(type, ref.getReferencingField())
				+ ": ");
		div.appendElement(anchor(nextNode.getName(referencedType, ref.getReferencedId(), lang),
				url(referencedType, ref.getReferencedId(), lang, view)));
		
		String url = url(type, lang, view) + filtersParameters(filters) + searchParameter(search)
			+ orderParameter(order) + calendarParameter(request.isCalendar());
		div.appendElement(iconAnchor(strings.gts(type, KeyWords.DELETE_REFERENCE), url, Icon.DELETE));
		
		return div;
	}

	public Element downReferences(String referencedType, String referencedId, String lang, String view) {
		Element references = document.createElement(HTML.DIV);

		for (TypeReference downReference : nextNode.getDownReferences(referencedType)) {
			FieldReference ref = new FieldReference(downReference.getReferencingField(), referencedType,
					referencedId);
			references.appendElement(selectElement(downReference.getReferencingType(), lang, view, ref,
					null, null, null, 0L, null, Component.REFERENCE));
		}

		return references;
	}
	
	public Element updateForm(NXObject object, String[] fields, String lang, String view) {
				
		String type = object.getType();
		
		boolean showType = typeSettings.getActionBoolean(type, Action.UPDATE, KeyWords.SHOW_TYPE);
		boolean showId = typeSettings.getActionBoolean(type, Action.UPDATE, KeyWords.SHOW_ID);
		boolean showHeader = typeSettings.getActionBoolean(type, Action.UPDATE, KeyWords.SHOW_HEADER);
		boolean showProgress = typeSettings.getActionBoolean(type, Action.UPDATE, KeyWords.SHOW_PROGRESS);
		boolean showRange = typeSettings.getActionBoolean(type, Action.UPDATE, KeyWords.SHOW_RANGE);
		
		return updateForm(object, fields, lang, view, showType, showId, showHeader, showProgress,
				showRange);
	}
	
	public Element updateForm(NXObject object, String[] fields, String lang, String view,
			boolean showType, boolean showId, boolean showHeader, boolean showProgress,
			boolean showRange) {
		
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
				header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.TYPE));
			}

			header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.NAME));
			header.appendElement(HTML.TH).appendText(strings.gts(type, KeyWords.VALUE));
		}

		form.appendElement(input(HTML.HIDDEN, KeyWords.UDATE, KeyWords.UDATE, object.getUDate()));

		Element row = body.appendElement(HTML.TR);

		if (showType) {
			row.appendElement(HTML.TD).appendText(PT.STRING);
		}

		if (showId) {
			row.appendElement(HTML.TD).appendText(strings.getIdName(type));
			
			Element idCell = row.appendElement(HTML.TD);
			idCell.appendText(object.getId());
			
			if (permissions.isAllowed(type, object.getId(), Action.UPDATE_ID_FORM)) {
				idCell.appendText(" ");
				idCell.appendElement(iconAnchor(strings.getActionName(type, Action.UPDATE_ID),
						url(object.getType(), object.getId(), lang, view)
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
			
			row.appendElement(updateFormCell(object, field, fieldName, value, typeField, lang, view,
					showRange));
		}

		String actionName = strings.getActionName(object.getType(), Action.UPDATE);

		Element actionButton = form.appendElement(button(actionName, Action.UPDATE, Icon.PENCIL,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, object.getId(), Action.UPDATE)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return form;
	}

	public Element updateIdFormElement(String type, String id, String lang, String view) {
		String newId = strings.gts(type, KeyWords.NEW_ID);

		Element form = form(type, id, lang, view);
		Element table = form.appendElement(HTML.TABLE);
		Element body = table.appendElement(HTML.TBODY);

		Element row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(strings.getIdName(type) + ":");
		row.appendElement(HTML.TD).appendText(id);

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(newId + ":");
		row.appendElement(HTML.TD).appendElement(idInput(type, KeyWords.NEW_ID, newId));

		String actionName = strings.getActionName(type, Action.UPDATE_ID);

		Element actionButton = form.appendElement(button(actionName, Action.UPDATE_ID, Icon.PENCIL,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, id, Action.UPDATE_ID)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return form;
	}
	
	public Element updatePasswordFormElement(String type, String id, String field, String lang, String view) {
		Element form = form(type, id, field, lang, view);
		Element table = form.appendElement(HTML.TABLE);
		Element body = table.appendElement(HTML.TBODY);

		String currentPassword = strings.gts(type, KeyWords.CURRENT_PASSWORD);
		String newPassword = strings.gts(type, KeyWords.NEW_PASSWORD);
		String repeatNewPassword = strings.gts(type, KeyWords.NEW_PASSWORD_REPEAT);

		Element row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(strings.getIdName(type) + ":");
		row.appendElement(HTML.TD).appendText(id);

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(currentPassword + ":");
		row.appendElement(HTML.TD).appendElement(input(HTML.PASSWORD, KeyWords.CURRENT_PASSWORD, currentPassword));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(newPassword + ":");
		row.appendElement(HTML.TD).appendElement(input(HTML.PASSWORD, KeyWords.NEW_PASSWORD, newPassword));

		row = body.appendElement(HTML.TR);
		row.appendElement(HTML.TH).appendText(repeatNewPassword + ":");
		row.appendElement(HTML.TD)
				.appendElement(input(HTML.PASSWORD, KeyWords.NEW_PASSWORD_REPEAT, repeatNewPassword));

		String actionName = strings.getActionName(type, Action.UPDATE_PASSWORD);

		Element actionButton = form.appendElement(button(actionName, Action.UPDATE_PASSWORD, Icon.PENCIL,
				SUBMIT_FORM));
		if (!permissions.isAllowed(type, id, Action.UPDATE_PASSWORD)) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		return form;
	}

	public Element insertFormCell(String type, String field, String title, Object value, 
			TypeField typeField, String lang, FieldReference ref, boolean showRange) {
		Element cell = document.createElement(HTML.TD);
		Element input = null;

		if (ref != null && field.equals(ref.getReferencingField())) {
			input = document.createElement(HTML.DIV)
					.appendElement(input(HTML.HIDDEN, "@" + ref.getReferencingField(), title,
							ref.getReferencedId()));
			cell.addClass(REFERENCE_FIELD);
			cell.appendText(nextNode.getName(typeField.getType(), ref.getReferencedId(), lang));
		} else {
			input = fieldInput(type, Action.INSERT, field, title, value, typeField, lang);
		}

		cell.appendElement(input);
		
		if (showRange) {
			appendFieldRange(cell, typeField);
		}
		
		return cell;
	}

	public Element updateFormCell(NXObject object, String field, String title, Object value,
			TypeField typeField, String lang, String view, boolean showRange) {
		Element cell = document.createElement(HTML.TD);
		Element input = null;
		
		if (typeField.getType().equals(PT.PASSWORD)) {
			input = passwordFieldOutput(object.getType(), object.getId(), field, lang, view);
		} else {
			input = fieldInput(object.getType(), Action.UPDATE, field, title, value, typeField, lang);
		}

		cell.appendElement(input);
		
		if (showRange) {
			appendFieldRange(cell, typeField);
		}
		
		return cell;
	}

	public Element passwordFieldOutput(String type, String id, String field, String lang, String view) {
		Element password = document.createElement(HTML.SPAN);
		password.appendText(Security.HIDDEN_PASSWORD + " ");
		
		if (permissions.isAllowed(type, id, Action.UPDATE_PASSWORD_FORM)) {
			password.appendElement(iconAnchor(strings.getActionName(type, Action.UPDATE_PASSWORD),
				url(type, id, field, lang, view) + formParameter(Action.UPDATE_PASSWORD), Icon.PENCIL));
		}
		
		return password;
	}

	public Element fieldInput(String type, String action, String field, String title, Object value,
			TypeField typeField, String lang) {

		Element input = null;
		
		switch (typeField.getType()) {
		case PT.STRING:
		case PT.INT16:
		case PT.INT32:
		case PT.INT64:
		case PT.FLOAT32:
		case PT.FLOAT64:
		case PT.NUMERIC:
		case PT.URL:
		case PT.EMAIL:
		case PT.DATE:
		case PT.TEL:
		case PT.TIME:
		case PT.DATETIME:
		case PT.COLOR:
			input = fieldInput(type, action, field, title, value, typeField);
			break;
		case PT.TEXT:
		case PT.HTML:
		case PT.JSON:
		case PT.XML:
			input = textAreaFieldInput(type, action, field, title, value, typeField);
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
			input = timeZoneFieldInput(field, title, value, typeField);
			break;
		case PT.BOOLEAN:
			input = booleanFieldInput(field, title, value);
			break;
		case PT.PASSWORD:
			input = passwordFieldInput(type, field, title);
			break;
		default:
			input = objectFieldInput(type, action, field, title, value, typeField, lang);
		}

		return input;
	}

	public Element binaryFieldInput(String type, String action, String field, String title, Object value,
			TypeField typeField, String lang) {
		
		String allowedContentTypes = typeSettings.getActionFieldString(type, action, field,
					KeyWords.ALLOWED_CONTENT_TYPES);
		
		if (allowedContentTypes == null && (Action.INSERT.equals(action)
				|| Action.UPDATE.equals(action))) {
			allowedContentTypes = typeSettings.getFieldString(type, field, KeyWords.ALLOWED_CONTENT_TYPES);
		}

		if (allowedContentTypes == null && PT.IMAGE.equals(typeField.getType())) {
			allowedContentTypes = Format.IMAGES.getContentType();
		}

		Element input = binaryInput("@" + field, title, value, allowedContentTypes, lang);
		Element clearAnchor = iconAnchor(strings.gts(type, KeyWords.CLEAR), null, Icon.DELETE)
				.addClasses(CLEAR_BINARY_INPUT, HTML.HIDDEN);
		
		input.appendElement(clearAnchor);
		
		if ((Action.INSERT.equals(action) || Action.UPDATE.equals(action)) && !typeField.isNotNull()) {
			input.appendElement(nullFieldInput(type, field, value));
		}
		
		return input;
	}
	
	public Element nullFieldInput(String type, String field, Object value) {
		Element nullFieldInput = document.createElement(HTML.SPAN).addClass(NULL_FIELD_INPUT);
		
		String nullName = strings.gts(type, KeyWords.NULL);
		
		nullFieldInput.appendText(" | " + nullName + ":");
	
		nullFieldInput.appendElement(booleanInput("@" + field + "_" + KeyWords.NULL, nullName, false))
			.addClass(KeyWords.NULL);
		
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

		input.appendText(strings.gts(type, KeyWords.REPEAT) + ": ");

		input.appendElement(input(HTML.PASSWORD, "@" + field + "_" + KeyWords.REPEAT, title))
			.setAttribute(HTML.MAXLENGTH, Security.BCRYPT_MAX_PASSWORD_LENGTH);

		return input;
	}

	public Element fieldInput(String type, String action, String field, String title, Object value,
			TypeField typeField) {
		
		Element input = null;
		
		String fieldType = typeField.getType();
		String inputType = null;
						
		if (PT.isStringType(fieldType)) {
			Integer size = typeSettings.getActionFieldInt32(type, action, field, KeyWords.INPUT_SIZE);
						
			switch (fieldType) {
			case PT.STRING:
				inputType = HTML.TEXT;
				break;
				
			case PT.URL:
				inputType = HTML.URL;
				break;
				
			case PT.EMAIL:
				inputType = HTML.EMAIL;
				break;
				
			case PT.TEL:
				inputType = HTML.TEL;
				break;
			}
			
			input = input(inputType, "@" + field, title, value);
			
			setFieldMaxLength(input, typeField);
			setFieldSize(input, size);
			setFieldRequired(input, typeField);
			
		} else if (PT.isTimeType(fieldType) || PT.isNumericType(fieldType)) {
									
			switch(fieldType) {
				
			case PT.DATE:
				inputType = HTML.DATE;
				break;
				
			case PT.TIME:
				inputType = HTML.TIME;
				break;
				
			case PT.DATETIME:
				inputType = HTML.DATETIME_LOCAL;
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
			
			input = input(inputType, "@" + field, title, value);
			
			setFieldRange(input, typeField);
			setFieldRequired(input, typeField);
			
		} else if (PT.COLOR.equals(fieldType)) {
			inputType = PT.COLOR;
			
			input = input(inputType, "@" + field, title, value);
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
			input.setAttribute(HTML.VALUE, value);
		}

		return input;
	}
	
	public void appendFieldRange(Element element, TypeField typeField) {
		FieldRange range = typeField.getRange();
		
		if (range != null) {
			Object min = range.getMin();
			Object max = range.getMax();
		
			if (min != null || max != null) {
				element.appendElement(fieldRange(min, max));
			}
		}
	}
	
	public Element fieldRange(Object min, Object max) {
		Element span = document.createElement(HTML.SPAN)
				.addClass(FIELD_RANGE);
		
		if (min != null) {
			span.appendText(min);
		}
			
		span.appendText(" - ");
			
		if (max != null) {
			span.appendText(max);
		}
				
		return span;
	}
	
	public void setFieldRange(Element input, TypeField typeField) {
		setFieldRange(input, typeField.getRange());
	}
	
	public void setFieldRange(Element input, FieldRange range) {
		if (range != null) {
			setFieldRange(input, range.getMin(), range.getMax());
		}
	}

	public void setFieldRange(Element input, Object min, Object max) {
		if (min != null || max != null) {
			input.setAttribute(HTML.STEP, HTML.ANY);
			
			if (min != null) {
				input.setAttribute(HTML.MIN, min);
			}
			
			if (max != null) {
				input.setAttribute(HTML.MAX, max);
			}
		}
	}
	
	public void setFieldMaxLength(Element input, TypeField typeField) {
		Integer maxLength = typeField.getLength();
		if (maxLength != null) {
			input.setAttribute(HTML.MAXLENGTH, maxLength);
		}
	}
	
	public void setFieldSize(Element input, Integer size) {
		if (size != null) {
			input.setAttribute(HTML.SIZE, size);
		}
	}
	
	public void setFieldRequired(Element input, TypeField typeField) {
		if (typeField.isNotNull()) {
			input.setAttribute(HTML.REQUIRED);
		}
	}

	public Element textAreaOutput(Object value, boolean preview) {
		String textAreaClass = null;
		if (preview) {
			textAreaClass = SMALL_TEXTAREA;
			value = value + " ...";
		} else {
			textAreaClass = MEDIUM_TEXTAREA;
		}

		Element textArea = document.createElement(HTML.TEXTAREA);
		textArea.appendText(value);
		textArea.addClass(textAreaClass);
		return textArea;
	}

	public Element documentFieldOutput(String type, String id, String field, Object value, String lang,
			boolean preview) {

		DocumentPreview docPrev = (DocumentPreview) value;

		Element span = document.createElement(HTML.SPAN);
		span.appendElement(textAreaOutput(docPrev.getText(), preview));
		span.appendElement(binaryFieldOutput(type, id, field, docPrev.getSize(), lang));

		return span;
	}
	
	public Element textAreaFieldInput(String type, String action, String field, String title, Object value,
			TypeField typeField) {
		return textAreaFieldInput(type, action, field, title, value, typeField.getType());
	}

	public Element textAreaFieldInput(String type, String action, String field, String title,
			Object value, String fieldType) {
		Element textArea = document.createElement(HTML.TEXTAREA).setAttribute(HTML.NAME, "@" + field)
				.setAttribute(HTML.TITLE, title);

		if (value != null) {
			textArea.appendText(value);
		}

		if (fieldType != null) {
			textArea.addClass(fieldType);
		}

		String[] modes = typeSettings.getActionFieldStringArray(type, action, field, KeyWords.EDITOR);
		
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
			textArea.setAttribute(DATA_EDITOR, modes[0]);

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

		return textArea;
	}
	
	public Element actionObjectsInput(String type, String action, String title, String lang) {
		Boolean notNull = typeSettings.getActionBoolean(type, action, KeyWords.OBJECTS_INPUT_NOT_NULL);
		String mode = typeSettings.getActionString(type, action, KeyWords.OBJECTS_INPUT_MODE);
		Integer size = typeSettings.getActionInt32(type, action, KeyWords.OBJECTS_INPUT_SIZE);
		Long limit = typeSettings.getActionInt64(type, action, KeyWords.OBJECTS_INPUT_LIMIT);
		
		return objectsInput(KeyWords.OBJECTS, title, null, type, action, notNull, mode, size, limit,
				lang);
	}
	
	public Element objectFieldInput(String type, String action, String field, String title, Object value,
			TypeField typeField, String lang) {
		
		String mode = typeSettings.getActionFieldString(type, action, field, KeyWords.OBJECT_INPUT_MODE);
		Long limit = typeSettings.getActionFieldInt64(type, action, field, KeyWords.OBJECT_INPUT_LIMIT);
		Integer	size = typeSettings.getActionFieldInt32(type, action, field, KeyWords.INPUT_SIZE);
								
		return objectInput("@" + field, title, value, typeField.getType(), type, action, field,
				typeField.isNotNull(), mode, size, limit, lang);
	}
	
	public Element filterObjectInput(String name, String title, Object value, String type,
			 boolean notNull, String lang) {
		
		String mode = typeSettings.getActionString(type, Action.SEARCH, KeyWords.OBJECT_INPUT_MODE);
		Long limit = typeSettings.getActionInt64(type, Action.SEARCH, KeyWords.OBJECT_INPUT_LIMIT);
		Integer size = typeSettings.getTypeInt32(type, KeyWords.ID_INPUT_SIZE);
				
		return objectInput(name, title, value, type, null, Action.SEARCH, null, notNull, mode, size,
				limit, lang);
	}
	
	public Element filterObjectTextInput(String name, String title, Object value, String type) {
		Integer size = typeSettings.getTypeInt32(type, KeyWords.ID_INPUT_SIZE);
		
		return objectTextInput(name, title, value, size);
	}
	
	public Element objectInput(String name, String title, Object value, String referencedType,
			String referencingType, String referencingAction, String referencingField,
			boolean notNull, String mode, Integer size, Long limit, String lang) {

		Element input = null;
				
		switch (mode) {
		case HTML.SELECT:			
			input = objectSelectInput(name, title, value, referencedType, referencingType, 
						referencingAction, referencingField, notNull, limit, lang);
			break;
			
		case HTML.TEXT:		
			input = objectTextInput(name, title, value, size);
			break;
			
		case HTML.RADIO:			
			input = objectRadioInput(name, title, value, referencedType, referencingType,
						referencingAction, referencingField, notNull, lang);
			break;
		
		case HTML.LIST:
			input = objectListInput(name, title, value, referencedType, referencingType,
						referencingAction, referencingField, size, lang);
			break;
			
		default:
			throw new InvalidValueException(KeyWords.INVALID_OBJECT_INPUT_MODE, mode);
		}	
		
		return input;
	}
	
	public Element objectsInput(String name, String title, Object value, String type,
			String action, boolean notNull, String mode, Integer size, Long limit, String lang) {

		Element input = null;
		
		switch(mode) {
		case HTML.TEXTAREA:			
			input = objectsTextAreaInput(name, title);
			break;
			
		case MULTIPLE_SELECT:
			input = objectsMultipleSelectInput(name, title, size, type, action, lang);
			break;
			
		case HTML.SELECT:			
			input = objectSelectInput(name, title, value, type, null, action, null, notNull, limit, 
					lang);
			break;
			
		case HTML.TEXT:		
			input = objectTextInput(name, title, value, size);
			break;
			
		case HTML.RADIO:			
			input = objectRadioInput(name, title, value, type, null, action, null, notNull, lang);
			break;
			
		default:
			throw new InvalidValueException(KeyWords.INVALID_OBJECTS_INPUT_MODE, mode);
		}
		
		return input;
	}
	
	public Element idInput(String type, String name, String title) {
		Integer size = typeSettings.getTypeInt32(type, KeyWords.ID_INPUT_SIZE);

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
	
	public String namesURL(String referencedType, String referencingType, String referencingAction,
			String referencingField, String lang) {
		return url(referencedType, lang, Format.JSON.toString()) + parameter(KeyWords.NAMES)
			+ arefParameter(referencingType, referencingAction, referencingField);
	}
	
	public Element objectListInput(String name, String title, Object value, String referencedType,
			String referencingType, String referencingAction, String referencingField, Integer size,
			String lang) {
				
		Element input = input(HTML.TEXT, name, title, value)
				.addClass(OBJECT_LIST_INPUT)
				.setAttribute(DATA_URL, namesURL(referencedType, referencingType, referencingAction,
						referencingField, lang));
				
		if (size != null) {
			input.setAttribute(HTML.SIZE, size);
		}
						
		return document.createListInput(input);
	}
	
	public Element objectsTextAreaInput(String name, String title) {
		
		return document.createElement(HTML.TEXTAREA)
				.setAttribute(HTML.NAME, name)
				.setAttribute(HTML.TITLE, title)
				.addClass(OBJECTS_TEXTAREA_INPUT);
	}
	
	public Element objectsMultipleSelectInput(String name, String title, Integer size, 
			String type, String action, String lang) {
		
		Element input = document.createElement(HTML.SELECT).setAttribute(HTML.NAME, name)
				.setAttribute(HTML.TITLE, title);

		Names names = nextNode.getNames(type, null, action, null, lang);

		for (Entry<String, String> entry : names.getItems().entrySet()) {
			String objectId = entry.getKey();
			String objectName = entry.getValue();

			input.appendElement(HTML.OPTION).setAttribute(HTML.VALUE, objectId).appendText(objectName);
		}
		
		input.setAttribute(HTML.MULTIPLE).setAttribute(HTML.SIZE, size);
		
		return input;
		
	}
	
	public Element objectSelectInput(String name, String title, Object value, String referencedType,
			String referencingType, String referencingAction, String referencingField,
			boolean notNull, Long limit, String lang) {
		
		String previous = strings.gts(referencingType, KeyWords.PREVIOUS);
		String next = strings.gts(referencingType, KeyWords.NEXT);
		
		Element input = document.createElement(HTML.SELECT).setAttribute(HTML.NAME, name)
				.setAttribute(HTML.TITLE, title).addClass(KeyWords.OBJECT)
				.setAttribute(DATA_URL, namesURL(referencedType, referencingType, referencingAction,
						referencingField, lang))
				.setAttribute(DATA_STRINGS_PREVIOUS, previous)
				.setAttribute(DATA_STRINGS_NEXT, next);
		
		if (notNull) {
			input.setAttribute(DATA_NOT_NULL);
		} else {
			input.appendElement(HTML.OPTION);
		}

		Names names = nextNode.getNames(referencedType, referencingType, referencingAction,
				referencingField, lang, null, 0L, limit);
		
		Long offset = 0L;
		
		if (limit != null) {
			input.setAttribute(DATA_OFFSET, offset);
			input.setAttribute(DATA_LIMIT, limit);
		}
		
		for (Entry<String, String> entry : names.getItems().entrySet()) {
			String objectId = entry.getKey();
			String objectName = entry.getValue();

			Element option = input.appendElement(HTML.OPTION).setAttribute(HTML.VALUE, objectId);
			if (objectId.equals(value)) {
				option.setAttribute(HTML.SELECTED);
			}
			option.appendText(objectName);
		}
		
		if (limit != null && (offset + limit < names.getCount())) {
			input.appendElement(HTML.OPTION).setAttribute(HTML.VALUE, "@" + KeyWords.NEXT)
				.appendText(next + " >>>>");
		}
		
		return input;
	}
	
	public Element objectRadioInput(String name, String title, Object value, String referencedType,
			String referencingType, String referencingAction, String referencingField,
			boolean notNull, String lang) {
		
		InputGroup inputGroup = document.createInputGroup();
		inputGroup.addClass(OBJECT_RADIO_INPUT);
		
		if (!notNull) {
			String nullName = strings.gts(referencedType, KeyWords.NULL);
			
			inputGroup.appendInput(input(HTML.RADIO, name, nullName, ""));
			
			inputGroup.appendText(nullName);
			
		}
		
		Names names = nextNode.getNames(referencedType, referencingType,
				referencingAction, referencingField, lang);
		
		for (Entry<String, String> entry : names.getItems().entrySet()) {
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
	
	public Element timeZoneFieldInput(String field, String title, Object value, TypeField typeField) {
		return timeZoneInput("@" + field, title, value, typeField.isNotNull());
	}

	public Element timeZoneInput(String name, String title, Object value, boolean notNull) {
		Element select = document.createElement(HTML.SELECT).setAttribute(HTML.NAME, name);
		
		if (!notNull) {
			select.appendElement(HTML.OPTION);
		}

		if (title == null) {
			title = name;
		}

		select.setAttribute(HTML.TITLE, title);

		ZoneId.getAvailableZoneIds().stream().sorted().forEach(timeZoneId -> {
			ZoneId timeZone = ZoneId.of(timeZoneId);
			ZoneOffset offset = timeZone.getRules().getOffset(Instant.now());

			Element option = select.appendElement(HTML.OPTION).setAttribute(HTML.VALUE, timeZoneId)
					.appendText(offset + " - " + timeZoneId);
			if (timeZone.equals(value)) {
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
					.appendText(entry.getValue());

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
	
	public Element dateOutput(Object value, String lang) {
		DateTimeFormatter format = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
				.withLocale(new Locale(lang));
		return document.createElement(HTML.TIME).appendText(((LocalDate)value).format(format));
	}
	
	public Element timeOutput(Object value, String lang) {
		DateTimeFormatter format = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
				.withLocale(new Locale(lang));
		return document.createElement(HTML.TIME).appendText(((LocalTime)value).format(format));
	}
	
	public Element dateTimeOutput(Object value, String lang) {
		DateTimeFormatter format = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
				.withLocale(new Locale(lang));
		return document.createElement(HTML.TIME).appendText(((LocalDateTime)value).format(format));
	}

	public Element time(Object time) {
		return document.createElement(HTML.TIME).appendText(time);
	}

	public Element colorOutput(Object color) {
		return document.createElement(HTML.SPAN)
				.setAttribute(HTML.STYLE, HTML.BACKGROUND_COLOR + ": " + color)
				.appendText(color);
	}

	public Element selectTableHeaderCell(String type, String field, String lang, String view,
			FieldReference ref, Filter[] filters, String search, LinkedHashMap<String, Order> order,
			Long offset, Long limit, Component component) {

		Element cell = document.createElement(HTML.TH);
		String fieldName = KeyWords.ID.equals(field) ? strings.getIdName(type) : strings.getFieldName(type, field);
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
					.setAttribute(DATA_MULTI_ORDER,	multiOrderParameter));

		return cell;
	}

	public Element selectTable(String type, NXObject[] objects, LinkedHashMap<String, TypeField> typeFields,
			String lang, String view, Long count, Long offset, Long limit, Long minLimit, Long maxLimit,
			Long limitIncrement, String search, LinkedHashMap<String, Order> order, Component component) {

		return selectTable(type, objects, typeFields, lang, view, null, null, search, order, count,
				offset, limit, minLimit, maxLimit, limitIncrement, component);
	}
	
	public LinkedHashMap<String, String[]> disallowedReferences(NXObject[] objects, 
			LinkedHashMap<String, TypeField> typeFields) {
		
		LinkedHashMap<String, ArrayList<String>> references = new LinkedHashMap<>();
		LinkedHashMap<String, String[]> disallowedReferences = new LinkedHashMap<>();
		
		for (NXObject object : objects) {
			for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
				String field = entry.getKey();
				Object value = entry.getValue();
				String fieldType = typeFields.get(field).getType();
				
				if (!PT.isPrimitiveType(fieldType) && value != null) {
					ArrayList<String> fieldReferences = references.get(field);
					
					if (fieldReferences == null) {
						fieldReferences = new ArrayList<>();
						references.put(field, fieldReferences);
					}
					
					fieldReferences.add(((ObjectReference) value).getId());
				}
			}
		}
		
		for (Map.Entry<String, ArrayList<String>> entry : references.entrySet()) {
			String field = entry.getKey();
			ArrayList<String> fieldReferences = entry.getValue();
			String fieldType = typeFields.get(field).getType();
			
			disallowedReferences.put(field, getPermissions(fieldType)
					.isAllowed(fieldType, fieldReferences.toArray(new String[] {}), Action.GET));
		}
		
		return disallowedReferences;
	}

	public Element selectTable(String type, NXObject[] objects, LinkedHashMap<String, TypeField> typeFields,
			String lang, String view, FieldReference ref, Filter[] filters, String search,
			LinkedHashMap<String, Order> order, Long count, Long offset, Long limit, Long minLimit,
			Long maxLimit, Long limitIncrement, Component component) {

		String[] updateDisallowedObjects = permissions.isAllowed(type, objects, Action.UPDATE_FORM);
		String[] getDisallowedObjects = permissions.isAllowed(type, objects, Action.GET);
		String[] deleteDisallowedObjects = permissions.isAllowed(type, objects, Action.DELETE);
		String[] exportDisallowedObjects = permissions.isAllowed(type, objects, Action.EXPORT_OBJECTS);
		
		LinkedHashMap<String, String[]> disallowedReferences = disallowedReferences(objects, typeFields);
		
		Element form = form(type, lang, view).setAttribute(HTML.AUTOCOMPLETE, HTML.OFF)
				.setAttribute(DATA_URL, selectTableURL(type, lang, view, ref, filters, search, order,
						offset, limit))
				.setAttribute(DATA_STRINGS_OBJECTS_DELETE_CONFIRMATION,
						strings.gts(type, KeyWords.OBJECTS_DELETE_CONFIRMATION));

		form.appendElement(input(HTML.HIDDEN, KeyWords.ORDER, KeyWords.ORDER, orderString(order)));

		Element index = selectTableIndex(type, lang, view, ref, filters, search, order, count,
				offset, limit, minLimit, maxLimit, limitIncrement, component);
		Element indexHeader = index;
		Element indexFooter = index.clone();
		form.appendElement(indexHeader);

		Element table = form.appendElement(HTML.TABLE);

		Element header = table.appendElement(HTML.THEAD).appendElement(HTML.TR);
		Element body = table.appendElement(HTML.TBODY);

		Element allCheckbox = header.appendElement(HTML.TH).appendElement(allCheckbox(type));
		
		if (objects.length == deleteDisallowedObjects.length
				&& objects.length == exportDisallowedObjects.length) {
			allCheckbox.setAttribute(HTML.DISABLED);
		}

		header.appendElement(selectTableHeaderCell(type, KeyWords.ID, lang, view, ref, filters, 
				search, order, offset, limit, component));

		for (Map.Entry<String, Object> entry : objects[0].getFields().entrySet()) {
			header.appendElement(selectTableHeaderCell(type, entry.getKey(), lang, view, ref, filters, 
					search, order, offset, limit, component));
		}

		header.appendElement(HTML.TH);
		
		for (NXObject object : objects) {
			String id = object.getId();
			
			Element row = body.appendElement(HTML.TR);

			Element checkbox = row.appendElement(HTML.TD).appendElement(
					input(HTML.CHECKBOX, KeyWords.OBJECTS, strings.getObjectsName(type), id)
							.addClass(ITEM_CHECKBOX));
			if (ArrayUtils.contains(deleteDisallowedObjects, id)
					&& ArrayUtils.contains(exportDisallowedObjects, id)) {
				checkbox.setAttribute(HTML.DISABLED);
			}
			
			String idString = null;
			if (id.length() >= 25) {
				idString = id.substring(0, 22) + "...";
			} else {
				idString = id;
			}

			Element idCell = row.appendElement(HTML.TD);
			
			if (!ArrayUtils.contains(getDisallowedObjects, id)) {
				idCell.appendElement(HTML.A).appendText(idString)
					.setAttribute(HTML.HREF, url(object.getType(), id, lang, view));
			} else {
				idCell.appendText(idString);
			}

			for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
				String field = entry.getKey();
				Object value = entry.getValue();
				TypeField typeField = typeFields.get(field);
				String fieldType = typeField.getType();
				
				Element outputCell = row.appendElement(HTML.TD);
				
				if (!PT.isPrimitiveType(fieldType) && value != null && ArrayUtils
						.contains(disallowedReferences.get(field), ((ObjectReference) value).getId())) {
					
					outputCell.appendText(((ObjectReference) value).getName());
					
				} else {
					outputCell.appendElement(fieldOutput(object, field, value, typeField, lang, view, true));
				}
			}

			if (!ArrayUtils.contains(updateDisallowedObjects, id)) {
				String updateActionName = strings.getActionName(object.getType(), Action.UPDATE);
				
				row.appendElement(HTML.TD).appendElement(iconAnchor(updateActionName,
					url(object.getType(), id, lang, view)
					+ formParameter(Action.UPDATE), Icon.PENCIL));
			}
		}

		form.appendElement(indexFooter);

		Element div = form.appendElement(HTML.DIV);
		div.addClass(SELECT_BUTTONS);

		String actionName = strings.getActionName(type, Action.DELETE);

		Element actionButton = button(actionName, Action.DELETE, Icon.MINUS, SUBMIT_FORM);
		if (objects.length == deleteDisallowedObjects.length) {
			actionButton.setAttribute(HTML.DISABLED);
		}

		if (component != null) {
			actionButton.setAttribute(DATA_COMPONENT, component);
		}
		
		div.appendElement(actionButton);

		div.appendElement(exportButton(type, objects.length == exportDisallowedObjects.length));

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
			
			String includeObjects = strings.gts(KeyWords.INCLUDE_OBJECTS);
			buttons.appendText(includeObjects);
			
			Element includeObjectsCheckbox = buttons.appendElement(
					booleanInput(KeyWords.INCLUDE_OBJECTS, includeObjects, false));
			if (disabled) {
				includeObjectsCheckbox.setAttribute(HTML.DISABLED);
			}
			
			buttons.appendElement(HTML.BR);
		} else {
			action = Action.EXPORT_OBJECTS;			
		}

		Element actionButton = buttons.appendElement(button(strings.gts(type, KeyWords.EXPORT), 
				action, Icon.SHARE_BOXED, KeyWords.EXPORT));
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
			Long longObjectsCount = typeSettings.getTypeInt64(type, KeyWords.LONG_OBJECTS_COUNT);

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

		String offsetTextMode = typeSettings.gts(type, KeyWords.OFFSET_TEXT_MODE);

		for (Long offset = 0L; offset < count; offset += limit) {
			String text = selectTableIndexOffsetText(offsetTextMode, count, offset, limit);

			if (offset != selectedOffset) {
				index.add(selectTableAnchor(text, type, lang, view, ref, filters, search, order, offset,
						limit, component).addClass(KeyWords.OFFSET));
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

		String offsetTextMode = typeSettings.gts(type, KeyWords.OFFSET_TEXT_MODE);

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
					rightOffset, limit, component).addClass(KeyWords.OFFSET));
		}

		for (int x = 1; leftOffset > 0; x *= 2) {
			leftOffset = leftOffset - (limit * x);

			if (leftOffset < 0) {
				leftOffset = 0;
			}

			text = selectTableIndexOffsetText(offsetTextMode, count, leftOffset, limit);

			index.add(0, selectTableAnchor(text, type, lang, view, ref, filters, search, order,
					leftOffset, limit, component).addClass(KeyWords.OFFSET));
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
		Element select = document.createElement(HTML.SELECT).addClass(KeyWords.LIMIT);

		if (Component.REFERENCE.equals(component)) {
			select.setAttribute(DATA_COMPONENT, component);
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
				.setAttribute(HTML.HREF, selectTableURL(type, lang, view, ref, filters, search,
						order, offset, limit))
				.appendText(text);

		if (Component.REFERENCE.equals(component)) {
			anchor.setAttribute(DATA_COMPONENT, component);
		}

		return anchor;
	}

	public String selectTableURL(String type, String lang, String view, FieldReference ref, 
			Filter[] filters, String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		return url(type, lang, view) + refParameter(ref) + filtersParameters(filters)
			+ searchParameter(search) + orderParameter(order) + parameter(KeyWords.OFFSET, offset)
			+ parameter(KeyWords.LIMIT, limit) + previewParameter(request.isPreview());
	}
	
	public String parameter(String name) {
		return "&" + name;
	}

	public String parameter(String name, Object value) {
		return value != null ? "&" + name + "=" + value : "";
	}

	public String refParameter(FieldReference ref) {
		return ref != null ? parameter(KeyWords.REF, refString(ref)) : "";
	}
	
	public String refString(FieldReference ref) {
		return ref.getReferencingField() + ":" + ref.getReferencedId();
	}
	
	public String arefParameter(String referencingType, String referencingAction,
			String referencingField) {
		return referencingType != null || referencingAction != null || referencingField != null
				? parameter(KeyWords.AREF, arefString(referencingType, referencingAction, 
						referencingField)) : "";
	}
	
	public String arefString(String referencingType, String referencingAction, String referencingField) {
		return referencingType + ":" + referencingAction + ":" + referencingField;
	}
	
	public String formParameter(String action) {
		return parameter(KeyWords.FORM, action);
	}
	
	public String previewParameter(boolean preview) {
		return preview ? previewParameter() : "";
	}
	
	public String calendarParameter(boolean calendar) {
		return calendar ? calendarParameter() : "";
	}
	
	public String previewParameter() {
		return parameter(Action.PREVIEW);
	}
	
	public String calendarParameter() {
		return parameter(Action.CALENDAR);
	}
	
	public String searchParameter(String search) {
		return parameter(KeyWords.SEARCH, search);
	}
	
	public String orderParameter(LinkedHashMap<String, Order> order) {
		return parameter(KeyWords.ORDER, orderString(order));
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
				String parameterRoot = parameter(KeyWords.FILTERS + ":" + x + ":");
			
				parameters.append(parameterRoot + KeyWords.FIELD + "=" + filter.getField());
			
				parameters.append(parameterRoot + KeyWords.COMPARISON + "=" + filter.getComparison());
			
				parameters.append(parameterRoot + KeyWords.VALUE + "=");
				
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
		return listFieldOutput(type, label, objects, id -> url(objectsType, id, lang, view));
	}
	
	public Element listFieldOutput(String type, String label, Object[] objects,
			Function<String, String> urlFunction) {
		
		boolean tuple = objects instanceof Tuple[];
		
		Element output = document.createElement(HTML.DIV);
		output.addClass(FIELD_OUTPUT);
		
		output.appendElement(HTML.STRONG).appendText(label + ": ");
		
		String id, name = null;
		
		if (objects != null && objects.length > 0) {
			
			int last = objects.length - 1;
			
			for (int x = 0; x < objects.length; x++) {
				if (tuple) {
					id = ((Tuple)objects[x]).getString(KeyWords.ID);
					name = ((Tuple)objects[x]).getString(KeyWords.NAME);
				} else {
					id = ((String[])objects[x])[0];
					name = ((String[])objects[x])[1];
				}
				
				output.appendElement(anchor(name, urlFunction.apply(id)));
			
				if (x < last) {
					output.appendText(" | ");
				}
			}
		}
				
		return output;
	}

	public Element fieldWithLabelOutput(NXObject object, String field, Object value, TypeField typeField,
			String lang, String view) {
		Element output = document.createElement(HTML.DIV);
		output.addClass(FIELD_OUTPUT);
		
		String fieldType = typeField.getType();
		String fieldName = strings.getFieldName(object.getType(), field);
		output.appendElement(HTML.STRONG).appendText(fieldName + ": ");
		
		if (value != null && !PT.isPrimitiveType(fieldType) && !getPermissions(fieldType)
				.isAllowed(fieldType, ((ObjectReference) value).getId(), Action.GET)) {
			output.appendText(((ObjectReference) value).getName());
		} else {
			output.appendElement(fieldOutput(object, field, value, typeField, lang, view, false));
		}
		
		return output;
	}

	public Element fieldOutput(NXObject object, String field, Object value, TypeField typeField,
			String lang, String view, boolean preview) {
		Element fieldElement = null;

		if (value != null) {

			switch (typeField.getType()) {
			case PT.INT16:
			case PT.INT32:
			case PT.INT64:
			case PT.FLOAT32:
			case PT.FLOAT64:
			case PT.NUMERIC:
				fieldElement = numericOutput(value, lang);
				break;
			case PT.STRING:
			case PT.TIMEZONE:
				fieldElement = textOutput(value);
				break;
			case PT.BOOLEAN:
				fieldElement = booleanOutput(value);
				break;
			case PT.DATE:
				fieldElement = dateOutput(value, lang);
				break;
			case PT.TIME:
				fieldElement = timeOutput(value, lang);
				break;
			case PT.DATETIME:
				fieldElement = dateTimeOutput(value, lang);
				break;
			case PT.COLOR:
				fieldElement = colorOutput(value);
				break;
			case PT.URL:
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
				fieldElement = textAreaOutput(value, preview);
				break;
			case PT.PASSWORD:
				fieldElement = passwordOutput();
				break;
			default:
				fieldElement = referenceAnchor(value, typeField, lang, view);
			}
		} else {
			fieldElement = document.createElement(HTML.SPAN);
		}

		return fieldElement;
	}
	
	public Element fieldAnchor(Object value, TypeField typeField) {
		return fieldAnchor(value, typeField.getType());
	}
	
	public Element fieldAnchor(Object value, String fieldType) {
		String href = value.toString();

		switch (fieldType) {
		case PT.EMAIL:
			href = HTML.MAILTO + ":" + href;
			break;
		case PT.TEL:
			href = HTML.TEL + ":" + href;
			break;
		}

		return anchor(value.toString(), href);
	}
	
	public Element referenceAnchor(Object value, TypeField typeField, String lang, String view) {
		return referenceAnchor(value, typeField.getType(), lang, view);
	}

	public Element referenceAnchor(Object value, String fieldType, String lang, String view) {
		Element anchor = null;
		
		ObjectReference reference = (ObjectReference) value;
		
		anchor = anchor(reference.getName(), url(fieldType, reference.getId(), lang, view));
				
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
		binary.appendElement(anchor(humanReadableBytes((Integer) value, lang),
				url(type, id, field, null, null)));
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
	
	public Element numericOutput(Object value, String lang) {
		NumberFormat format =  NumberFormat.getNumberInstance(new Locale(lang));
		return document.createElement(HTML.SPAN).appendText(format.format(value));
	}

	public Element textOutput(Object value) {
		return document.createElement(HTML.SPAN).appendText(value);
	}

	public Element passwordOutput() {
		Element password = document.createElement(HTML.SPAN);
		password.appendText(Security.HIDDEN_PASSWORD);
		return password;
	}

	public Element anchor(String text, URL url) {
		return anchor(text, url.toString());
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
		Element anchor = document.createElement(HTML.A);
		
		if (href != null) {
			anchor.setAttribute(HTML.HREF, href);
		}
		
		anchor.appendElement(image(text, src));
		return anchor;
	}

	public Element iconAnchor(String text, String href, String icon) {
		return imageAnchor(text, href, "/static/icons/" + icon + ".svg").addClass(ICON);
	}

	public Element logoAnchor(String type, String lang, String view) {
		return imageAnchor(strings.gts(type, KeyWords.LOGO_TEXT),
				hrefURL(typeSettings.gts(type, KeyWords.LOGO_URL), lang, view),
				typeSettings.gts(type, KeyWords.LOGO));
	}

	public Element image(String text, String type, String id, String field) {
		return image(text, "/" + type + "/" + id + "/" + field + "/" + KeyWords.THUMBNAIL);
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
	
	public Element submitButton(String text) {
		return document.createElement(HTML.BUTTON)
			.setAttribute(HTML.TYPE, HTML.SUBMIT)
			.appendText(text);
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
		String searchParameter = parameter(KeyWords.SEARCH, search);

		if (id != null && form != null && permissions.isAllowed(type, id, Action.GET)) {
			elements.add(iconAnchor(strings.gts(type, KeyWords.VIEW), url(type, id, lang, view)
					+ refParameter, Icon.MAGNIFYING_GLASS));
		}

		if (!Action.INSERT.equals(form) && permissions.isAllowed(type, Action.INSERT_FORM)
				&& ((ref == null || permissions.isAllowedToMakeReference(type, id, ref))
						|| typeSettings.getFieldBoolean(type, ref.getReferencingField(), 
								KeyWords.SHOW_INSERT_FORM_BUTTON))) {
			
			elements.add(iconAnchor(strings.getActionName(type, Action.INSERT),
					url(type, lang, view) + formParameter(Action.INSERT) + refParameter, Icon.PLUS));
		}

		if (permissions.isAllowed(type, Action.SELECT) && (id != null || form != null
				|| component != null || request.isInfo() || request.isPreview() 
				|| request.isCalendar())) {

			String url = url(type, lang, view) + refParameter + searchParameter;

			elements.add(iconAnchor(strings.gts(type, KeyWords.LIST), url, Icon.LIST));
		}

		if (typeSettings.getTypeBoolean(type, KeyWords.SHOW_PREVIEW) && !request.isPreview()
				&& permissions.isAllowed(type,  Action.PREVIEW)) {
			
			String url = url(type, lang, view) + previewParameter() + searchParameter;
							
			elements.add(iconAnchor(strings.getActionName(type, Action.PREVIEW), url, Icon.LIST_RICH));
		}

		if (id == null && !Action.ALTER.equals(form) && permissions.isAllowed(type, Action.ALTER_FORM)) {
			elements.add(iconAnchor(strings.getActionName(type, Action.ALTER),
					url(type, lang, view) + formParameter(Action.ALTER), Icon.PENCIL));
		}

		if (id != null && !Action.UPDATE.equals(form)
				&& permissions.isAllowed(type, id, Action.UPDATE_FORM)) {
			elements.add(iconAnchor(strings.getActionName(type, Action.UPDATE),
					url(type, id, lang, view) + formParameter(Action.UPDATE), Icon.PENCIL));
		}

		if (!request.isInfo() && permissions.isAllowed(type, Action.GET_TYPE)) {
			elements.add(iconAnchor(strings.gts(type, KeyWords.TYPE), url(type, lang, view)
					+ parameter(KeyWords.INFO), Icon.INFO));
		}

		String rssSelect = typeSettings.gts(type, Constants.RSS_SELECT);

		if (rssSelect != null) {
			elements.add(rssIconAnchor(type, lang, ref));
		}

		String icalendarSelect = typeSettings.gts(type, Constants.ICALENDAR_SELECT);

		if (icalendarSelect != null) {
			elements.add(icalendarIconAnchor(type, lang, ref));
		}

		if (!request.isCalendar() && permissions.isAllowed(type, Action.CALENDAR)) {
			String calendarSelect = typeSettings.gts(type, Constants.CALENDAR_SELECT);

			if (calendarSelect != null) {
				String url = url(type, lang, view) + calendarParameter() + refParameter;

				elements.add(iconAnchor(strings.getActionName(type, Action.CALENDAR), url, Icon.CALENDAR));
			}
		}

		return elements.toArray(new Element[] {});
	}
	
	public Element rssIconAnchor(String type, String lang, FieldReference ref) {
		return iconAnchor(RSS, rssURL(type, lang, ref), Icon.RSS);
	}
	
	public String rssURL(String type, String lang, FieldReference ref) {
		return url(type, lang, Format.RSS.toString()) + refParameter(ref);
	}
	
	public Element icalendarIconAnchor(String type, String lang, FieldReference ref) {
		return iconAnchor(ICALENDAR, icalendarURL(type, lang, ref), Icon.FILE);
	}
	
	public String icalendarURL(String type, String lang, FieldReference ref) {
		return url(type, lang, Format.ICALENDAR.toString()) + refParameter(ref);
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

		Element input = group.appendInput(input(HTML.CHECKBOX, "@" + field, title, KeyWords.TRUE));

		if (value != null && (boolean) value) {
			input.setAttribute(HTML.CHECKED);
		}

		group.appendInput(input(HTML.HIDDEN, "@" + field, title, KeyWords.FALSE));

		return group;
	}

	public Element dates(String type, ZonedDateTime cdate, ZonedDateTime udate) {
		Element dates = document.createElement(HTML.DIV);
		Element creation = dates.appendElement(HTML.P);
		creation.addClass(KeyWords.DATE);
		creation.appendElement(HTML.STRONG).appendText(strings.gts(type, KeyWords.CREATION_DATE) + ": ");
		creation.appendElement(time(cdate));

		Element updating = dates.appendElement(HTML.P);
		updating.addClass(KeyWords.DATE);
		updating.appendElement(HTML.STRONG).appendText(strings.gts(type, KeyWords.UPDATING_DATE) + ": ");
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
			main.appendElement(HTML.P).addClass(KeyWords.MESSAGE).appendText(message);
			content = render(type);
			content.setStatus(HTTPStatus.NOT_FOUND);
		} else {
			content = new Content(message, Format.TEXT, HTTPStatus.NOT_FOUND);
		}

		return content;
	}

	@Override
	public Content unauthorized(String type, String lang, String view, UnauthorizedException e) {
		Content content = null;

		String message = e.getMessage(strings);

		if (request.getField() == null && request.getElement() == null) {

			if (document == null) {
				loadTemplate(type, lang, view);
			}

			document.getTitle().appendText(message);
			main.appendElement(HTML.P).addClass(KeyWords.MESSAGE).appendText(message);
			content = render(type);
			content.setStatus(HTTPStatus.UNAUTHORIZED);
		} else {
			content = new Content(message, Format.TEXT, HTTPStatus.UNAUTHORIZED);
		}

		return content;
	}

	public String url(String lang, String view) {
		return url(null, null, null, lang, view);
	}

	public String url(String type, String lang, String view) {
		return url(type, null, null, lang, view);
	}

	public String url(String type, String id, String lang, String view) {
		return url(type, id, null, lang, view);
	}

	public String url(String type, String id, String field, String lang, String view) {
		String typeParameter = type != null ? type : "";
		String idParameter = id != null ? "/" + id : "";
		String fieldParameter = field != null ? "/" + field : "";

		return hrefURL("/" + typeParameter + idParameter + fieldParameter, lang, view);
	}

	public String hrefURL(String href, String lang, String view) {
		try {
			URIBuilder url = new URIBuilder(href);

			if (lang != null) {
				url.addParameter(KeyWords.LANG, lang);
			}

			if (view != null) {
				url.addParameter(KeyWords.VIEW, view);
			}

			return url.toString();
		} catch (URISyntaxException e) {
			throw new NXException(e);
		}
	}
	
	public String imageURL(Tuple image) {
		return "/" + image.getString(KeyWords.IMAGE_TYPE) + "/" 
				+ image.getString(KeyWords.IMAGE_ID) + "/" + KeyWords.IMAGE;
	}

	public Content render() {
		return render(null);
	}

	public Content render(String type) {
		images();

		Content content = new Content(document.toString(), Format.XHTML);
		content.setHeader(HTTPHeader.CONTENT_SECURITY_POLICY,
				typeSettings.gts(type, KeyWords.CONTENT_SECURITY_POLICY));
		content.setHeader(HTTPHeader.REFERRER_POLICY,
				typeSettings.gts(type, KeyWords.REFERRER_POLICY));

		return content;
	}

	public void close() {
		if (nextNode != null) {
			nextNode.close();
		}
	}

	protected HTMLView getHTMLView(String type, String view) {
		if (type == null) {
			throw new NXException(KeyWords.EMPTY_TYPE_NAME);
		}

		HTMLView htmlView = null;

		String className = typeSettings.getView(type, view);

		if (className != null) {
			htmlView = Loader.loadHTMLView(className, type, this);
		} else {
			htmlView = this;
		}

		return htmlView;
	}

	public void head(String type, String lang, String view) {
		if (head != null) {
			head.appendElement(HTML.META).setAttribute(HTML.CHARSET, Constants.UTF_8_CHARSET);

			head.appendElement(HTML.META).setAttribute(HTML.NAME, HTML.VIEWPORT)
				.setAttribute(HTML.CONTENT, "width=device-width, initial-scale=1");

			String description = strings.gts(type, KeyWords.DESCRIPTION);
			if (description != null) {
				head.appendElement(HTML.META).setAttribute(HTML.NAME, HTML.DESCRIPTION)
					.setAttribute(HTML.CONTENT, description);
			}

			head.appendElement(HTML.SCRIPT).setAttribute(HTML.SRC, "/static/javascript/nexttypes.js");

			head.appendElement(HTML.LINK).setAttribute(HTML.REL, HTML.STYLESHEET)
				.setAttribute(HTML.TYPE, "text/css")
				.setAttribute(HTML.HREF, typeSettings.gts(type, KeyWords.STYLE));

			head.appendElement(HTML.LINK).setAttribute(HTML.REL, HTML.SHORTCUT_ICON)
				.setAttribute(HTML.HREF, "/static/images/logo.ico");
		}
	}

	public void logo(String type, String lang, String view) {
		Element logo = document.getElementById(KeyWords.LOGO);
		if (logo != null) {
			logo.appendElement(logoAnchor(type, lang, view));
		}
	}

	public void menu(String type, String lang, String view) {
		Element menuElement = document.getElementById(KeyWords.MENU);

		if (menuElement != null) {
			
			String file = typeSettings.gts(type, KeyWords.MENU);
			
			if (file != null) {
				Menu menu = context.getMenu(typeSettings.gts(type, KeyWords.MENU));

				for (MenuSection section : menu.getSections()) {
					menuElement.appendElement(menuTitle(type, section.getTitle()));

					Element ul = menuElement.appendElement(HTML.UL);

					for (Anchor anchor : section.getAnchors()) {
						ul.appendElement(menuListItem(type, anchor.getText(), anchor.getHref(), lang, view));
					}
				}
			}
			
			if (typeSettings.getTypeBoolean(type, KeyWords.SHOW_CONTROL_PANEL)) {
				menuElement.appendElement(controlPanel(type, lang, view));
			}
		}
	}
	
	public Element controlPanel(String type, String lang, String view) {
		Element section = document.createElement(HTML.SPAN).addClass(CONTROL_PANEL);
	
		section.appendElement(menuTitle(type, KeyWords.CONTROL_PANEL));
		
		Element ul = section.appendElement(HTML.UL);
		
		if (permissions.isAllowed(type, Action.LOGIN_FORM)) {
			ul.appendElement(menuListItem(type, Action.LOGIN, "/?form=login", lang, view));
		}
		
		if (permissions.isAllowed(type, Action.GET_TYPES_INFO)) {
			ul.appendElement(menuListItem(type, KeyWords.TYPES, "/?info", lang, view));
		}
		
		if (permissions.isAllowed(type, Action.CREATE_FORM)) {
			ul.appendElement(menuListItem(type, KeyWords.CREATE_TYPE, "/?form=create", lang, view));
		}
		
		if (permissions.isAllowed(type, Action.IMPORT_TYPES_FORM)) {
			ul.appendElement(menuListItem(type, Action.IMPORT_TYPES, "/?form=import_types", lang, view));
		}
		
		if (permissions.isAllowed(type, Action.IMPORT_OBJECTS_FORM)) {
			ul.appendElement(menuListItem(type, Action.IMPORT_OBJECTS, "/?form=import_objects", lang, view));
		}
		
		if (permissions.isAllowed(type, Action.GET_REFERENCES)) {
			ul.appendElement(menuListItem(type, KeyWords.REFERENCES, "/?references", lang, view));
		}
		
		return section;
	}
	
	public Element menuTitle(String type, String title) {
		return document.createElement(HTML.DIV).addClass(MENU_TITLE)
				.appendText(strings.gts(type, title) + ":");
	}
	
	public Element menuListItem(String type, String text, String href, String lang, String view) {
		Element li = document.createElement(HTML.LI);
		li.appendElement(anchor(strings.gts(type, text), hrefURL(href, lang, view)));
		return li;
	}

	public void langs(String lang) {
		Element langsElement = document.getElementById(KeyWords.LANGS);

		if (langsElement != null) {
			Tuple langs = settings.getTuple(KeyWords.LANGS);

			if (langs != null) {
				Element select = langsElement.appendElement(HTML.SELECT).addClass(KeyWords.LANGS);

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

		Element userElement = document.getElementById(KeyWords.USER);

		if (userElement != null) {
			String user = auth.getUser();

			if (auth.isGuest()) {
				userElement.addClass(HTML.HIDDEN);
			}

			Element form = userElement.appendElement(form(lang, view));

			form.appendText(strings.gts(type, KeyWords.USER) + ": ");

			Element userName = form.appendElement(HTML.SPAN).setId(USER_NAME);
			userName.appendText(user);

			Element button = button(strings.gts(type, KeyWords.LOGOUT), Action.LOGOUT, Icon.ACCOUNT_LOGOUT,
					SUBMIT_FORM).setId(LOGOUT_BUTTON);
			
			form.appendElement(button);

			if (!auth.isLoginUser()) {
				button.addClass(HTML.HIDDEN);
			}
		}
	}

	public void search(String type, String lang, String view, FieldReference ref, String search,
			LinkedHashMap<String, Order> order, Long offset, Long limit) {
		if (type != null) {
			Element form = document.getElementById(KeyWords.SEARCH)
					.setAttribute(HTML.AUTOCOMPLETE, HTML.OFF);

			if (form != null) {
				form.setAttribute(HTML.ACTION, "/" + type).setAttribute(HTML.METHOD, HTML.GET);

				String searchName = strings.gts(type, KeyWords.SEARCH);
				
				form.appendElement(input(HTML.HIDDEN, KeyWords.LANG, KeyWords.LANG, lang));
				form.appendElement(input(HTML.HIDDEN, KeyWords.VIEW, KeyWords.VIEW, view));
				form.appendElement(input(HTML.SEARCH, KeyWords.SEARCH, searchName, search));
				
				if (request.isPreview()) {
					form.appendElement(input(HTML.HIDDEN, Action.PREVIEW, Action.PREVIEW, Action.PREVIEW));
				}
				
				if (ref != null) {
					form.appendElement(input(HTML.HIDDEN, KeyWords.REF, KeyWords.REF, refString(ref)));
				}
				
				if (order != null) {
					form.appendElement(input(HTML.HIDDEN, KeyWords.ORDER, KeyWords.ORDER,
							orderString(order)));
				}
				
				if (offset != null) {
					form.appendElement(input(HTML.HIDDEN, KeyWords.OFFSET, KeyWords.OFFSET, offset));
				}
				
				if (limit != null) {
					form.appendElement(input(HTML.HIDDEN, KeyWords.LIMIT, KeyWords.LIMIT, limit));
				}

				form.appendElement(submitButton(searchName));
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
		Element actionsElement = document.getElementById(KeyWords.ACTIONS);

		if (actionsElement != null) {
			String form = request.getForm();

			if (permissions.isAllowed(type, id, Action.EXECUTE_ACTION_FORM) && type != null
					&& !Action.INSERT.equals(form) && !Action.ALTER.equals(form)
					&& !Action.RENAME.equals(form) && !request.isInfo()) {

				LinkedHashMap<String, LinkedHashMap<String, TypeField>> actions = nextNode.getTypeActions(type);
								
				if (actions != null && actions.size() > 0) {
					String requestAction = request.getAction();

					if (requestAction != null) {
						actions.remove(requestAction);
					}
					
					for (String action : actions.keySet()) {
						if (!permissions.isAllowed(type, id, action + "_" + KeyWords.FORM)) {
							actions.remove(action);
						}
					}

					if (actions.size() > 0) {

						for (Map.Entry<String, LinkedHashMap<String, TypeField>> entry : actions.entrySet()) {
							String action = entry.getKey();

							String actionName = strings.getActionName(type, action);

							String actionParameters = formParameter(Action.EXECUTE_ACTION)
									+ parameter(KeyWords.TYPE_ACTION, action);

							actionsElement.appendElement(anchor(actionName, url(type, id, lang, view)
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
			Map<String, List<Element>> imagesById = Arrays.stream(images).collect(
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
				for (Element div : imagesById.get(tuple.getString(KeyWords.ID) + ":" + tuple.getString(KeyWords.LANGUAGE))) {
					Element container = div;

					Element image = document.createElement(HTML.IMG)
							.setAttribute(HTML.SRC, imageURL(tuple))
							.setAttribute(HTML.ALT, tuple.getString(HTML.ALT));

					String link = tuple.getString(KeyWords.LINK);
					if (link != null) {
						Element anchor = document.createElement(HTML.A).setAttribute(HTML.HREF, link);
						String title = tuple.getString(HTML.TITLE);
						if (title != null) {
							anchor.setAttribute(HTML.TITLE, title);
						}
						anchor.appendElement(image);
						image = anchor;
					}

					String description = tuple.getString(KeyWords.DESCRIPTION);
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
			Element qrcodeElement = document.getElementById(KeyWords.QRCODE);

			if (qrcodeElement != null) {
				String objectURL = request.getHost() + "/" + type + "/" + id;

				QRCode objectURLQrcode = new QRCode(objectURL, 80, ErrorCorrectionLevel.L);
				qrcodeElement.appendElement(image(objectURL, objectURLQrcode.getBase64()));
			}
		}
	}

	public void rss(String type, String lang) {
		String rssSelect = typeSettings.gts(type, Constants.RSS_SELECT);

		if (rssSelect != null && head != null) {
			head.appendElement(HTML.LINK).setAttribute(HTML.REL, HTML.ALTERNATE)
					.setAttribute(HTML.TYPE, Format.RSS.getContentType())
					.setAttribute(HTML.TITLE, RSS)
					.setAttribute(HTML.HREF, url(type, lang, Format.RSS.toString()));
		}
	}

	public void validators(String type) {
		Boolean showValidators = typeSettings.getTypeBoolean(type, KeyWords.SHOW_VALIDATORS);

		if (showValidators) {

			Element validators = document.getElementById(VALIDATORS);

			if (validators != null) {
				validators.appendElement(imageAnchor(HTML5,
						"https://validator.w3.org/check?uri=referer", "/static/images/html5.png"));
				validators.appendElement(imageAnchor(CSS,
						"https://jigsaw.w3.org/css-validator/check/referer", "/static/images/css.gif"));
				validators.appendElement(imageAnchor(WCAG,
						"https://achecker.ca/checker/index.php?uri=referer&gid=WCAG2-AAA", "/static/images/wcag.jpeg"));
			}
		}
	}

	public void footer(String type) {

		if (footer != null) {
			footer.appendFragment(strings.gts(type, KeyWords.FOOTER));
		}
	}

	@Override
	public Content calendar(String type, String lang, String view, FieldReference ref, Year year,
			Month month) {
		loadTemplate(type, lang, view);

		String title = strings.gts(type, KeyWords.CALENDAR_TITLE);
		String typeName = strings.getTypeName(type);
		setTitle(Utils.format(title, typeName));

		ZoneId timeZone = nextNode.getTimeZone("select time_zone from \"user\" where id=?",
				auth.getUser());
		
		LocalDate today = timeZone != null ? LocalDate.now(timeZone) : LocalDate.now();

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
			parameters.add(ref.getReferencingField());
			parameters.add(ref.getReferencedId());
		}

		Tuple[] events = nextNode.select(type, sql, parameters, filters.toString(), "date, start_time");

		main.appendElement(calendar(type, lang, view, ref, month, today, date, events, firstDate));

		return render(type);
	}

	public Element calendar(String type, String lang, String view, FieldReference ref, Month month,
			LocalDate today, LocalDate date, Tuple[] events, LocalDate firstDate) {

		Element calendar = document.createElement(HTML.DIV).addClass(CALENDAR);

		Map<LocalDate, List<Tuple>> eventsByDate = Arrays.stream(events)
				.collect(Collectors.groupingBy(event -> event.getDate(KeyWords.DATE)));

		calendar.appendElement(dateSelect(type, lang, view, ref, date));
		calendar.appendElement(month(type, lang, view, month, today, firstDate, eventsByDate));

		return calendar;
	}

	public Element month(String type, String lang, String view, Month month, LocalDate today,
			LocalDate date, Map<LocalDate, List<Tuple>> eventsByDate) {

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

		navigator.appendElement(anchor("<<", url(type, lang, view) + calendarParameter()
			+ parameter(KeyWords.YEAR, before.getYear()) + parameter(KeyWords.MONTH, 
					before.getMonthValue()) + refParameter(ref)));

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

		navigator.appendElement(anchor(">>", url(type, lang, view) + calendarParameter()
			+ parameter(KeyWords.YEAR, after.getYear()) + parameter(KeyWords.MONTH, 
					after.getMonthValue()) + refParameter(ref)));

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
				.setAttribute(HTML.STYLE, HTML.BACKGROUND_COLOR + ": " + event.getColor(KeyWords.COLOR));

		div.appendElement(anchor(event.getTime(KeyWords.START_TIME) + " " + event.getString(KeyWords.SUMMARY),
				url(type, event.getString(KeyWords.ID), lang, view)));

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