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
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.jcr.DavLocatorFactoryImpl;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameIterator;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Element;

import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.ICalendar;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.protocol.http.HTTPMethod;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.protocol.http.HTTPStatus;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Utils;

public class CalDAVView extends WebDAVView {
	public static final Namespace CALDAV_NAMESPACE = Namespace.getNamespace("urn:ietf:params:xml:ns:caldav");
	public static final int CALENDAR_RESOURCE_TYPE = ResourceType.registerResourceType("calendar", CALDAV_NAMESPACE);
	public static final DavPropertyName CALENDAR_DATA_PROPERTY_NAME = DavPropertyName.create("calendar-data",
			CALDAV_NAMESPACE);

	public CalDAVView(HTTPRequest request) {
		super(request);
	}

	@Override
	public Content select(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		if (HTTPMethod.REPORT.equals(request.getRequestMethod())) {
			return report(type);
		} else {
			return propfind(type);
		}
	}

	protected Content propfind(String type) {

		switch (propFindType) {
		case DavConstants.PROPFIND_ALL_PROP:
			response.add(new ResourceType(new int[] { ResourceType.COLLECTION, CALENDAR_RESOURCE_TYPE }));
			break;

		case DavConstants.PROPFIND_PROPERTY_NAMES:
			response.add(DavPropertyName.RESOURCETYPE);
			break;

		case DavConstants.PROPFIND_BY_PROPERTY:
			DavPropertyNameIterator properties = requestProperties.iterator();

			while (properties.hasNext()) {
				DavPropertyName property = properties.next();

				if (DavPropertyName.RESOURCETYPE.equals(property)) {
					response.add(new ResourceType(new int[] { ResourceType.COLLECTION, CALENDAR_RESOURCE_TYPE }));
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
					response.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
					response.add(new DefaultDavProperty(DavPropertyName.GETCONTENTLENGTH, 0));
					response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
							udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
					response.add(new DefaultDavProperty(DavPropertyName.GETETAG, Utils.etag(udate)));
					response.add(
							new DefaultDavProperty(DavPropertyName.GETCONTENTTYPE, Format.ICALENDAR.getContentType()));
					break;

				case DavConstants.PROPFIND_PROPERTY_NAMES:
					response.add(DavPropertyName.RESOURCETYPE);
					response.add(DavPropertyName.GETCONTENTLENGTH);
					response.add(DavPropertyName.GETLASTMODIFIED);
					response.add(DavPropertyName.GETETAG);
					response.add(DavPropertyName.GETCONTENTTYPE);
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
							response.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
						} else if (DavPropertyName.GETCONTENTLENGTH.equals(property)) {
							response.add(new DefaultDavProperty(DavPropertyName.GETCONTENTLENGTH, 0));
						} else if (DavPropertyName.GETLASTMODIFIED.equals(property)) {
							response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
									udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
						} else if (DavPropertyName.GETETAG.equals(property)) {
							response.add(new DefaultDavProperty(DavPropertyName.GETETAG, Utils.etag(udate)));
						} else if (DavPropertyName.GETCONTENTTYPE.equals(property)) {
							response.add(new DefaultDavProperty(DavPropertyName.GETCONTENTTYPE,
									Format.ICALENDAR.getContentType()));
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

	protected Content report(String type) {
		ArrayList<String> objects = new ArrayList<>();

		try {
			multiStatus = new MultiStatus();
			path = request.getURIPath();
			DavLocatorFactory factory = new DavLocatorFactoryImpl("");
			davRequest = new WebdavRequestImpl(request.getServletRequest(), factory);
			depth = davRequest.getDepth();
			ReportInfo report = davRequest.getReportInfo();
			requestProperties = report.getPropertyNameSet();

			switch (report.getReportName()) {
			case "{urn:ietf:params:xml:ns:caldav}calendar-query":
				break;

			case "{urn:ietf:params:xml:ns:caldav}calendar-multiget":
				for (Element element : report.getContentElements(DavConstants.XML_HREF, DavConstants.NAMESPACE)) {
					String href = element.getTextContent();
					objects.add((href.substring(href.lastIndexOf("/") + 1, href.length())));
				}
				break;
			}

		} catch (DavException e) {
			throw new NXException(e);
		}

		if (depth == DavConstants.DEPTH_1) {
			String sql = typeSettings.gts(type, Constants.ICAL_SELECT);
			Object[] parameters = null;

			if (objects.size() > 0) {
				sql += " where type.id in(?)";
				parameters = new Object[] { objects.toArray() };
			}

			Tuple[] resources = nextNode.query(sql, parameters);
			ZonedDateTime udate = null;
			String calendarURI = request.getURIRoot() + "/" + type + "/";

			for (Tuple resource : resources) {
				MultiStatusResponse response = addResponse(path + resource.getString(Constants.ID));
				ICalendar calendar = new ICalendar(calendarURI, resource);

				if (requestProperties.getContentSize() == 0) {
					udate = resource.getUTCDatetime(Constants.UDATE);
					response.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
					response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
							udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
					response.add(new DefaultDavProperty(DavPropertyName.GETETAG, Utils.etag(udate)));
					response.add(new DefaultDavProperty(CALENDAR_DATA_PROPERTY_NAME, calendar.toString()));
				} else {
					DavPropertyNameIterator properties = requestProperties.iterator();

					while (properties.hasNext()) {
						DavPropertyName property = properties.next();

						if (DavPropertyName.GETLASTMODIFIED.equals(property)
								|| DavPropertyName.GETETAG.equals(property)) {
							udate = resource.getUTCDatetime(Constants.UDATE);
						}

						if (DavPropertyName.RESOURCETYPE.equals(property)) {
							response.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
						} else if (DavPropertyName.GETLASTMODIFIED.equals(property)) {
							response.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
									udate.format(DateTimeFormatter.RFC_1123_DATE_TIME)));
						} else if (DavPropertyName.GETETAG.equals(property)) {
							response.add(new DefaultDavProperty(DavPropertyName.GETETAG, Utils.etag(udate)));
						} else if (CALENDAR_DATA_PROPERTY_NAME.equals(property)) {
							response.add(new DefaultDavProperty(CALENDAR_DATA_PROPERTY_NAME, calendar.toString()));
						} else {
							response.add(property, HTTPStatus.NOT_FOUND.toInt32());
						}
					}
				}
			}
		}

		return content();
	}
}