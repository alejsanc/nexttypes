/*
 * Copyright 2015-2024 Alejandro Sánchez <alex@nexttypes.com>
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

package com.nexttypes.protocol.http;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.Month;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.nexttypes.datatypes.ActionReference;
import com.nexttypes.datatypes.Audio;
import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Document;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.File;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.Image;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.Type;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.datatypes.URL;
import com.nexttypes.datatypes.Video;
import com.nexttypes.enums.Comparison;
import com.nexttypes.enums.Component;
import com.nexttypes.enums.ImportAction;
import com.nexttypes.enums.IndexMode;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.FieldException;
import com.nexttypes.exceptions.IndexException;
import com.nexttypes.exceptions.InvalidValueException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.security.Checks;
import com.nexttypes.security.Security;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.LanguageSettings;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.system.Action;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Context;
import com.nexttypes.system.Utils;

public class HTTPRequest {

	protected static final String[] REQUEST_PARAMETERS = new String[] { KeyWords.TYPE, KeyWords.TYPES,
			KeyWords.ADATE, KeyWords.ID, KeyWords.UDATE, KeyWords.OBJECTS, KeyWords.NEW_ID,
			KeyWords.NEW_NAME, KeyWords.EXISTING_TYPES_ACTION, KeyWords.EXISTING_OBJECTS_ACTION,
			KeyWords.OFFSET, KeyWords.LIMIT, KeyWords.ORDER, KeyWords.SEARCH,
			KeyWords.CURRENT_PASSWORD, KeyWords.NEW_PASSWORD, KeyWords.NEW_PASSWORD_REPEAT,
			KeyWords.VIEW, KeyWords.REF, KeyWords.AREF, KeyWords.FORM, KeyWords.YEAR, KeyWords.MONTH,
			KeyWords.ACTION, KeyWords._ACTION, KeyWords.LOGIN_USER, KeyWords.LOGIN_PASSWORD, 
			KeyWords.COMPONENT, KeyWords.INCLUDE_OBJECTS, KeyWords.VERSION, KeyWords.INFO, KeyWords.NAMES,
			KeyWords.CALENDAR, KeyWords.PREVIEW, KeyWords.REFERENCES, Action.FILTER_COMPONENT};

	protected Settings settings;
	protected TypeSettings typeSettings;
	protected LanguageSettings languageSettings;
	protected HttpServletRequest request;
	protected URL url;
	protected Auth auth;
	protected HTTPMethod requestMethod;
	protected String remoteAddress;
	protected HttpSession session;
	protected String etag;
	protected Context context;
	protected boolean secure;

	protected String type;
	protected String id;
	protected String field;
	protected String element;
	protected String new_id;
	protected String new_name;
	protected String lang;
	protected String view;
	protected String[] types;
	protected String[] objects;
	protected FieldReference ref;
	protected ActionReference aref;
	protected String search;
	protected LinkedHashMap<String, Order> order;
	protected String action;
	protected String _action;
	protected Tuple parameters;
	protected Tuple fields;
	protected ZonedDateTime udate;
	protected ZonedDateTime adate;
	protected Year year;
	protected Month month;
	protected String form;
	protected Long offset;
	protected Long limit;
	protected ImportAction existing_types_action;
	protected ImportAction existing_objects_action;
	protected String current_password;
	protected String new_password;
	protected String new_password_repeat;
	protected String login_user;
	protected String login_password;
	protected Component component;
	protected Integer filter_component;
	protected boolean include_objects = false;
	protected boolean version = false;
	protected boolean info = false;
	protected boolean names = false;
	protected boolean references = false;
	protected boolean preview = false;
	protected boolean calendar = false;
	protected boolean default_parameter = false;

	protected LinkedHashMap<String, LinkedHashMap<String, HashMap<String, String>>> compositeParameters;

	public HTTPRequest(HttpServletRequest request, Settings settings, Context context, String lang, 
			LanguageSettings languageSettings, Auth auth, URL url) {
		this.request = request;
		this.settings = settings;
		this.context = context;
		this.languageSettings = languageSettings;
		this.lang = lang;
		this.auth = auth;
		this.url = url;

		remoteAddress = request.getRemoteAddr();
		session = request.getSession();
		secure = request.isSecure();

		requestMethod = HTTPMethod.valueOf(request.getMethod());

		typeSettings = context.getTypeSettings(auth);

		if (HTTPMethod.GET.equals(requestMethod)) {
			etag = request.getHeader(HTTPHeader.IF_NONE_MATCH.toString());
		}

		setSession();

		fields = new HTTPRequestParameters();
		parameters = new HTTPRequestParameters();
		compositeParameters = new LinkedHashMap<>();

		readParameters();

		if (view == null || !ArrayUtils.contains(settings.getStringArray(KeyWords.VIEWS), view)) {
			view = settings.getString(KeyWords.DEFAULT_VIEW);
		}

		if (limit != null && limit == 0) {
			limit = null;
		}

		parseURLPath();
		
		if (objects != null && objects.length == 1 && objects[0].contains("\n")) {
			objects = objects[0].split("\\r\\n|\\n");
		}

		Checks.checkType(type);
		Checks.checkId(id);
		Checks.checkField(field);
		Checks.checkElement(element);
		Checks.checkId(new_id);
		Checks.checkView(view);
		Checks.checkLang(lang);
		Checks.checkObjects(objects);
		Checks.checkTypes(types);
		Checks.checkRef(ref);
		Checks.checkARef(aref);
		Checks.checkOrder(order);
		Checks.checkAction(action);
		Checks.checkAction(_action);
		Checks.checkParameters(parameters);
		Checks.checkTuple(fields);
	}

	protected void setSession() {
		if (secure) {

			if (requestMethod.equals(HTTPMethod.GET)) {
				setSessionToken();
			} else if (requestMethod.equals(HTTPMethod.POST)) {
				String token = getSessionToken();

				if (token == null) {
					throw new NXException(type, KeyWords.SESSION_EXPIRED);
				}

				String requestToken = request.getParameter(KeyWords.SESSION);

				if (requestToken == null) {
					throw new NXException(type, KeyWords.SESSION_PARAMETER_NOT_FOUND);
				}

				if (!requestToken.equals(token)) {
					throw new NXException(type, KeyWords.INVALID_SESSION);
				}
			}
		}
	}

	protected void setSessionToken() {
		if (session.getAttribute(KeyWords.SESSION) == null) {
			session.setAttribute(KeyWords.SESSION, Security.randomString());
		}
	}

	public String getSessionToken() {
		return (String) session.getAttribute(KeyWords.SESSION);
	}

	protected void parseURLPath() {
		String path = url.getPath();

		String[] parameters = path.substring(1, path.length()).split("/");

		if (parameters.length > 0 && parameters[0].length() > 0) {
			type = parameters[0];
			if (parameters.length > 1) {
				id = parameters[1];
				if (parameters.length > 2) {
					field = parameters[2];
					if (parameters.length > 3) {
						element = parameters[3];
					}
				}
			}
		}
	}

	protected void readParameters() {
		Enumeration<String> parameterNames = request.getParameterNames();
		
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			
			if (KeyWords.DEFAULT.equals(parameterName)) {
				default_parameter = true;
			} else if (ArrayUtils.contains(REQUEST_PARAMETERS, parameterName)) {
				try {
					Field field = getClass().getDeclaredField(parameterName);
					Class fieldType = field.getType();
					Object value = null;

					if (fieldType == String[].class) {
						value = Utils.trim(request.getParameterValues(parameterName));
					} else if (fieldType == boolean.class) {
						value = true;
					} else {
						String tmp = Utils.trim(request.getParameter(parameterName));
						if (tmp != null) {
							if (fieldType == String.class) {
								value = tmp;
							} else if (fieldType == Integer.class) {
								value = Integer.parseInt(tmp);
							} else if (fieldType == Long.class) {
								value = Long.parseLong(tmp);
							} else if (fieldType == Month.class) {
								value = Month.of(Integer.parseInt(tmp));
							} else if (fieldType == Year.class) {
								value = Year.of(Integer.parseInt(tmp));
							} else if (fieldType.isEnum()) {
								value = Enum.valueOf(fieldType, tmp.toUpperCase());
							} else if (fieldType == FieldReference.class) {
								String[] parts = tmp.split(":");
								value = new FieldReference(parts[0], null, parts[1]);
							} else if (fieldType == ActionReference.class) {
								String[] parts = tmp.split(":");
								value = new ActionReference(parts[0], parts[1], parts[2]);
							} else if (fieldType == ZonedDateTime.class) {
								value = ZonedDateTime.parse(tmp);
							} else if (parameterName.equals(KeyWords.ORDER)) {
								value = Utils.parserOrderString(tmp);
							}
						}
					}

					field.set(this, value);
				} catch (NoSuchFieldException | IllegalAccessException e) {
					throw new NXException(e);
				}

			} else {
				if (parameterName.contains(":")) {
					parseCompositeParameter(parameterName);
				} else {
					String[] parameterValues = request.getParameterValues(parameterName);
					if (parameterName.startsWith("@")) {
						fields.put(parameterName.substring(1), parameterValues);
					} else {
						parameters.put(parameterName, parameterValues);
					}
				}
			}
		}
	}

	protected void parseCompositeParameter(String parameterName) {
		String value = Utils.trim(request.getParameter(parameterName));
								
		String[] parameter = parameterName.split(":");

		LinkedHashMap<String, HashMap<String, String>> name = compositeParameters.get(parameter[0]);
		if (name == null) {
			name = new LinkedHashMap<>();
			compositeParameters.put(parameter[0], name);
		}

		HashMap<String, String> number = name.get(parameter[1]);
		if (number == null) {
			number = new HashMap<>();
			name.put(parameter[1], number);
		}

		number.put(parameter[2], value);
	}
	
	public void checkFields(LinkedHashMap<String, TypeField> typeFields) {
		for (String field : fields.getFields().keySet()) {
			if (!typeFields.containsKey(field)) {
				if (!((field.endsWith("_" + KeyWords.REPEAT) || field.endsWith("_" + KeyWords.NULL))
						&& typeFields.containsKey(field.substring(0, field.lastIndexOf("_"))))) {
					
					throw new FieldException(type, field, KeyWords.INVALID_FIELD);
				}
			}
		}
	}
	
	public NXObject readObject(LinkedHashMap<String, TypeField> typeFields) {
		
		checkFields(typeFields);
		
		NXObject object = new NXObject(type, id);

		for (Map.Entry<String, TypeField> entry : typeFields.entrySet()) {

			String field = entry.getKey();
			TypeField typeField = entry.getValue();
			String fieldType = typeField.getType();

			if (PT.isBinaryType(fieldType)) {
				
				if (fields.containsKey(field + "_" + KeyWords.NULL)) {
					
					object.put(field, null);
					
				} else {

					Object value = readField(field, fieldType);

					if (value != null) {
						object.put(field, value);
					}
				}

			} else if (fields.containsKey(field)) {

				if (PT.PASSWORD.equals(fieldType) && Action.UPDATE.equals(action)) {
					throw new FieldException(type, field, KeyWords.PASSWORD_FIELD_UPDATE);
				}

				object.put(field, readField(field, fieldType));
			}
		}

		return object;
	}

	public InputStream getDataStream() {
		try {
			InputStream stream = null;

			Part part = request.getPart(KeyWords.DATA);
			if (part != null && part.getSize() > 0) {
				stream = part.getInputStream();
			}

			return stream;
		} catch (IOException | ServletException e) {
			throw new NXException(e);
		}
	}

	public Object[] readActionFields(LinkedHashMap<String, TypeField> typeFields) {
		ArrayList<Object> values = new ArrayList<>();

		for (Map.Entry<String, TypeField> entry : typeFields.entrySet()) {
			values.add(readField(entry.getKey(), entry.getValue().getType()));
		}

		return values.toArray();
	}

	protected Object readField(String field, String fieldType) {
		Object value = null;

		switch (fieldType) {
		case PT.HTML:
			value = fields.getHTML(field, lang, typeSettings.getFieldString(type, field,
					KeyWords.HTML_ALLOWED_TAGS));
			break;
		case PT.XML:
			value = fields.getXML(field, lang, typeSettings.getFieldString(type, field,
					KeyWords.XML_ALLOWED_TAGS));
			break;
		case PT.JSON:
			value = fields.getJSON(field);
			break;
		case PT.INT16:
			value = fields.getInt16(field);
			break;
		case PT.INT32:
			value = fields.getInt32(field);
			break;
		case PT.INT64:
			value = fields.getInt64(field);
			break;
		case PT.FLOAT32:
			value = fields.getFloat32(field);
			break;
		case PT.FLOAT64:
			value = fields.getFloat64(field);
			break;
		case PT.NUMERIC:
			value = fields.getNumeric(field);
			break;
		case PT.BOOLEAN:
			value = fields.getBoolean(field);
			break;
		case PT.DATE:
			value = fields.getDate(field);
			break;
		case PT.TIME:
			value = fields.getTime(field);
			break;
		case PT.DATETIME:
			value = fields.getDateTime(field);
			break;
		case PT.TIMEZONE:
			value = fields.getTimeZone(field);
			break;
		case PT.COLOR:
			value = fields.getColor(field);
			break;
		case PT.URL:
			value = fields.getURL(field);
			break;
		case PT.EMAIL:
			value = fields.getEmail(field);
			break;
		case PT.BINARY:
			value = fields.getBinary(field);
			break;
		case PT.FILE:
			value = fields.getFile(field);
			break;
		case PT.IMAGE:
			value = fields.getImage(field);
			break;
		case PT.DOCUMENT:
			value = fields.getDocument(field);
			break;
		case PT.AUDIO:
			value = fields.getAudio(field);
			break;
		case PT.VIDEO:
			value = fields.getVideo(field);
			break;
		case PT.PASSWORD:
			value = fields.getPassword(field);
			break;
		default:
			value = fields.getString(field);
		}

		return value;
	}
	
	public Filter[] readFilters(LinkedHashMap<String, TypeField> typeFields) {
		ArrayList<Filter> filters = new ArrayList<>();
		
		LinkedHashMap<String, HashMap<String, String>> filtersParameters = 
				compositeParameters.get(KeyWords.FILTERS);
		
		if (filtersParameters != null) {
		
			for(Map.Entry<String, HashMap<String, String>> entry : filtersParameters.entrySet()) {
			
				HashMap<String, String> filterParameters = entry.getValue();
			
				String field = filterParameters.get(KeyWords.FIELD);
				Comparison comparison = Comparison.valueOf(
						filterParameters.get(KeyWords.COMPARISON).toUpperCase());
			
				Object value = filterParameters.get(KeyWords.VALUE);
				
				if (!Comparison.LIKE.equals(comparison) && !Comparison.NOT_LIKE.equals(comparison)
						&& !KeyWords.ID.equals(field)) {
					
					String fieldType = typeFields.get(field).getType();
				
					if (PT.isPrimitiveType(fieldType) && !PT.isFilterType(fieldType)) {
						throw new InvalidValueException(KeyWords.INVALID_FILTER_TYPE, fieldType);
					}
				
					switch(fieldType) {
					case PT.INT16:
						value = Tuple.parseInt16(value);
						break;
					
					case PT.INT32:
						value = Tuple.parseInt32(value);
						break;
					
					case PT.INT64:
						value = Tuple.parseInt64(value);
						break;
					
					case PT.FLOAT32:
						value = Tuple.parseFloat32(value);
						break;
					
					case PT.FLOAT64:
						value = Tuple.parseFloat64(value);
						break;
					
					case PT.NUMERIC:
						value = Tuple.parseNumeric(value);
						break;
					
					case PT.BOOLEAN:
						value = Tuple.parseBoolean(value);
						break;
					
					case PT.COLOR:
						value = Tuple.parseColor(value);
						break;
					
					case PT.DATE:
						value = Tuple.parseDate(value);
						break;
					
					case PT.TIME:
						value = Tuple.parseTime(value);
						break;
					
					case PT.TIMEZONE:
						value = Tuple.parseTimeZone(value);
						break;
					
					case PT.DATETIME:
						value = Tuple.parseDateTime(value);
						break;
					
					case PT.EMAIL:
						value = Tuple.parseEmail(value);
						break;
					
					case PT.URL:
						value = Tuple.parseURL(value);
						break;					
					}
				}
			
				filters.add(new Filter(field, comparison, value, true));
			}
		}
		
		return filters.toArray(new Filter[] {});
	}

	public Type readType() {
		Type typeObject = new Type(type);
		LinkedHashMap<String, TypeField> typeObjectFields = typeObject.getFields();
		LinkedHashMap<String, TypeIndex> typeObjectIndexes = typeObject.getIndexes();

		LinkedHashMap<String, HashMap<String, String>> fields = compositeParameters.get(KeyWords.FIELDS);

		if (fields != null) {
			for (Map.Entry<String, HashMap<String, String>> entry : fields.entrySet()) {

				HashMap<String, String> value = entry.getValue();

				String fieldName = value.get(KeyWords.NAME);

				if (typeObjectFields.containsKey(fieldName)) {
					throw new FieldException(type, fieldName, KeyWords.DUPLICATE_FIELD);
				}

				String fieldType = value.get(KeyWords.TYPE);
				String fieldParameters = value.get(KeyWords.PARAMETERS);
				boolean fieldNotNull = value.get(KeyWords.NOT_NULL) != null ? true : false;
				String fieldOldName = value.get(KeyWords.OLD_NAME);

				typeObjectFields.put(fieldName, new TypeField(fieldType, fieldParameters, fieldNotNull,
						fieldOldName));
			}
		}

		LinkedHashMap<String, HashMap<String, String>> indexes = compositeParameters.get(KeyWords.INDEXES);

		if (indexes != null) {
			for (Map.Entry<String, HashMap<String, String>> entry : indexes.entrySet()) {

				HashMap<String, String> value = entry.getValue();

				String indexName = value.get(KeyWords.NAME);

				if (typeObjectIndexes.containsKey(indexName)) {
					throw new IndexException(type, indexName, KeyWords.DUPLICATE_INDEX);
				}

				IndexMode indexMode = IndexMode.valueOf(value.get(KeyWords.MODE).toUpperCase());
				String[] indexFields = Utils.split(value.get(KeyWords.FIELDS));
				String indexOldName = value.get(KeyWords.OLD_NAME);

				typeObjectIndexes.put(indexName, new TypeIndex(indexMode, indexFields, indexOldName));
			}
		}

		return typeObject;
	}

	public URL getURL() {
		return url;
	}

	public String getURLRoot() {
		return url.getRoot();
	}

	public String getURLPath() {
		return url.getPath();
	}

	public String getHost() {
		return url.getHost();
	}

	public boolean isSitemap() {
		return url.isSitemap();
	}

	public boolean isRobots() {
		return url.isRobots();
	}

	public Auth getAuth() {
		return auth;
	}

	public HTTPMethod getRequestMethod() {
		return requestMethod;
	}

	public HttpServletRequest getServletRequest() {
		return request;
	}

	public String getForm() {
		return form;
	}

	public Long getOffset() {
		return offset;
	}

	public Long getLimit() {
		return limit;
	}

	public Component getComponent() {
		return component;
	}
	
	public Integer getFilterComponent() {
		return filter_component;
	}

	public boolean includeObjects() {
		return include_objects;
	}
	
	public boolean isVersion() {
		return version;
	}

	public boolean isInfo() {
		return info;
	}

	public boolean isNames() {
		return names;
	}

	public boolean isReferences() {
		return references;
	}

	public boolean isPreview() {
		return preview;
	}

	public boolean isCalendar() {
		return calendar;
	}
	
	public boolean isDefault() {
		return default_parameter;
	}

	public ImportAction getExistingTypesAction() {
		return existing_types_action;
	}

	public ImportAction getExistingObjectsAction() {
		return existing_objects_action;
	}

	public String getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public String getNewId() {
		return new_id;
	}

	public String getNewName() {
		return new_name;
	}

	public String getField() {
		return field;
	}

	public String getElement() {
		return element;
	}

	public String getLang() {
		return lang;
	}
	
	public void setView(String view) {
		this.view = view;
	}
	
	public String getView() {
		return view;
	}

	public FieldReference getRef() {
		return ref;
	}
	
	public ActionReference getARef() {
		return aref;
	}

	public String getSearch() {
		return search;
	}

	public LinkedHashMap<String, Order> getOrder() {
		return order;
	}

	public String[] getObjects() {
		return objects;
	}

	public String[] getTypes() {
		return types;
	}

	public String getAction() {
		return action != null ? action : _action ;
	}

	public Tuple getParameters() {
		return parameters;
	}

	public ZonedDateTime getADate() {
		return adate;
	}

	public ZonedDateTime getUDate() {
		return udate;
	}

	public Month getMonth() {
		return month;
	}

	public Year getYear() {
		return year;
	}

	public TypeSettings getTypeSettings() {
		return typeSettings;
	}

	public LanguageSettings getLanguageSettings() {
		return languageSettings;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public String getUserAgent() {
		return request.getHeader(HTTPHeader.USER_AGENT.toString());
	}

	public HttpSession getSession() {
		return session;
	}

	public Context getContext() {
		return context;
	}

	public boolean isSecure() {
		return secure;
	}

	public String getETag() {
		return etag;
	}

	public String getCurrentPassword() {
		return current_password;
	}

	public String getNewPassword() {
		return new_password;
	}

	public String getNewPasswordRepeat() {
		return new_password_repeat;
	}

	public String getLoginUser() {
		return login_user;
	}

	public String getLoginPassword() {
		return login_password;
	}

	protected class HTTPRequestParameters extends Tuple {
		
		@Override
		public Object get(String field) {
			String value = null;
			String[] values = (String[]) fields.get(field);
			
			if (values != null && values.length > 0) {
				value = Utils.trim(values[0]);
			} 
			
			return value;
		}
		
		@Override
		public byte[] getBinary(String field) {
			byte[] value = null;
			
			try {
				Part part = request.getPart("@" + field);
				if (part != null && part.getSize() > 0) {
					value = IOUtils.toByteArray(part.getInputStream());
				}
			} catch (IOException | ServletException e) {
				throw new NXException(e);
			}
			
			return value;
		}

		@Override
		public File getFile(String field) {
			File value = null;

			try {
				Part part = request.getPart("@" + field);
				if (part != null && part.getSize() > 0) {
					value = new File(part.getSubmittedFileName(), IOUtils.toByteArray(part.getInputStream()));
				}
			} catch (IOException | ServletException e) {
				throw new NXException(e);
			}

			return value;
		}

		@Override
		public Image getImage(String field) {
			Image value = null;

			File file = getFile(field);

			if (file != null) {
				value = new Image(file);
			}

			return value;
		}

		@Override
		public Document getDocument(String field) {
			Document value = null;

			File file = getFile(field);

			if (file != null) {
				value = new Document(file);
			}

			return value;
		}

		@Override
		public Audio getAudio(String field) {
			Audio value = null;

			File file = getFile(field);

			if (file != null) {
				value = new Audio(file);
			}

			return value;
		}

		@Override
		public Video getVideo(String field) {
			Video value = null;

			File file = getFile(field);

			if (file != null) {
				value = new Video(file);
			}

			return value;
		}

		@Override
		public String getPassword(String field) {

			String password = getString(field);
			String passwordRepeat = getString(field + "_" + KeyWords.REPEAT);

			if (!Security.passwordsMatch(password, passwordRepeat)) {
				throw new NXException(type, KeyWords.PASSWORDS_DONT_MATCH);
			}

			if (!Security.checkPasswordStrength(password)) {
				throw new NXException(type, KeyWords.INVALID_PASSWORD);
			}

			return Security.passwordHash(password);
		}
	}
}