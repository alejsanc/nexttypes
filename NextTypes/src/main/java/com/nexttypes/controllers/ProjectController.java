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

package com.nexttypes.controllers;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.ICalendar;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.Names;
import com.nexttypes.nodes.Node;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Action;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Controller;

import net.fortuna.ical4j.model.component.VEvent;

public class ProjectController extends Controller {
	
	public static final String PROJECT = "project";
	public static final String PROJECT_MEETING = "project_meeting";
	public static final String PROJECT_MEMBER = "project_member";
	public static final String PROJECT_DOCUMENT = "project_document";
	public static final String PROJECT_TICKET = "project_ticket";
	public static final String PROJECT_DOCUMENT_CHAPTER = "project_document_chapter";
	public static final String PROJECT_MEETING_PARTICIPANT = "project_meeting_participant";
	public static final String PROJECT_TICKET_MESSAGE = "project_ticket_message";
	public static final String DOCUMENT = "document";
	public static final String MEETING = "meeting";
	public static final String TICKET = "ticket";
	
	public ProjectController(String type, Auth auth, Node nextNode) {
		super(type, auth, nextNode);
	}

	@Override
	public ZonedDateTime update(String id, byte[] data) {
		
		if (PROJECT_MEETING.equals(type)) {
			ICalendar calendar = new ICalendar(data);
			VEvent vevent = calendar.getFirstEvent();

			String startDateString = vevent.getDateTimeStart().get().getValue();
			String endDateString = vevent.getDateTimeEnd().get().getValue();

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.BASIC_DATETIME_FORMAT);

			NXObject object = new NXObject(type, id);
			if (startDateString != null) {
				LocalDateTime startDate = LocalDateTime.parse(startDateString, formatter);
				object.put(KeyWords.DATE, startDate.toLocalDate());
				object.put(KeyWords.START_TIME, startDate.toLocalTime());
			}

			if (endDateString != null) {
				LocalDateTime endDate = LocalDateTime.parse(endDateString, formatter);
				object.put(KeyWords.END_TIME, endDate.toLocalTime());
			}

			object.put(KeyWords.DESCRIPTION, vevent.getDescription().get().getValue());

			return nextNode.update(object);
		} else {
			return null;
		}
	}
	
	@Override
	public Names getNames(String referencingType, String referencingAction,
			String referencingField, String lang) {
		return getNames(referencingType, referencingAction, referencingField, lang,
				null, null, null);
	}
	
	@Override
	public Names getNames(String referencingType, String referencingAction,
			String referencingField, String lang, String search, Long offset, Long limit) {
		
		String user = auth.getUser();
		
		if (auth.isGuest() || auth.isAdministrator()) {
			return getNames(lang);
		}
		
		if (Action.SEARCH.equals(referencingAction)) {
			return getNames(lang);
		}
		
		Names names = null;
		String sql = null;
		Object[] parameters = null;
		
		switch (type) {
		case PROJECT:
			if (PROJECT_MEMBER.equals(referencingType)) {
				sql = "select id, name from project where owner = ?";
				parameters = new Object[] { user };
			} else {
				sql = "select"
						+ " p.id,"
						+ " p.name"
					
					+ " from"
						+ " project p"
						+ " left join project_member pm on (p.id = pm.project and pm.member = ?)"
				
					+ " where"
						+ " p.owner = ? or pm.member = ?";
			
				parameters = new Object[] {user, user, user};			
			}
			
			break;
			
		case PROJECT_MEETING:
		case PROJECT_DOCUMENT:
		case PROJECT_TICKET:
			sql = "select"
					+ " type.id,"
					+ " p.name || ' - ' || type.# as name"
			
				+ " from"
					+ " # type"
					+ " join project p on type.project = p.id"
					+ " left join project_member pm on (type.project = pm.project and pm.member = ?)"
			
				+ " where"
					+ " p.owner = ? or pm.member = ?";
			
			parameters = new Object[] { getNameField(type), type, user, user, user };
			
			break;
		}
		
		if (sql != null) {
		
			names = nextNode.getNames(type, sql, parameters, lang, search, offset, limit);
			
		} else {
			names = getNames(lang);
		}
		
		return names;
	}	
	
	public static String getNameField(String type) {
		String name = null;
				
		switch (type) {
		case PROJECT_MEETING:
			name = "summary";
			break;
		case PROJECT_DOCUMENT:
		case PROJECT_TICKET:
			name = "title";
			break;
		}
		
		return name;
	}
	
	public static String getReferencedType(String type) {
		String referencedType = null;

		switch (type) {
		case PROJECT_DOCUMENT_CHAPTER:
			referencedType = PROJECT_DOCUMENT;
			break;

		case PROJECT_MEETING_PARTICIPANT:
			referencedType = PROJECT_MEETING;
			break;

		case PROJECT_TICKET_MESSAGE:
			referencedType = PROJECT_TICKET;
			break;
		}

		return referencedType;
	}
	
	public static String getReferencingField(String type) {
		String referencingField = null;

		switch (type) {
		case PROJECT_DOCUMENT_CHAPTER:
			referencingField = DOCUMENT;
			break;

		case PROJECT_MEETING_PARTICIPANT:
			referencingField = MEETING;
			break;

		case PROJECT_TICKET_MESSAGE:
			referencingField = TICKET;
			break;
		}

		return referencingField;
	}
}