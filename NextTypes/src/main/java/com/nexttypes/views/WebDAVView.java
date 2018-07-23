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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.jcr.DavLocatorFactoryImpl;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.w3c.dom.Document;

import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.FieldInfo;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeInfo;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.protocol.http.HTTPStatus;
import com.nexttypes.settings.Settings;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Utils;

public class WebDAVView extends View {
	public static final DavPropertyName CHILDCOUNT_PROPERTY_NAME = DavPropertyName.create("childcount",
			DavConstants.NAMESPACE);

	protected WebdavRequest davRequest;
	protected ReportInfo reportInfo;
	protected MultiStatus multiStatus;
	protected DavPropertyNameSet requestProperties;
	protected MultiStatusResponse response;
	protected int propFindType;
	protected int depth;
	protected String path;
	protected boolean debug;

	public WebDAVView(HTTPRequest request) {
		super(request, Settings.WEBDAV_SETTINGS);
		
		DavLocatorFactory factory = new DavLocatorFactoryImpl("");
		davRequest = new WebdavRequest(request.getServletRequest(), factory);
		
		debug = settings.getBoolean(Constants.DEBUG);
		
		if (debug) {
			try { 
				System.out.println("--------------- WEBDAV CLIENT ---------------");
				System.out.println(Utils.toString(davRequest.getRequestDocument()));
				System.out.println();
			} catch (DavException e) {
				throw new NXException(e);
			}
		}
		
		multiStatus = new MultiStatus();
		path = request.getURIPath();
		response = addResponse(path);
		depth = davRequest.getDepth();
		
		try {
			switch (request.getRequestMethod()) {
			case PROPFIND:
				propFindType = davRequest.getPropFindType();
				requestProperties = davRequest.getPropFindProperties();
				break;
				
			case REPORT:
				reportInfo = davRequest.getReportInfo();
				requestProperties = reportInfo.getPropertyNameSet();
			}
		} catch (DavException e) {
			throw new NXException(e);
		}
	}

	@Override
	public Content getTypesName(String lang, String view) {

		switch (propFindType) {
		case DavConstants.PROPFIND_ALL_PROP:
			response.add(new ResourceType(ResourceType.COLLECTION));
			break;

		case DavConstants.PROPFIND_PROPERTY_NAMES:
			response.add(DavPropertyName.RESOURCETYPE);
			break;

		case DavConstants.PROPFIND_BY_PROPERTY:
			DavPropertyNameIterator properties = requestProperties.iterator();

			while (properties.hasNext()) {
				DavPropertyName property = properties.next();

				if (DavPropertyName.RESOURCETYPE.equals(property)) {
					response.add(new ResourceType(ResourceType.COLLECTION));
				} else {
					response.add(property, HTTPStatus.NOT_FOUND.toInt32());
				}
			}
			break;
		}

		if (depth == DavConstants.DEPTH_1) {
			TypeInfo[] resources = nextNode.getTypesInfo();
			ZonedDateTime udate = null;

			for (TypeInfo resource : resources) {
				MultiStatusResponse response = addResponse(path + resource.getName());

				switch (propFindType) {
				case DavConstants.PROPFIND_ALL_PROP:
					udate = resource.getUDate();

					response.add(new ResourceType(ResourceType.COLLECTION));
					response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
							udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
					response.add(new DefaultDavProperty(CHILDCOUNT_PROPERTY_NAME, resource.getObjects()));
					break;

				case DavConstants.PROPFIND_PROPERTY_NAMES:
					response.add(DavPropertyName.RESOURCETYPE);
					response.add(DavPropertyName.GETLASTMODIFIED);
					response.add(CHILDCOUNT_PROPERTY_NAME);
					break;

				case DavConstants.PROPFIND_BY_PROPERTY:
					DavPropertyNameIterator properties = requestProperties.iterator();

					while (properties.hasNext()) {
						DavPropertyName property = properties.next();

						if (DavPropertyName.RESOURCETYPE.equals(property)) {
							response.add(new ResourceType(ResourceType.COLLECTION));
						} else if (DavPropertyName.GETLASTMODIFIED.equals(property)) {
							udate = resource.getUDate();
							if (udate != null) {
								response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
										udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
							}
						} else if (CHILDCOUNT_PROPERTY_NAME.equals(property)) {
							response.add(new DefaultDavProperty(CHILDCOUNT_PROPERTY_NAME, resource.getObjects()));
						} else {
							response.add(property, HTTPStatus.NOT_FOUND.toInt32());
						}
					}
					break;
				}
			}
		}

		return content();
	}

	@Override
	public Content select(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		switch (propFindType) {
		case DavConstants.PROPFIND_ALL_PROP:
			response.add(new ResourceType(ResourceType.COLLECTION));
			break;

		case DavConstants.PROPFIND_PROPERTY_NAMES:
			response.add(DavPropertyName.RESOURCETYPE);
			break;

		case DavConstants.PROPFIND_BY_PROPERTY:
			DavPropertyNameIterator properties = requestProperties.iterator();

			while (properties.hasNext()) {
				DavPropertyName property = properties.next();

				if (DavPropertyName.RESOURCETYPE.equals(property)) {
					response.add(new ResourceType(ResourceType.COLLECTION));
				} else {
					response.add(property, HTTPStatus.NOT_FOUND.toInt32());
				}
			}
			break;
		}

		if (depth == DavConstants.DEPTH_1) {
			Tuple[] resources = nextNode.query("select id, udate from # order by id", type);
			ZonedDateTime udate = null;

			for (Tuple resource : resources) {
				MultiStatusResponse response = addResponse(path + resource.getString(Constants.ID));

				switch (propFindType) {
				case DavConstants.PROPFIND_ALL_PROP:
					udate = resource.getUTCDatetime(Constants.UDATE);
					response.add(new ResourceType(ResourceType.COLLECTION));
					response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
							udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
					response.add(new DefaultDavProperty(DavPropertyName.GETETAG, Utils.etag(udate)));
					break;

				case DavConstants.PROPFIND_PROPERTY_NAMES:
					response.add(DavPropertyName.RESOURCETYPE);
					response.add(DavPropertyName.GETLASTMODIFIED);
					response.add(DavPropertyName.GETETAG);
					break;

				case DavConstants.PROPFIND_BY_PROPERTY:
					DavPropertyNameIterator properties = requestProperties.iterator();

					while (properties.hasNext()) {
						DavPropertyName property = properties.next();

						if (DavPropertyName.GETLASTMODIFIED.equals(property)
								|| DavPropertyName.GETETAG.equals(property)) {
							udate = resource.getUTCDatetime(Constants.UDATE);
						}

						if (DavPropertyName.RESOURCETYPE.equals(property)) {
							response.add(new ResourceType(ResourceType.COLLECTION));
						} else if (DavPropertyName.GETLASTMODIFIED.equals(property)) {
							response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
									udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
						} else if (DavPropertyName.GETETAG.equals(property)) {
							response.add(new DefaultDavProperty(DavPropertyName.GETETAG, Utils.etag(udate)));
						} else {
							response.add(property, HTTPStatus.NOT_FOUND.toInt32());
						}
					}
					break;
				}
			}
		}

		return content();
	}

	@Override
	public Content get(String type, String id, String lang, String view, String etag) {

		ZonedDateTime udate = null;

		switch (propFindType) {
		case DavConstants.PROPFIND_ALL_PROP:
			udate = nextNode.getUDate(type, id);

			response.add(new ResourceType(ResourceType.COLLECTION));
			response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
					udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
			break;

		case DavConstants.PROPFIND_PROPERTY_NAMES:
			response.add(DavPropertyName.RESOURCETYPE);
			response.add(DavPropertyName.GETLASTMODIFIED);
			break;

		case DavConstants.PROPFIND_BY_PROPERTY:
			DavPropertyNameIterator properties = requestProperties.iterator();

			while (properties.hasNext()) {
				DavPropertyName property = properties.next();

				if (DavPropertyName.RESOURCETYPE.equals(property)) {
					response.add(new ResourceType(ResourceType.COLLECTION));
				} else if (DavPropertyName.GETLASTMODIFIED.equals(property)) {
					udate = nextNode.getUDate(type, id);

					response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
							udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
				} else {
					response.add(property, HTTPStatus.NOT_FOUND.toInt32());
				}
			}
			break;
		}

		if (depth == DavConstants.DEPTH_1) {
			LinkedHashMap<String, FieldInfo> fieldsInfo = null;

			switch (propFindType) {
			case DavConstants.PROPFIND_ALL_PROP:
				fieldsInfo = nextNode.getFieldsInfo(type, id);

				for (Map.Entry<String, FieldInfo> entry : fieldsInfo.entrySet()) {
					String field = entry.getKey();
					FieldInfo fieldInfo = entry.getValue();

					MultiStatusResponse response = addResponse(path + field);
					response.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
					response.add(new DefaultDavProperty(DavPropertyName.GETCONTENTLENGTH, fieldInfo.getSize()));
					response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
							udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
					response.add(new DefaultDavProperty(DavPropertyName.GETCONTENTTYPE, fieldInfo.getContentType()));
				}
				break;

			case DavConstants.PROPFIND_PROPERTY_NAMES:
				LinkedHashMap<String, TypeField> fields = nextNode.getTypeFields(type);

				for (Map.Entry<String, TypeField> entry : fields.entrySet()) {
					MultiStatusResponse response = addResponse(path + entry.getKey());
					response.add(DavPropertyName.RESOURCETYPE);
					response.add(DavPropertyName.GETCONTENTLENGTH);
					response.add(DavPropertyName.GETLASTMODIFIED);
				}
				break;

			case DavConstants.PROPFIND_BY_PROPERTY:
				fieldsInfo = nextNode.getFieldsInfo(type, id);

				for (Map.Entry<String, FieldInfo> entry : fieldsInfo.entrySet()) {
					String field = entry.getKey();
					FieldInfo fieldInfo = entry.getValue();

					MultiStatusResponse response = addResponse(path + field);
					DavPropertyNameIterator properties = requestProperties.iterator();

					while (properties.hasNext()) {
						DavPropertyName property = properties.next();
						if (DavPropertyName.RESOURCETYPE.equals(property)) {
							response.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
						} else if (DavPropertyName.GETCONTENTLENGTH.equals(property)) {
							response.add(new DefaultDavProperty(DavPropertyName.GETCONTENTLENGTH, fieldInfo.getSize()));
						} else if (DavPropertyName.GETLASTMODIFIED.equals(property)) {
							response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
									udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
						} else if (DavPropertyName.GETCONTENTTYPE.equals(property)) {
							response.add(
									new DefaultDavProperty(DavPropertyName.GETCONTENTTYPE, fieldInfo.getContentType()));
						} else {
							response.add(property, HTTPStatus.NOT_FOUND.toInt32());
						}
					}
				}
				break;
			}
		}

		return content();
	}

	@Override
	public Content getField(String type, String id, String field, String etag) {

		ZonedDateTime udate = null;
		String contentType = null;

		switch (propFindType) {
		case DavConstants.PROPFIND_ALL_PROP:
			udate = nextNode.getUDate(type, id);
			contentType = nextNode.getFieldContentType(type, id, field);

			response.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
			response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
					udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
			response.add(new DefaultDavProperty(DavPropertyName.GETETAG, Utils.etag(udate)));
			response.add(new DefaultDavProperty(DavPropertyName.GETCONTENTTYPE, contentType));
			break;

		case DavConstants.PROPFIND_PROPERTY_NAMES:
			response.add(DavPropertyName.RESOURCETYPE);
			response.add(DavPropertyName.GETLASTMODIFIED);
			response.add(DavPropertyName.GETETAG);
			response.add(DavPropertyName.GETCONTENTTYPE);
			break;

		case DavConstants.PROPFIND_BY_PROPERTY:
			DavPropertyNameIterator properties = requestProperties.iterator();

			while (properties.hasNext()) {
				DavPropertyName property = properties.next();

				if (DavPropertyName.GETLASTMODIFIED.equals(property) || DavPropertyName.GETETAG.equals(property)) {
					udate = nextNode.getUDate(type, id);
				}

				if (DavPropertyName.RESOURCETYPE.equals(property)) {
					response.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
				} else if (DavPropertyName.GETLASTMODIFIED.equals(property)) {
					response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
							udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
				} else if (DavPropertyName.GETETAG.equals(property)) {
					response.add(new DefaultDavProperty(DavPropertyName.GETETAG, Utils.etag(udate)));
				} else if (DavPropertyName.GETCONTENTTYPE.equals(property)) {
					contentType = nextNode.getFieldContentType(type, id, field);
					response.add(new DefaultDavProperty(DavPropertyName.GETCONTENTTYPE, contentType));
				} else {
					response.add(property, HTTPStatus.NOT_FOUND.toInt32());
				}
			}
			break;
		}

		return content();

	}

	public Content content() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			String text = Utils.toString(multiStatus.toXml(document));
			
			if (debug) {
				System.out.println("--------------- WEBDAV SERVER ---------------");
				System.out.println(text);
				System.out.println();
			}
			
			return new Content(text, Format.XML, HTTPStatus.MULTI_STATUS);
		} catch (ParserConfigurationException e) {
			throw new NXException(e);
		}
	}

	protected MultiStatusResponse addResponse(String uri) {
		MultiStatusResponse response = new MultiStatusResponse(uri, "");
		multiStatus.addResponse(response);
		return response;
	}
	
	protected class WebdavRequest extends WebdavRequestImpl {
		protected Document requestDocument;
		
		protected WebdavRequest(HttpServletRequest httpRequest, DavLocatorFactory factory) {
	        super(httpRequest, factory);
		}
		
		public Document getRequestDocument() throws DavException {
			if (requestDocument == null) {
				requestDocument = super.getRequestDocument();
			}
			
			return requestDocument;
		}
	}
}