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
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.client.utils.URIBuilder;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.Message;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.ObjectInfo;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.RenameResult;
import com.nexttypes.datatypes.Serial;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.URL;
import com.nexttypes.datatypes.UpdateIdResult;
import com.nexttypes.datatypes.UpdateResult;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.NodeMode;
import com.nexttypes.exceptions.ActionNotFoundException;
import com.nexttypes.exceptions.CertificateNotFoundException;
import com.nexttypes.exceptions.FieldException;
import com.nexttypes.exceptions.InvalidHostNameException;
import com.nexttypes.exceptions.InvalidUserOrPasswordException;
import com.nexttypes.exceptions.MethodNotAllowedException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.exceptions.NotFoundException;
import com.nexttypes.exceptions.UnauthorizedException;
import com.nexttypes.exceptions.ViewNotFoundException;
import com.nexttypes.interfaces.Stream;
import com.nexttypes.logging.Logger;
import com.nexttypes.nodes.Node;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.settings.LanguageSettings;
import com.nexttypes.system.Action;
import com.nexttypes.system.ClamAV;
import com.nexttypes.system.Constants;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Context;
import com.nexttypes.system.Debug;
import com.nexttypes.system.Loader;
import com.nexttypes.system.Utils;
import com.nexttypes.views.View;
import com.nexttypes.views.WebDAVView;

public class HTTPServlet extends HttpServlet {
	protected static final long serialVersionUID = 1L;
	public static final String MAX_REQUESTS = "429 Error: Requests per minute exceeded.";
	public static final String X509_CERTIFICATES = "javax.servlet.request.X509Certificate";
	
	protected Settings settings;
	protected Context context;
	protected Logger logger;
	protected int maxRequests;
	protected int maxAuthErrors;
	protected boolean debug;
	protected boolean binaryDebug;
	protected int binaryDebugLimit;
	protected ClamAV antivirus;
	protected ConcurrentHashMap<String, Requests> requestsMap = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, Requests> authErrorsMap = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, ConcurrentHashMap<String, Requests>> insertRequestsMap
		= new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, Requests>[] requestsMaps = new ConcurrentHashMap[] { requestsMap,
			authErrorsMap };

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		context = Context.get(getServletContext());
		settings = context.getSettings(Settings.HTTP_SETTINGS);
		maxRequests = settings.getInt32(KeyWords.MAX_REQUESTS);
		maxAuthErrors = settings.getInt32(KeyWords.MAX_AUTH_ERRORS);
		debug = settings.getBoolean(KeyWords.DEBUG);
		binaryDebug = settings.getBoolean(KeyWords.BINARY_DEBUG);
		binaryDebugLimit = settings.getInt32(KeyWords.BINARY_DEBUG_LIMIT);
		logger = context.getLogger();
		antivirus = new ClamAV(context);
				
		purgeRequestsMapsThread();
	}
	
	protected void purgeRequestsMapsThread() {
		
		new Thread () {
			
			protected void purge (long now, ConcurrentHashMap<String, Requests> requestsMap, 
					Map.Entry<String, Requests> entry) {
				if (now - entry.getValue().firstRequestTime > Constants.MINUTE_MILLISECONDS) {
					requestsMap.remove(entry.getKey());
				}
			}
			
			public void run() {
				while (true) {
					try {
						sleep(Constants.MINUTE_MILLISECONDS);
					} catch (InterruptedException e) {
						throw new NXException(e);
					}
					
					long now = System.currentTimeMillis();
					
					for (ConcurrentHashMap<String, Requests> requestsMap : requestsMaps) {
						for (Map.Entry<String, Requests> entry : requestsMap.entrySet()) {
							purge(now, requestsMap, entry);
						}
					}
					
					for (Map.Entry<String, ConcurrentHashMap<String, Requests>> entry
							: insertRequestsMap.entrySet()) {
						
						ConcurrentHashMap<String, Requests> typesRequests = entry.getValue();
						
						for (Map.Entry<String, Requests> typeEntry : typesRequests.entrySet()) {
							purge(now, typesRequests, typeEntry);
						}
						
						if (typesRequests.size() == 0) {
							insertRequestsMap.remove(entry.getKey());
						}
					}
				}
			}
		}.start();
	}

	protected Content get(HTTPRequest req, HttpServletResponse response) throws IOException,
		URISyntaxException {
		
		Content content = null;

		String type = req.getType();
		String id = req.getId();
		String form = req.getForm();

		try (View view = getView(req)) {
			try {
				if (type == null) {

					if (Action.CREATE.equals(form)) {
						content = view.createForm(req.getLang(), req.getView());
					} else if (Action.IMPORT_TYPES.equals(form)) {
						content = view.importTypesForm(req.getLang(), req.getView());
					} else if (Action.IMPORT_OBJECTS.equals(form)) {
						content = view.importObjectsForm(req.getLang(), req.getView());
					} else if (Action.LOGIN.equals(form)) {
						content = view.loginForm(req.getLang(), req.getView());
					} else {

						if (req.isVersion()) {
							content = view.getVersion();
						} else if (req.isInfo()) {
							content = view.getTypesInfo(req.getLang(), req.getView());
						} else if (req.isNames()) {
							content = view.getTypesName(req.getLang(), req.getView());
						} else if (req.isReferences()) {
							content = view.getReferences(req.getLang(), req.getView());
						} else {
							URIBuilder newURL = new URIBuilder(settings.getString(KeyWords.INDEX));
							newURL.setParameter(KeyWords.LANG, req.getLang());
							newURL.setParameter(KeyWords.VIEW, req.getView());
							content = new Content(HTTPStatus.FOUND);
							content.setHeader(HTTPHeader.LOCATION, newURL.toString());
						}
					}

				} else if (id == null) {
					
					LinkedHashMap<String, TypeField> typeFields = null;
					Integer filterCount = req.getFilterComponent();
					
					if (filterCount != null) {
						content = view.filterComponent(req.getType(), null, req.getLang(),
								req.getView(), filterCount);
					} else if (Action.INSERT.equals(form)) {
						content = view.insertForm(req.getType(), req.getLang(), req.getView(), req.getRef());
					} else if (Action.ALTER.equals(form)) {
						content = view.alterForm(req.getType(), req.getLang(), req.getView());
					} else if (Action.RENAME.equals(form)) {
						content = view.renameForm(req.getType(), req.getLang(), req.getView());
					} else if (Action.EXECUTE_ACTION.equals(form)) {
						content = view.executeActionForm(req.getType(), null, req.getAction(),
								req.getLang(), req.getView());
					} else if (req.isNames()) {
						content = view.getNames(req.getType(), req.getLang(), req.getView(),
								req.getARef(), req.getSearch(), req.getOffset());
					} else if (req.isInfo()) {
						content = view.getType(req.getType(), req.getLang(), req.getView());
					} else if (req.isPreview()) {
						typeFields = view.getNextNode().getTypeFields(req.getType());
						content = view.preview(req.getType(), req.getLang(), req.getView(),
							req.getRef(), req.readFilters(typeFields), req.getSearch(),
							req.getOrder(), req.getOffset(), req.getLimit());
					} else if (req.isCalendar()) {
						content = view.calendar(req.getType(), req.getLang(), req.getView(),
							req.getRef(), req.getYear(), req.getMonth());
					} else if (req.getComponent() != null) {
						typeFields = view.getNextNode().getTypeFields(req.getType());
						content = view.selectComponent(req.getType(), req.getLang(), req.getView(),
							req.getRef(), req.readFilters(typeFields), req.getSearch(),
							req.getOrder(), req.getOffset(), req.getLimit(), req.getComponent());
					} else {
						typeFields = view.getNextNode().getTypeFields(req.getType());
						content = view.select(req.getType(), req.getLang(), req.getView(),
							req.getRef(), req.readFilters(typeFields), req.getSearch(),
							req.getOrder(), req.getOffset(), req.getLimit());
					}

				} else if (req.getField() == null) {
					
					if (Action.UPDATE.equals(form)) {
						content = view.updateForm(req.getType(), req.getId(), req.getLang(), req.getView());
					} else if (Action.UPDATE_ID.equals(form)) {
						content = view.updateIdForm(req.getType(), req.getId(), req.getLang(), req.getView());
					} else if (Action.EXECUTE_ACTION.equals(form)) {
						content = view.executeActionForm(req.getType(), req.getId(),
								req.getAction(), req.getLang(), req.getView());
					} else {
						content = view.get(req.getType(), req.getId(), req.getLang(), req.getView(), req.getETag());
					}

				} else if (req.getElement() == null) {
					Integer filterCount = req.getFilterComponent();
					if (filterCount != null) {
						content = view.filterComponent(req.getType(), req.getField(), req.getLang(),
								req.getView(), filterCount);
					} else if (Action.UPDATE_PASSWORD.equals(form)) {
						content = view.updatePasswordForm(req.getType(), req.getId(), req.getField(), req.getLang(),
								req.getView());
					} else if (req.isDefault()) {
						content = view.getFieldDefault(req.getType(), req.getField());
					} else {
						content = view.getField(req.getType(), req.getId(), req.getField(), req.getView(),
								req.getETag());
					}
				} else {

					content = view.getElement(req.getType(), req.getId(), req.getField(), req.getElement(),
							req.getLang(), req.getView(), req.getETag());
				}

			} catch (NotFoundException e) {
				logException(e, req.getAuth().getUser(), req.getRemoteAddress());
				content = view.notFound(req.getType(), req.getLang(), req.getView(), e);
			} catch (UnauthorizedException e) {
				logException(e, req.getAuth().getUser(), req.getRemoteAddress());
				content = view.unauthorized(req.getType(), req.getLang(), req.getView(), e);
			}
		}

		if (id == null || form != null) {
			response.setHeader("cache-control", "no-cache, no-store, must-revalidate");
			response.setHeader("pragma", "no-cache");
			response.setHeader("expires", "0");
		}

		return content;
	}

	protected void exportTypes(HTTPRequest req, Node nextNode, HttpServletResponse response,
			LanguageSettings languageSettings)
			throws IOException {

		try (Stream stream = nextNode.exportTypes(req.getTypes(), req.includeObjects())) {
			Content content = new Content(stream, Format.JSON);
			content.setHeader(HTTPHeader.CONTENT_DISPOSITION,
					"attachment; filename=\"types." + Format.JSON.getExtension() + "\"");

			writeContent(content, response);
		}
	}

	protected void exportObjects(HTTPRequest req, Node nextNode, HttpServletResponse response,
			LanguageSettings languageSettings)
			throws IOException {

		try (Stream stream = nextNode.exportObjects(req.getType(), req.getObjects(), req.getOrder())) {
			Content content = new Content(stream, Format.JSON);
			content.setHeader(HTTPHeader.CONTENT_DISPOSITION,
					"attachment; filename=\"" + req.getType() + "." + Format.JSON.getExtension() + "\"");

			writeContent(content, response);
		}
	}

	protected Content post(HTTPRequest req, HttpServletResponse response) throws IOException {
		NodeMode mode = null;
		Content content = null;

		switch (req.getAction()) {
		case Action.EXPORT_TYPES:
		case Action.EXPORT_OBJECTS:
		case Action.LOGIN:
		case Action.LOGOUT:
			mode = NodeMode.READ;
			break;

		case Action.INSERT:
			content = checkMaxInsertRequests(req);
			
			if (content != null) {
				return content;
			}
			
		case Action.UPDATE:
		case Action.UPDATE_ID:
		case Action.UPDATE_PASSWORD:
		case Action.DELETE:
		case Action.IMPORT_OBJECTS:
			mode = NodeMode.WRITE;
			break;

		case Action.CREATE:
		case Action.ALTER:
		case Action.RENAME:
		case Action.DROP:
		case Action.IMPORT_TYPES:
			mode = NodeMode.ADMIN;
			break;

		default:
			mode = NodeMode.WRITE;
		}

		try (Node nextNode = Loader.loadNode(settings.getString(KeyWords.NEXT_NODE), req, mode)) {
			LanguageSettings languageSettings = context.getLanguageSettings(req.getLang());
			ZonedDateTime udate = null;
			String[] fields = null;
			LinkedHashMap<String, TypeField> typeFields = null;
			NXObject object = null;
			String action = req.getAction();

			switch (action) {
			case Action.CREATE:
				nextNode.create(req.readType());
				content = new Content(languageSettings.gts(req.getType(), KeyWords.TYPE_SUCCESSFULLY_CREATED));
				break;

			case Action.ALTER:
				content = new Content(nextNode.alter(req.readType(), req.getADate()));
				break;

			case Action.RENAME:
				ZonedDateTime adate = nextNode.rename(req.getType(), req.getNewName());
				content = new Content(
						new RenameResult(languageSettings.gts(req.getType(), KeyWords.TYPE_SUCCESSFULLY_RENAMED), adate));
				break;

			case Action.INSERT:
				fields = req.getTypeSettings().getActionStringArray(req.getType(),
						Action.INSERT, KeyWords.FIELDS);
				typeFields = nextNode.getTypeFields(req.getType(), fields);
				
				object = req.readObject(typeFields);
				
				scanVirus(object, typeFields, Action.INSERT, req.getTypeSettings());
				
				nextNode.insert(object);
				
				content = new Content(languageSettings.gts(req.getType(), KeyWords.OBJECT_SUCCESSFULLY_INSERTED));
				insertRequest(req);
				break;

			case Action.UPDATE:
				fields = req.getTypeSettings().getActionStringArray(req.getType(),
						Action.UPDATE, KeyWords.FIELDS);
				typeFields = nextNode.getTypeFields(req.getType(), fields);
				
				object = req.readObject(typeFields);
				
				scanVirus(object, typeFields, Action.UPDATE, req.getTypeSettings());
				
				udate = nextNode.update(object, req.getUDate());
				content = new Content(new UpdateResult(languageSettings.gts(req.getType(), 
						KeyWords.OBJECT_SUCCESSFULLY_UPDATED), udate));
				break;

			case Action.UPDATE_ID:
				UpdateIdResult updateIdResult = nextNode.updateId(req.getType(), req.getId(), req.getNewId());
				content = new Content(updateIdResult);
				break;

			case Action.UPDATE_PASSWORD:
				nextNode.updatePassword(req.getType(), req.getId(), req.getField(), req.getCurrentPassword(),
						req.getNewPassword(), req.getNewPasswordRepeat());
				content = new Content(languageSettings.gts(req.getType(), KeyWords.PASSWORD_SUCCESSFULLY_UPDATED));
				break;

			case Action.DELETE:
				nextNode.delete(req.getType(), req.getObjects());
				content = new Content(languageSettings.gts(req.getType(), KeyWords.OBJECTS_SUCCESSFULLY_DELETED));
				break;

			case Action.DROP:
				nextNode.drop(req.getTypes());
				content = new Content(languageSettings.gts(KeyWords.TYPES_SUCCESSFULLY_DROPPED));
				break;

			case Action.IMPORT_OBJECTS:
				content = new Content(nextNode.importObjects(req.getDataStream(),
						req.getExistingObjectsAction()), Format.JSON);
				break;

			case Action.IMPORT_TYPES:
				content = new Content(nextNode.importTypes(req.getDataStream(), 
						req.getExistingTypesAction(), req.getExistingObjectsAction()), Format.JSON);
				break;

			case Action.EXPORT_TYPES:
				exportTypes(req, nextNode, response, languageSettings);
				break;

			case Action.EXPORT_OBJECTS:
				exportObjects(req, nextNode, response, languageSettings);
				break;

			case Action.LOGIN:
				content = login(req, nextNode, languageSettings);
				break;

			case Action.LOGOUT:
				content = logout(req, languageSettings);
				break;

			default:
				String type = req.getType();
				String id = req.getId();
				String[] objects = id != null ? new String[] { id } : req.getObjects();

				typeFields = nextNode.getActionFields(type, action);
				
				if (typeFields == null) {
					throw new ActionNotFoundException(type, action);
				}
				
				Object[] values = req.readActionFields(typeFields);
				
				scanVirus(type, values, typeFields, action, req.getTypeSettings());

				Object actionResult = nextNode.executeAction(type, objects, action, values);
				content = new Content(actionResult, Format.JSON);
				break;
			}

			nextNode.commit();
		}

		return content;
	}

	protected Content login(HTTPRequest req, Node nextNode, LanguageSettings languageSettings) {
		String remoteAddress = req.getRemoteAddress();

		checkAuthErrors(remoteAddress);

		Content content = null;

		String user = req.getLoginUser();
		if (user == null) {
			throw new NXException(KeyWords.EMPTY_USER_NAME);
		}

		String password = req.getLoginPassword();
		if (password == null) {
			throw new NXException(KeyWords.EMPTY_PASSWORD);
		}

		if (nextNode.checkPassword(KeyWords.USER, user, KeyWords.PASSWORD, password)) {
			String[] groups = nextNode.getGroups(user);

			HttpSession session = req.getSession();
			session.setAttribute(KeyWords.AUTH, new Auth(user, groups, true));

			content = new Content(languageSettings.gts(KeyWords.SUCCESSFUL_LOGIN) + ".");
			logger.info(this, user, remoteAddress, new Message(KeyWords.SUCCESSFUL_LOGIN, user));
		} else {
			authError(remoteAddress);
			throw new InvalidUserOrPasswordException();
		}

		return content;
	}

	protected Content logout(HTTPRequest req, LanguageSettings languageSettings) {
		Content content = null;
		HttpServletRequest request = req.getServletRequest();
		HttpSession session = request.getSession();
		String remoteAddress = request.getRemoteAddr();
		Auth auth = (Auth) session.getAttribute(KeyWords.AUTH);

		if (auth != null) {
			session.removeAttribute(KeyWords.AUTH);
			content = new Content(languageSettings.gts(KeyWords.SUCCESSFUL_LOGOUT));
			logger.info(this, auth.getUser(), remoteAddress, new Message(KeyWords.SUCCESSFUL_LOGOUT));
		} else {
			throw new NXException(KeyWords.USER_NOT_LOGGED_IN);
		}

		return content;
	}

	protected void put(HTTPRequest req) throws IOException {
				
		try (Node nextNode = Loader.loadNode(settings.getString(KeyWords.NEXT_NODE), req, NodeMode.WRITE)) {

			Object value = IOUtils.toByteArray(req.getServletRequest().getInputStream());
								
			if (debug) {
				Debug.body();
				
				if (binaryDebug) {
					Debug.binary((byte[]) value, binaryDebugLimit);
				}
			}
			
			String fieldType = nextNode.getTypeField(req.getType(), req.getField()).getType();
			
			scanVirus(req.getType(), Action.PUT, req.getField(), fieldType, value, req.getTypeSettings());
			
			if (req.getType() == null) {
				throw new NXException(KeyWords.EMPTY_TYPE_NAME);
			} else if (req.getId() == null) {
				throw new NXException(req.getType(), KeyWords.EMPTY_ID);
			} else if (req.getField() == null) {
				nextNode.update(req.getType(), req.getId(), (byte[]) value);
			} else {
				
				String allowedTags = null;

				switch (fieldType) {
				case PT.PASSWORD:
					throw new FieldException(req.getType(), req.getField(), KeyWords.PASSWORD_FIELD_UPDATE);

				case PT.BINARY:
					break;

				case PT.FILE:
					value = Tuple.parseFile(value);
					break;
					
				case PT.IMAGE:
					value = Tuple.parseImage(value);
					break;

				case PT.DOCUMENT:
					value = Tuple.parseDocument(value);
					break;

				case PT.AUDIO:
					value = Tuple.parseAudio(value);
					break;

				case PT.VIDEO:
					value = Tuple.parseVideo(value);
					break;

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

				case PT.DATE:
					value = Tuple.parseDate(value);
					break;

				case PT.TIME:
					value = Tuple.parseTime(value);
					break;

				case PT.DATETIME:
					value = Tuple.parseDateTime(value);
					break;

				case PT.BOOLEAN:
					value = Tuple.parseBoolean(value);
					break;

				case PT.URL:
					value = Tuple.parseURL(value);
					break;

				case PT.EMAIL:
					value = Tuple.parseEmail(value);
					break;

				case PT.TEL:
					value = Tuple.parseTel(value);
					break;

				case PT.HTML:
					allowedTags = req.getTypeSettings().getFieldString(req.getType(), req.getField(),
							KeyWords.HTML_ALLOWED_TAGS);
					value = Tuple.parseHTML(value, req.getLang(), allowedTags);
					break;

				case PT.XML:
					allowedTags = req.getTypeSettings().getFieldString(req.getType(), req.getField(),
							KeyWords.XML_ALLOWED_TAGS);
					value = Tuple.parseXML(value, req.getLang(), allowedTags);
					break;

				case PT.JSON:
					value = Tuple.parseJSON(value);
					break;

				case PT.TEXT:
					value = Tuple.parseText(value);
					break;

				case PT.TIMEZONE:
					value = Tuple.parseTimeZone(value);
					break;

				case PT.COLOR:
					value = Tuple.parseColor(value);
					break;

				default:
					value = Tuple.parseString(value);
				}

				nextNode.updateField(req.getType(), req.getId(), req.getField(), value);
			}

			nextNode.commit();
		}
	}

	protected Content webdav(HTTPRequest req) {
		Content content = null;
		
		try (WebDAVView view = getWebDAVView(req.getType(), KeyWords.WEBDAV, req)) {
			
			if (debug) {
				Debug.body();
				Debug.text(view.getRequestText());
			}
			
			try {
				if (req.getType() == null) {
					content = view.getTypesName(req.getLang(), KeyWords.WEBDAV);
				} else if (req.getId() == null) {
					content = view.select(req.getType(), req.getLang(), KeyWords.WEBDAV, req.getRef(),
							null, req.getSearch(), req.getOrder(), req.getOffset(), req.getLimit());
				} else if (req.getField() == null) {
					content = view.get(req.getType(), req.getId(), req.getLang(), KeyWords.WEBDAV, req.getETag());
				} else {
					content = view.getField(req.getType(), req.getId(), req.getField(), KeyWords.WEBDAV,
							req.getETag());
				}

			} catch (NotFoundException e) {
				content = view.notFound(req.getType(), req.getLang(), req.getView(), e);
			}
		}

		return content;
	}

	protected Content options() {
		Content content = new Content();
		content.setHeader(HTTPHeader.ALLOW, "OPTIONS, GET, POST, PUT, PROPFIND, REPORT");
		content.setHeader(HTTPHeader.DAV, "1, calendar-access");
		return content;
	}

	protected Content robots() {
		HTTPRobots robots = new HTTPRobots(settings.getString(KeyWords.HOST),
				settings.getInt32(KeyWords.HTTPS_PORT), settings.getStringArray(KeyWords.INDEX_TYPES));
		return new Content(robots.toString(), Format.TEXT);
	}

	protected Content sitemap(HttpServletRequest request, String lang, Auth auth) {
		try (Node nextNode = Loader.loadNode(settings.getString(KeyWords.NEXT_NODE), auth,
				NodeMode.READ, lang, request.getRemoteAddr(), context, true)) {

			LinkedHashMap<String, ObjectInfo[]> objectsInfo = nextNode
					.getObjectsInfo(settings.getStringArray(KeyWords.INDEX_TYPES));

			HTTPSitemap sitemap = new HTTPSitemap(settings.getString(KeyWords.HOST),
					settings.getInt32(KeyWords.HTTPS_PORT), objectsInfo);

			return new Content(sitemap.toString(), Format.XML);
		}
	}

	protected Content checkRequest(HttpServletRequest request, URL url) {
		Content content = null;

		boolean secure = request.isSecure();
		String host = settings.getString(KeyWords.HOST);
		String requestHost = request.getServerName();
		boolean validHost = requestHost.equals(host);
		String method = request.getMethod();

		if (!secure || !validHost) {

			if ("GET".equals(method)) {

				if (!secure) {
					url.setScheme(URL.HTTPS);
					url.setPort(settings.getInt32(KeyWords.HTTPS_PORT));
				}

				if (!validHost) {
					url.setHost(host);
				}

				content = new Content(HTTPStatus.MOVED_PERMANENTLY);
				content.setHeader(HTTPHeader.LOCATION, url.toString());

			} else {

				if (!validHost) {
					throw new InvalidHostNameException(requestHost);
				}

				if (!secure) {
					throw new MethodNotAllowedException(method);
				}
			}

		}

		return content;
	}

	protected String readLang(HttpServletRequest request) {
		String lang = request.getParameter(KeyWords.LANG);

		String[] langs = settings.getStringArray(KeyWords.LANGS);

		if (lang == null || !ArrayUtils.contains(langs, lang)) {

			Enumeration<Locale> locales = request.getLocales();

			while (locales.hasMoreElements()) {
				String clientLang = locales.nextElement().toLanguageTag().toLowerCase();

				if (ArrayUtils.contains(langs, clientLang)) {
					lang = clientLang;
					break;
				}
			}
		}

		if (lang == null) {
			lang = settings.getString(KeyWords.DEFAULT_LANG);
		}

		return lang;
	}

	protected Auth auth(HttpServletRequest request) {
		HttpSession session = request.getSession();

		Auth auth = (Auth) session.getAttribute(KeyWords.AUTH);

		if (auth == null) {

			try (Node nextNode = Loader.loadNode(settings.getString(KeyWords.NEXT_NODE), 
					new Auth(Auth.GUEST, Auth.GUESTS), NodeMode.READ,
					settings.getString(KeyWords.DEFAULT_LANG), request.getRemoteAddr(), context, true)) {

				String user = tlsAuth(request, nextNode);

				if (user == null) {
					user = basicAuth(request, nextNode);
				}

				if (user == null) {
					auth = new Auth(Auth.GUEST, Auth.GUESTS);
				} else {
					String[] groups = nextNode.getGroups(user);
					auth = new Auth(user, groups, false);
					session.setAttribute(KeyWords.AUTH, auth);
				}
			}
		}

		return auth;
	}

	protected String basicAuth(HttpServletRequest request, Node nextNode) {
		String user = null;

		String basicAuth = request.getHeader(HTTPHeader.AUTHORIZATION.toString());

		if (basicAuth != null) {
			String remoteAddress = request.getRemoteAddr();

			checkAuthErrors(remoteAddress);

			String userPassword = Utils.base64decode(basicAuth.substring(6, basicAuth.length()));
			int separatorIndex = userPassword.indexOf(":");
			String authUser = userPassword.substring(0, separatorIndex);
			String authPassword = userPassword.substring(separatorIndex + 1, userPassword.length());

			if (nextNode.checkPassword(KeyWords.USER, authUser, KeyWords.PASSWORD, authPassword)) {
				user = authUser;
			} else {
				authError(remoteAddress);
				throw new InvalidUserOrPasswordException();
			}
		}

		return user;
	}

	protected String tlsAuth(HttpServletRequest request, Node nextNode) {
		String user = null;

		if (request.isSecure()) {

			X509Certificate[] requestCertificates = (X509Certificate[]) request.getAttribute(X509_CERTIFICATES);

			if (requestCertificates != null && requestCertificates.length > 0) {
				String remoteAddress = request.getRemoteAddr();

				checkAuthErrors(remoteAddress);

				X509Certificate cert = ((X509Certificate[]) requestCertificates)[0];
				String subject = cert.getSubjectX500Principal().getName();
				user = nextNode.getString("select \"user\" from user_certificate"
						+ " where certificate_subject = ?", subject);

				if (user == null) {
					authError(remoteAddress);
					throw new CertificateNotFoundException(subject);
				}
			}
		}

		return user;
	}
	
	protected void debug(HttpServletRequest request) {
		
		Debug.httpRequest();
		
		String url = request.getRemoteAddr() + " " + request.getMethod() + " " + request.getRequestURL();
			
		String query = request.getQueryString();
			
		if (query != null && query.length() > 0) {
			url += "?" + query;
		}
		
		Debug.text(url);
			
		Debug.headers();		
			
		Enumeration<String> headers = request.getHeaderNames();
			
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			Debug.text(header + ": " + request.getHeader(header));
		}
		
		Debug.parameters();
		
		Map<String, String[]> parameters = request.getParameterMap();
		
		for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
			StringBuilder text = new StringBuilder(entry.getKey() + ": ");
			
			for (String value : entry.getValue()) {
				text.append(value + " ");
			}
			
			Debug.text(text.toString());
		}
				
		try {
			Collection<Part> parts = request.getParts();
			
			if (parts != null && parts.size() > 0) {
				
				for (Part part : parts) {
					String partName = part.getName();
					
					if (!parameters.keySet().contains(partName)) {
						long size = part.getSize();
						
						Debug.subtitle(partName + " " + part.getSubmittedFileName()
							+ " " + size + " bytes");
						
						if (binaryDebug && size > 0) {
							Debug.binary(IOUtils.toByteArray(part.getInputStream()), binaryDebugLimit);
						}
					}				
				}
			}
		} catch (ServletException e) {
			
		} catch (IOException e) {
			Debug.exception(e);
		}
	}
	
	protected void debug(HttpServletResponse response) {
		Debug.httpResponse();
		
		Debug.text("Status: " + response.getStatus());
		Debug.text(HTTPHeader.CONTENT_TYPE + ": " + response.getContentType());
		
		Debug.headers();
		
		for (String header : response.getHeaderNames()) {
			Debug.text(header + ": " + response.getHeader(header));
		}
	}
	
	protected void debug(HttpServletResponse response, Content content) {
		
		debug(response);

		Debug.body();
		
		Object value = content.getValue();
		if (value != null) {
								
			if (value instanceof String) {
				Debug.text((String) value);
			} else if (value instanceof byte[]) {
				if (binaryDebug) {
					Debug.binary((byte[]) value, binaryDebugLimit);
				}
			} else {
				Debug.text(new Serial(value, Format.JSON).toString());
			}
		}
	}

	protected void service(HttpServletRequest request, HttpServletResponse response) {
		
		String lang = null;
		LanguageSettings languageSettings = null;
		String user = Auth.GUEST;

		try {
			request.setCharacterEncoding(Constants.UTF_8_CHARSET);
			response.setCharacterEncoding(Constants.UTF_8_CHARSET);
			
			if (debug) {
				debug(request);		
			}

			Content content = checkMaxRequests(request);

			if (content == null) {

				lang = readLang(request);
				languageSettings = context.getLanguageSettings(lang);
				Auth auth = auth(request);
				user = auth.getUser();

				URL url = new URL(request);

				log(request, user, url);

				content = checkRequest(request, url);

				if (content == null) {
					if (url.isRobots()) {
						content = robots();
					} else if (url.isSitemap()) {
						content = sitemap(request, lang, auth);
					} else {
						HTTPRequest req = new HTTPRequest(request, settings, context, lang, 
								languageSettings, auth, url);

						switch (req.getRequestMethod()) {
						case GET:
							content = get(req, response);
							break;

						case POST:
							content = post(req, response);
							break;

						case PUT:
							put(req);
							break;

						case PROPFIND:
						case REPORT:
							content = webdav(req);
							break;

						case HEAD:
							break;

						case OPTIONS:
							content = options();
							break;
						}
					}
				}
			}

			writeContent(content, response);

		} catch (Exception e) {
			writeException(e, request, response, languageSettings, user);
		}
	}

	protected void writeException(Exception e, HttpServletRequest request, HttpServletResponse response,
			LanguageSettings languageSettings, String user) {
		
		HTTPStatus status = null;
		String message = null;

		if (e instanceof NotFoundException) {
			status = HTTPStatus.NOT_FOUND;
		} else if (e instanceof MethodNotAllowedException) {
			status = HTTPStatus.METHOD_NOT_ALLOWED;
		} else if (e instanceof UnauthorizedException) {
			status = HTTPStatus.UNAUTHORIZED;
			if (Auth.GUEST.equals(user)) {
				String[] basicAuthUserAgents = settings.getStringArray(KeyWords.BASIC_AUTH_USER_AGENTS);

				if (basicAuthUserAgents != null) {
					String clientUserAgent = request.getHeader(HTTPHeader.USER_AGENT.toString());

					for (String basicAuthUserAgent : basicAuthUserAgents) {
						if (clientUserAgent.contains(basicAuthUserAgent)) {
							String basic_auth_realm = settings.getString(KeyWords.BASIC_AUTH_REALM);
							response.setHeader(HTTPHeader.WWW_AUTHENTICATE.toString(),
									"Basic realm=\"" + basic_auth_realm + "\"");
							break;
						}
					}
				}
			}
		} else {
			status = HTTPStatus.INTERNAL_SERVER_ERROR;
		}
		
		response.setStatus(status.toInt32());
		response.setContentType(Format.TEXT.getContentType());

		if (e instanceof NXException) {
			message = ((NXException) e).getMessage(languageSettings);
		} else {
			message = NXException.getMessage(e);
		}
		
		if (debug) {
			debug(response);
			Debug.exception(message, e);
		}

		String remoteAddress = request.getRemoteAddr();

		try {
			logException(e, user, remoteAddress);
			response.getWriter().write(message);
		} catch (Exception e2) {
			logger.severe(user, remoteAddress, e2);
		}
	}

	protected void logException(Exception e, String user, String remoteAddress) {
		logger.warning(user, remoteAddress, e);
	}

	protected void writeContent(Content content, HttpServletResponse response) throws IOException {
		if (content != null) {
			response.setStatus(content.getStatus());
			response.setContentType(content.getContentType());

			for (Map.Entry<String, String> entry : content.getHeaders().entrySet()) {
				response.setHeader(entry.getKey(), entry.getValue());
			}
			
			if (debug) {
				debug(response, content);
			}

			Object value = content.getValue();
			if (value != null) {
						
				ServletOutputStream output = response.getOutputStream();

				if (value instanceof String) {
					output.write(((String) value).getBytes());
				} else if (value instanceof byte[]) {
					output.write((byte[]) value);
				} else {
					new Serial(value, Format.JSON).write(output);
				}
			}
		}
	}
	
	protected View getView(HTTPRequest request) {
		String type = request.getType();
		String view = request.getView();

		return getView(type, view, request);
	}

	protected View getView(String type, String view, HTTPRequest req) {
		String className = req.getTypeSettings().getView(type, view);

		if (className != null) {
			return Loader.loadView(className, req);
		} else {
			throw new ViewNotFoundException(req.getType(), view);
		}
	}
	
	protected WebDAVView getWebDAVView(String type, String view, HTTPRequest req) {
		String className = req.getTypeSettings().getView(type, view);
		
		if (className != null) {
			return Loader.loadWebDAVView(className, req);
		} else {
			throw new ViewNotFoundException(req.getType(), view);
		}
	}
	
	protected void log(HttpServletRequest request, String user, URL url) {
		logger.info(this, user, request.getRemoteAddr(), request.getMethod() + " " + url);
	}

	protected Content checkMaxInsertRequests(HTTPRequest req) {
		Content content = null;
		
		String type = req.getType();
		
		int maxInserts = req.getTypeSettings().getTypeInt32(type, KeyWords.MAX_INSERTS);

		if (maxInserts > 0) {
			
			String remoteAddress = req.getRemoteAddress();

			ConcurrentHashMap<String, Requests> typesRequests = insertRequestsMap.get(remoteAddress);
			
			if (typesRequests != null) {
			
				Requests requests = typesRequests.get(type);

				if (requests != null) {
					long now = System.currentTimeMillis();

					if (now - requests.firstRequestTime > Constants.MINUTE_MILLISECONDS) {
						typesRequests.remove(type);
					} else {
				
						if (requests.requests >= maxInserts) {
							String message = req.getLanguageSettings().gts(type,
									KeyWords.MAX_INSERTS_EXCEEDED);

							content = new Content(message, Format.TEXT, HTTPStatus.TOO_MANY_REQUESTS);

							if (!requests.logged) {
								logger.severe(this, req.getAuth().getUser(), remoteAddress,
									new Message(KeyWords.MAX_INSERTS_EXCEEDED));
								requests.logged = true;
							}
						}
					}
				}
			}
		}

		return content;
	}

	protected Content checkMaxRequests(HttpServletRequest req) {
		Content content = null;

		if (maxRequests > 0) {
			String remoteAddress = req.getRemoteAddr();

			Requests requests = requestsMap.get(remoteAddress);

			if (requests != null) {
				long now = System.currentTimeMillis();

				if (now - requests.firstRequestTime > Constants.MINUTE_MILLISECONDS) {
					requests.requests = 1;
					requests.firstRequestTime = now;
					requests.logged = false;
				} else {
					requests.requests += 1;

					if (requests.requests > maxRequests) {

						content = new Content(MAX_REQUESTS, Format.TEXT, HTTPStatus.TOO_MANY_REQUESTS);

						if (!requests.logged) {
							Auth auth = (Auth) req.getSession().getAttribute(KeyWords.AUTH);
							String user = auth != null ? auth.getUser() : Auth.GUEST;

							logger.severe(this, user, remoteAddress, MAX_REQUESTS);
							requests.logged = true;
						}
					}
				}
			} else {
				requestsMap.putIfAbsent(remoteAddress, new Requests());
			}
		}

		return content;
	}

	protected void insertRequest(HTTPRequest req) {
		
		String type = req.getType();
		
		int maxInserts = req.getTypeSettings().getTypeInt32(type, KeyWords.MAX_INSERTS);

		if (maxInserts > 0) {
			
			String remoteAddress = req.getRemoteAddress();
			
			ConcurrentHashMap<String, Requests> typesRequests = insertRequestsMap.get(remoteAddress);
			
			if (typesRequests == null) {
				typesRequests = new ConcurrentHashMap<>();
				insertRequestsMap.putIfAbsent(remoteAddress, typesRequests);
			}

			Requests requests = typesRequests.get(type);

			if (requests == null) {
				requests = new Requests();
				typesRequests.putIfAbsent(type, requests);
			} else {
				requests.requests += 1;
			}
		}
	}

	protected void authError(String remoteAddress) {

		if (maxAuthErrors > 0) {
			
			Requests authErrors = authErrorsMap.get(remoteAddress);
	
			if (authErrors == null) {
				authErrors = new Requests();
				authErrorsMap.putIfAbsent(remoteAddress, authErrors);
			} else {
				authErrors.requests += 1;
			}
		}
	}

	protected void checkAuthErrors(String remoteAddress) {

		if (maxAuthErrors > 0) {
			
			Requests authErrors = authErrorsMap.get(remoteAddress);
	
			if (authErrors != null) {
	
				long now = System.currentTimeMillis();
	
				if (now - authErrors.firstRequestTime > Constants.MINUTE_MILLISECONDS) {
					authErrorsMap.remove(remoteAddress);
				} else {
	
					if (authErrors.requests >= maxAuthErrors) {
	
						throw new NXException(KeyWords.AUTH_ERRORS_PER_MINUTE_EXCEEDED);
					}
				}
			}
		}
	}
	
	protected void scanVirus(NXObject object, LinkedHashMap<String, TypeField> typeFields, String action,
			TypeSettings typeSettings) {
		
		String type = object.getType();
		
		if (typeSettings.getActionBoolean(type, action, KeyWords.ANTIVIRUS)) {
			
			antivirus.scan(object, typeFields);
		}
	}
	
	protected void scanVirus(String type, Object[] parameters, LinkedHashMap<String, TypeField> typeFields,
			String action, TypeSettings typeSettings) {
		
		if (typeSettings.getActionBoolean(type, action, KeyWords.ANTIVIRUS)) {
			
			antivirus.scan(type, parameters, typeFields);
		}
	}
	
	protected void scanVirus(String type, String action, String field, String fieldType, Object value,
			TypeSettings typeSettings) {
		
		if (typeSettings.getActionBoolean(type, action, KeyWords.ANTIVIRUS)) {
			
			antivirus.scan(type, action, field, fieldType);
		}
	}
	
	protected class Requests {
		protected int requests;
		protected long firstRequestTime;
		protected boolean logged;

		protected Requests() {
			requests = 1;
			firstRequestTime = System.currentTimeMillis();
			logged = false;
		}
	}
}