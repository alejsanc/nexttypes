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
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.client.utils.URIBuilder;

import com.nexttypes.datatypes.AlterResult;
import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.Message;
import com.nexttypes.datatypes.ObjectInfo;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.RenameResponse;
import com.nexttypes.datatypes.Serial;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.URI;
import com.nexttypes.datatypes.UpdateResponse;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.NodeMode;
import com.nexttypes.exceptions.CertificateNotFoundException;
import com.nexttypes.exceptions.FieldException;
import com.nexttypes.exceptions.InvalidHostNameException;
import com.nexttypes.exceptions.InvalidUserOrPasswordException;
import com.nexttypes.exceptions.MethodNotAllowedException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.exceptions.NotFoundException;
import com.nexttypes.exceptions.UnauthorizedActionException;
import com.nexttypes.exceptions.ViewNotFoundException;
import com.nexttypes.interfaces.Node;
import com.nexttypes.interfaces.Stream;
import com.nexttypes.logging.Logger;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.Strings;
import com.nexttypes.system.Action;
import com.nexttypes.system.Constants;
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
	protected ConcurrentHashMap<String, Requests> requestsMap = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, AuthErrors> authErrorsMap = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, Requests> insertRequestsMap = new ConcurrentHashMap<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		context = Context.get(getServletContext());
		settings = context.getSettings(Settings.HTTP_SETTINGS);
		maxRequests = settings.getInt32(Constants.MAX_REQUESTS);
		maxAuthErrors = settings.getInt32(Constants.MAX_AUTH_ERRORS);
		debug = settings.getBoolean(Constants.DEBUG);
		binaryDebug = settings.getBoolean(Constants.BINARY_DEBUG);
		binaryDebugLimit = settings.getInt32(Constants.BINARY_DEBUG_LIMIT);
		logger = context.getLogger();
	}

	protected Content get(HTTPRequest req, HttpServletResponse response) throws IOException, URISyntaxException {

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

						if (req.isInfo()) {
							content = view.getTypesInfo(req.getLang(), req.getView());
						} else if (req.isNames()) {
							content = view.getTypesName(req.getLang(), req.getView());
						} else if (req.isReferences()) {
							content = view.getReferences(req.getLang(), req.getView());
						} else {
							URIBuilder newURI = new URIBuilder(settings.getString(Constants.INDEX));
							newURI.setParameter(Constants.LANG, req.getLang());
							newURI.setParameter(Constants.VIEW, req.getView());
							content = new Content(HTTPStatus.FOUND);
							content.setHeader(HTTPHeader.LOCATION, newURI.toString());
						}
					}

				} else if (id == null) {
					
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
						content = view.executeActionForm(req.getType(), null, req.getAction(), req.getLang(),
								req.getView());
					} else {
						LinkedHashMap<String, TypeField> typeFields = null;
						
						if (req.isInfo()) {
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
					}

				} else if (req.getField() == null) {
					
					if (Action.UPDATE.equals(form)) {
						content = view.updateForm(req.getType(), req.getId(), req.getLang(), req.getView());
					} else if (Action.UPDATE_ID.equals(form)) {
						content = view.updateIdForm(req.getType(), req.getId(), req.getLang(), req.getView());
					} else if (Action.EXECUTE_ACTION.equals(form)) {
						content = view.executeActionForm(req.getType(), req.getId(), req.getAction(), req.getLang(),
								req.getView());
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
					} else {
						content = view.getField(req.getType(), req.getId(), req.getField(), req.getETag());
					}
				} else {

					content = view.getElement(req.getType(), req.getId(), req.getField(), req.getElement(),
							req.getLang(), req.getView(), req.getETag());
				}

			} catch (NotFoundException e) {
				logException(e, req.getUser(), req.getRemoteAddress());
				content = view.notFound(req.getType(), req.getLang(), req.getView(), e);
			} catch (UnauthorizedActionException e) {
				logException(e, req.getUser(), req.getRemoteAddress());
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

	protected void exportTypes(HTTPRequest req, Node nextNode, HttpServletResponse response, Strings strings)
			throws IOException {

		try (Stream stream = nextNode.exportTypes(req.getTypes(), req.getLang(), req.includeObjects())) {
			Content content = new Content(stream, Format.JSON);
			content.setHeader(HTTPHeader.CONTENT_DISPOSITION,
					"attachment; filename=\"types." + Format.JSON.getExtension() + "\"");

			writeContent(content, response);
		}
	}

	protected void exportObjects(HTTPRequest req, Node nextNode, HttpServletResponse response, Strings strings)
			throws IOException {

		try (Stream stream = nextNode.exportObjects(req.getType(), req.getObjects(), req.getLang(), req.getOrder())) {
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
			content = maxInsertRequestsExceeded(req);
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

		try (Node nextNode = Loader.loadNode(settings.getString(Constants.NEXT_NODE), req, mode)) {
			Strings strings = context.getStrings(req.getLang());
			ZonedDateTime udate = null;

			switch (req.getAction()) {
			case Action.CREATE:
				nextNode.create(req.readType());
				content = new Content(strings.gts(req.getType(), Constants.TYPE_SUCCESSFULLY_CREATED));
				break;

			case Action.ALTER:
				AlterResult alterResult = nextNode.alter(req.readType(), req.getADate());
				content = new Content(alterResult);
				break;

			case Action.RENAME:
				ZonedDateTime adate = nextNode.rename(req.getType(), req.getNewName());
				content = new Content(
						new RenameResponse(strings.gts(req.getType(), Constants.TYPE_SUCCESSFULLY_RENAMED), adate));
				break;

			case Action.INSERT:
				nextNode.insert(req.readObject(nextNode.getTypeFields(req.getType())));
				content = new Content(strings.gts(req.getType(), Constants.OBJECT_SUCCESSFULLY_INSERTED));
				insertRequest(req);
				break;

			case Action.UPDATE:
				udate = nextNode.update(req.readObject(nextNode.getTypeFields(req.getType())), req.getUDate());
				content = new Content(
						new UpdateResponse(strings.gts(req.getType(), Constants.OBJECT_SUCCESSFULLY_UPDATED), udate));
				break;

			case Action.UPDATE_ID:
				udate = nextNode.updateId(req.getType(), req.getId(), req.getNewId());
				content = new Content(new UpdateResponse(
						strings.gts(req.getType(), Constants.OBJECT_ID_SUCCESSFULLY_UPDATED), udate));
				break;

			case Action.UPDATE_PASSWORD:
				nextNode.updatePassword(req.getType(), req.getId(), req.getField(), req.getCurrentPassword(),
						req.getNewPassword(), req.getNewPasswordRepeat());
				content = new Content(strings.gts(req.getType(), Constants.PASSWORD_SUCCESSFULLY_UPDATED));
				break;

			case Action.DELETE:
				nextNode.delete(req.getType(), req.getObjects());
				content = new Content(strings.gts(req.getType(), Constants.OBJECTS_SUCCESSFULLY_DELETED));
				break;

			case Action.DROP:
				nextNode.drop(req.getTypes());
				content = new Content(strings.gts(Constants.TYPES_SUCCESSFULLY_DROPPED));
				break;

			case Action.IMPORT_OBJECTS:
				content = new Content(nextNode.importObjects(req.getDataStream(), req.getExistingObjectsAction()),
						Format.JSON);
				break;

			case Action.IMPORT_TYPES:
				content = new Content(nextNode.importTypes(req.getDataStream(), req.getExistingTypesAction(),
						req.getExistingObjectsAction()), Format.JSON);
				break;

			case Action.EXPORT_TYPES:
				exportTypes(req, nextNode, response, strings);
				break;

			case Action.EXPORT_OBJECTS:
				exportObjects(req, nextNode, response, strings);
				break;

			case Action.LOGIN:
				content = login(req, nextNode, strings);
				break;

			case Action.LOGOUT:
				content = logout(req, strings);
				break;

			default:
				String id = req.getId();
				String[] objects = id != null ? new String[] { id } : req.getObjects();

				LinkedHashMap<String, TypeField> fields = nextNode.getActionFields(req.getType(), req.getAction());
				Object[] values = req.readActionFields(fields);

				Object actionResult = nextNode.executeAction(req.getType(), objects, req.getAction(), values);
				content = new Content(actionResult, Format.JSON);
				break;
			}

			nextNode.commit();
		}

		return content;
	}

	protected Content login(HTTPRequest req, Node nextNode, Strings strings) {
		String remoteAddress = req.getRemoteAddress();

		checkAuthErrors(remoteAddress);

		Content content = null;

		String user = req.getLoginUser();
		if (user == null) {
			throw new NXException(Constants.EMPTY_USER_NAME);
		}

		String password = req.getLoginPassword();
		if (password == null) {
			throw new NXException(Constants.EMPTY_PASSWORD);
		}

		if (nextNode.checkPassword(Constants.USER, user, Constants.PASSWORD, password)) {
			String[] groups = nextNode.getGroups(user);

			HttpSession session = req.getSession();
			session.setAttribute(Constants.USER, new Auth(user, groups, true));

			content = new Content(strings.gts(Constants.SUCCESSFUL_LOGIN) + ".");
			logger.info(this, user, remoteAddress, new Message(Constants.SUCCESSFUL_LOGIN, user));
		} else {
			authError(remoteAddress);
			throw new InvalidUserOrPasswordException();
		}

		return content;
	}

	protected Content logout(HTTPRequest req, Strings strings) {
		Content content = null;
		HttpServletRequest request = req.getServletRequest();
		HttpSession session = request.getSession();
		String remoteAddress = request.getRemoteAddr();
		Auth user = (Auth) session.getAttribute(Constants.USER);

		if (user != null) {
			session.removeAttribute(Constants.USER);
			content = new Content(strings.gts(Constants.SUCCESSFUL_LOGOUT));
			logger.info(this, user.getUser(), remoteAddress, new Message(Constants.SUCCESSFUL_LOGOUT));
		} else {
			throw new NXException(Constants.USER_NOT_LOGGED_IN);
		}

		return content;
	}

	protected void put(HTTPRequest req) throws IOException {
				
		try (Node nextNode = Loader.loadNode(settings.getString(Constants.NEXT_NODE), req, NodeMode.WRITE)) {

			Object value = IOUtils.toByteArray(req.getServletRequest().getInputStream());
			
			if (debug) {
				Debug.body();
				
				if (binaryDebug) {
					Debug.binary((byte[]) value, binaryDebugLimit);
				}
			}
			
			if (req.getType() == null) {
				throw new NXException(Constants.EMPTY_TYPE_NAME);
			} else if (req.getId() == null) {
				throw new NXException(req.getType(), Constants.EMPTY_ID);
			} else if (req.getField() == null) {
				nextNode.update(req.getType(), req.getId(), (byte[]) value);
			} else {
				
				String fieldType = nextNode.getTypeField(req.getType(), req.getField()).getType();
				String allowedTags = null;

				switch (fieldType) {
				case PT.PASSWORD:
					throw new FieldException(req.getType(), req.getField(), Constants.PASSWORD_FIELD_UPDATE);

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
					value = Tuple.parseDatetime(value);
					break;

				case PT.BOOLEAN:
					value = Tuple.parseBoolean(value);
					break;

				case PT.URI:
					value = Tuple.parseURI(value);
					break;

				case PT.EMAIL:
					value = Tuple.parseEmail(value);
					break;

				case PT.TEL:
					value = Tuple.parseTel(value);
					break;

				case PT.HTML:
					allowedTags = req.getTypeSettings().getFieldString(req.getType(), req.getField(),
							Constants.HTML_ALLOWED_TAGS);
					value = Tuple.parseHTML(value, req.getLang(), allowedTags);
					break;

				case PT.XML:
					allowedTags = req.getTypeSettings().getFieldString(req.getType(), req.getField(),
							Constants.XML_ALLOWED_TAGS);
					value = Tuple.parseXML(value, req.getLang(), allowedTags);
					break;

				case PT.JSON:
					value = Tuple.parseJSON(value);
					break;

				case PT.TEXT:
					value = Tuple.parseText(value);
					break;

				case PT.TIMEZONE:
					value = Tuple.parseTimezone(value);
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
		
		try (WebDAVView view = getWebDAVView(req.getType(), Constants.WEBDAV, req)) {
			
			if (debug) {
				Debug.body();
				Debug.text(view.getRequestText());
			}
			
			try {
				if (req.getType() == null) {
					content = view.getTypesName(req.getLang(), Constants.WEBDAV);
				} else if (req.getId() == null) {
					content = view.select(req.getType(), req.getLang(), Constants.WEBDAV, req.getRef(),
							null, req.getSearch(), req.getOrder(), req.getOffset(), req.getLimit());
				} else if (req.getField() == null) {
					content = view.get(req.getType(), req.getId(), req.getLang(), Constants.WEBDAV, req.getETag());
				} else {
					content = view.getField(req.getType(), req.getId(), req.getField(), req.getETag());
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
		HTTPRobots robots = new HTTPRobots(settings.getString(Constants.HOST), settings.getInt32(Constants.HTTPS_PORT),
				settings.getStringArray(Constants.INDEX_TYPES));
		return new Content(robots.toString(), Format.TEXT);
	}

	protected Content sitemap(HttpServletRequest request, String lang, Auth auth) {
		try (Node nextNode = Loader.loadNode(settings.getString(Constants.NEXT_NODE), auth.getUser(), auth.getGroups(),
				NodeMode.READ, lang, request.getRemoteAddr(), context, true)) {

			LinkedHashMap<String, ObjectInfo[]> objectsInfo = nextNode
					.getObjectsInfo(settings.getStringArray(Constants.INDEX_TYPES));

			HTTPSitemap sitemap = new HTTPSitemap(settings.getString(Constants.HOST),
					settings.getInt32(Constants.HTTPS_PORT), objectsInfo);

			return new Content(sitemap.toString(), Format.XML);
		}
	}

	protected Content checkRequest(HttpServletRequest request, URI uri) {
		Content content = null;

		boolean secure = request.isSecure();
		String host = settings.getString(Constants.HOST);
		String requestHost = request.getServerName();
		boolean validHost = requestHost.equals(host);
		String method = request.getMethod();

		if (!secure || !validHost) {

			if ("GET".equals(method)) {

				if (!secure) {
					uri.setScheme(URI.HTTPS);
					uri.setPort(settings.getInt32(Constants.HTTPS_PORT));
				}

				if (!validHost) {
					uri.setHost(host);
				}

				content = new Content(HTTPStatus.MOVED_PERMANENTLY);
				content.setHeader(HTTPHeader.LOCATION, uri.toString());

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
		String lang = request.getParameter(Constants.LANG);

		String[] langs = settings.getStringArray(Constants.LANGS);

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
			lang = settings.getString(Constants.DEFAULT_LANG);
		}

		return lang;
	}

	protected Auth auth(HttpServletRequest request) {
		HttpSession session = request.getSession();

		Auth user = (Auth) session.getAttribute(Constants.USER);

		if (user == null) {

			try (Node nextNode = Loader.loadNode(settings.getString(Constants.NEXT_NODE), Auth.GUEST,
					new String[] { Auth.GUESTS }, NodeMode.READ, settings.getString(Constants.DEFAULT_LANG),
					request.getRemoteAddr(), context, true)) {

				String userName = tlsAuth(request, nextNode);

				if (userName == null) {
					userName = basicAuth(request, nextNode);
				}

				if (userName == null) {
					user = new Auth();
				} else {
					String[] groups = nextNode.getGroups(userName);
					user = new Auth(userName, groups, false);
					session.setAttribute(Constants.USER, user);
				}
			}
		}

		return user;
	}

	protected String basicAuth(HttpServletRequest request, Node nextNode) {
		String userName = null;

		String basicAuth = request.getHeader(HTTPHeader.AUTHORIZATION.toString());

		if (basicAuth != null) {
			String remoteAddress = request.getRemoteAddr();

			checkAuthErrors(remoteAddress);

			String userPassword = Utils.base64decode(basicAuth.substring(6, basicAuth.length()));
			int separatorIndex = userPassword.indexOf(":");
			String authUser = userPassword.substring(0, separatorIndex);
			String authPassword = userPassword.substring(separatorIndex + 1, userPassword.length());

			if (nextNode.checkPassword(Constants.USER, authUser, Constants.PASSWORD, authPassword)) {
				userName = authUser;
			} else {
				authError(remoteAddress);
				throw new InvalidUserOrPasswordException();
			}
		}

		return userName;
	}

	protected String tlsAuth(HttpServletRequest request, Node nextNode) {
		String userName = null;

		if (request.isSecure()) {

			X509Certificate[] requestCertificates = (X509Certificate[]) request.getAttribute(X509_CERTIFICATES);

			if (requestCertificates != null && requestCertificates.length > 0) {
				String remoteAddress = request.getRemoteAddr();

				checkAuthErrors(remoteAddress);

				X509Certificate cert = ((X509Certificate[]) requestCertificates)[0];
				String subject = cert.getSubjectX500Principal().getName();
				userName = nextNode.getString("select \"user\" from user_certificate where certificate_subject = ?",
						subject);

				if (userName == null) {
					authError(remoteAddress);
					throw new CertificateNotFoundException(subject);
				}
			}
		}

		return userName;
	}
	
	protected void debug(HttpServletRequest request) {
		
		Debug.httpRequest();
		
		String uri = request.getRemoteAddr() + " " + request.getMethod() + " " + request.getRequestURL();
			
		String query = request.getQueryString();
			
		if (query != null && query.length() > 0) {
			uri += "?" + query;
		}
		
		Debug.text(uri);
			
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
	
	protected void debug(HttpServletResponse response, Content content) {
		Debug.httpResponse();
			
		Debug.text("Status: " + content.getStatus());
		Debug.text(HTTPHeader.CONTENT_TYPE + ": " + content.getContentType());
			
		Debug.headers();
			
		for (String header : response.getHeaderNames()) {
			Debug.text(header + ": " + response.getHeader(header));
		}

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
		Strings strings = null;
		String user = Auth.GUEST;

		try {
			request.setCharacterEncoding(Constants.UTF_8_CHARSET);
			response.setCharacterEncoding(Constants.UTF_8_CHARSET);
			
			if (debug) {
				debug(request);		
			}

			Content content = maxRequestsExceeded(request);

			if (content == null) {

				lang = readLang(request);
				strings = context.getStrings(lang);
				Auth auth = auth(request);
				user = auth.getUser();

				URI uri = new URI(request);

				log(request, user, uri);

				content = checkRequest(request, uri);

				if (content == null) {
					if (uri.isRobots()) {
						content = robots();
					} else if (uri.isSitemap()) {
						content = sitemap(request, lang, auth);
					} else {
						HTTPRequest req = new HTTPRequest(request, settings, context, lang, strings, auth, uri);

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
			writeException(e, request, response, strings, user);
		}
	}

	protected void writeException(Exception e, HttpServletRequest request, HttpServletResponse response,
			Strings strings, String userName) {
		
		HTTPStatus status = null;
		String message = null;

		if (e instanceof NotFoundException) {
			status = HTTPStatus.NOT_FOUND;
		} else if (e instanceof MethodNotAllowedException) {
			status = HTTPStatus.METHOD_NOT_ALLOWED;
		} else if (e instanceof UnauthorizedActionException) {
			status = HTTPStatus.UNAUTHORIZED;
			if (Auth.GUEST.equals(userName)) {
				String[] basicAuthUserAgents = settings.getStringArray(Constants.BASIC_AUTH_USER_AGENTS);

				if (basicAuthUserAgents != null) {
					String clientUserAgent = request.getHeader(HTTPHeader.USER_AGENT.toString());

					for (String basicAuthUserAgent : basicAuthUserAgents) {
						if (clientUserAgent.contains(basicAuthUserAgent)) {
							String basic_auth_realm = settings.getString(Constants.BASIC_AUTH_REALM);
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

		if (e instanceof NXException) {
			message = ((NXException) e).getMessage(strings);
		} else {
			message = NXException.getMessage(e);
		}
		
		if (debug) {
			Debug.exception(message, e);
		}

		String remoteAddress = request.getRemoteAddr();

		try {
			logException(e, userName, remoteAddress);
			response.setStatus(status.toInt32());
			response.setContentType(Format.TEXT.getContentType());
			response.getWriter().write(message);
		} catch (Exception e2) {
			logger.severe(userName, remoteAddress, e2);
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
	
	protected void log(HttpServletRequest request, String user, URI uri) {
		logger.info(this, user, request.getRemoteAddr(), request.getMethod() + " " + uri);
	}

	protected Content maxInsertRequestsExceeded(HTTPRequest req) {
		Content content = null;

		String remoteAddress = req.getRemoteAddress();

		Requests requests = insertRequestsMap.get(remoteAddress);

		if (requests != null) {
			long now = System.currentTimeMillis();

			if (requests.firstRequestTime + 60000 < now) {
				insertRequestsMap.remove(remoteAddress);
			} else {
				String type = req.getType();

				int maxInsertsPerMinute = req.getTypeSettings().getTypeInt32(type, Constants.MAX_INSERTS);

				if (requests.requests >= maxInsertsPerMinute) {
					String message = req.getStrings().gts(type, Constants.MAX_INSERTS_EXCEEDED);

					content = new Content(message, Format.TEXT, HTTPStatus.TOO_MANY_REQUESTS);

					if (!requests.logged) {
						logger.severe(this, req.getUser(), remoteAddress, new Message(Constants.MAX_INSERTS_EXCEEDED));
						requests.logged = true;
					}
				}
			}
		}

		return content;
	}

	protected Content maxRequestsExceeded(HttpServletRequest req) {
		Content content = null;

		String remoteAddress = req.getRemoteAddr();

		Requests requests = requestsMap.get(remoteAddress);

		if (requests != null) {
			long now = System.currentTimeMillis();

			if (requests.firstRequestTime + 60000 < now) {
				requests.requests = 1;
				requests.firstRequestTime = now;
				requests.logged = false;
			} else {
				requests.requests += 1;

				if (requests.requests > maxRequests) {

					content = new Content(MAX_REQUESTS, Format.TEXT, HTTPStatus.TOO_MANY_REQUESTS);

					if (!requests.logged) {
						Auth auth = (Auth) req.getSession().getAttribute(Constants.USER);
						String userName = auth != null ? auth.getUser() : Auth.GUEST;

						logger.severe(this, userName, remoteAddress, MAX_REQUESTS);
						requests.logged = true;
					}
				}
			}
		} else {
			requestsMap.putIfAbsent(remoteAddress, new Requests());
		}

		return content;
	}

	protected void insertRequest(HTTPRequest req) {
		String remoteAddress = req.getRemoteAddress();

		Requests requests = insertRequestsMap.get(remoteAddress);

		if (requests == null) {
			requests = new Requests();
			insertRequestsMap.putIfAbsent(remoteAddress, requests);
		} else {
			requests.requests += 1;
		}
	}

	protected void authError(String remoteAddress) {

		AuthErrors authErrors = authErrorsMap.get(remoteAddress);

		if (authErrors == null) {
			authErrors = new AuthErrors();
			authErrorsMap.putIfAbsent(remoteAddress, authErrors);
		} else {
			authErrors.errors += 1;
		}
	}

	protected void checkAuthErrors(String remoteAddress) {

		AuthErrors authErrors = authErrorsMap.get(remoteAddress);

		if (authErrors != null) {

			long now = System.currentTimeMillis();

			if (authErrors.firstErrorTime + 60000 < now) {
				authErrorsMap.remove(remoteAddress);
			} else {

				if (authErrors.errors >= maxAuthErrors) {

					throw new NXException(Constants.AUTH_ERRORS_PER_MINUTE_EXCEEDED);
				}
			}
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

	protected class AuthErrors {
		protected int errors;
		protected long firstErrorTime;

		protected AuthErrors() {
			errors = 1;
			firstErrorTime = System.currentTimeMillis();
		}
	}
}