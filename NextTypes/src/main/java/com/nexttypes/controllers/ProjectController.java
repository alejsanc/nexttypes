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

package com.nexttypes.controllers;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.nexttypes.datatypes.ICalendar;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.exceptions.UnauthorizedActionException;
import com.nexttypes.interfaces.Node;
import com.nexttypes.system.Action;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Controller;

import net.fortuna.ical4j.model.component.VEvent;

public class ProjectController extends Controller {

	public static final String PROJECT_MEETING = "project_meeting";
	public static final String START_TIME = "start_time";
	public static final String END_TIME = "end_time";
	public static final String PROJECT = "project";
	public static final String DOCUMENT = "document";
	public static final String PROJECT_MEMBER = "project_member";
	public static final String PROJECT_DOCUMENT = "project_document";
	public static final String PROJECT_TICKET = "project_ticket";
	public static final String PROJECT_DOCUMENT_CHAPTER = "project_document_chapter";
	public static final String PROJECT_MEETING_PARTICIPANT = "project_meeting_participant";
	public static final String PROJECT_TICKET_MESSAGE = "project_ticket_message";

	public ProjectController(String type, String[] ids, String user, String[] groups, Node nextNode) {
		super(type, ids, user, groups, nextNode);
	}

	@Override
	public ZonedDateTime insert(NXObject object) {
		checkParentPermissions(object.getString(getParentField()), Action.INSERT);
		return nextNode.insert(object);
	}

	@Override
	public ZonedDateTime update(NXObject object) {
		return update(object, null);
	}

	@Override
	public ZonedDateTime update(NXObject object, ZonedDateTime udate) {
		checkParentPermissions(object.getString(getParentField()), Action.UPDATE);
		checkPermissions(object.getType(), object.getId(), Action.UPDATE);
		return nextNode.update(object, udate);
	}

	@Override
	public ZonedDateTime update(String type, String id, byte[] data) {
		checkPermissions(type, id, Action.UPDATE);

		if (type.equals(PROJECT_MEETING)) {
			ICalendar calendar = new ICalendar(data);
			VEvent vevent = calendar.getFirstEvent();

			String startDateString = vevent.getStartDate().getValue();
			String endDateString = vevent.getEndDate().getValue();

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.BASIC_DATETIME_FORMAT);

			NXObject object = new NXObject(type, id);
			if (startDateString != null) {
				LocalDateTime startDate = LocalDateTime.parse(startDateString, formatter);
				object.put(Constants.DATE, startDate.toLocalDate());
				object.put(START_TIME, startDate.toLocalTime());
			}

			if (endDateString != null) {
				LocalDateTime endDate = LocalDateTime.parse(endDateString, formatter);
				object.put(END_TIME, endDate.toLocalTime());
			}

			object.put(Constants.DESCRIPTION, vevent.getDescription().getValue());

			return nextNode.update(object);
		} else {
			return null;
		}
	}

	@Override
	public ZonedDateTime updateId(String type, String id, String newId) {
		checkPermissions(type, id, Action.UPDATE_ID);
		return nextNode.updateId(type, id, newId);
	}

	@Override
	public void delete(String type, String... ids) {
		checkPermissions(type, ids, Action.DELETE);
		nextNode.delete(type, ids);
	}

	@Override
	public ZonedDateTime updateField(String type, String id, String field, Object value) {
		String parentField = getParentField();
		if (parentField.equals(field)) {
			checkParentPermissions((String) value, Action.UPDATE_FIELD);
		}

		checkPermissions(type, id, Action.UPDATE_FIELD);
		return nextNode.updateField(type, id, field, value);
	}

	protected void checkPermissions(String type, String id, String method) {
		checkPermissions(type, new String[] { id }, method);
	}

	protected void checkPermissions(String type, String[] ids, String method) {
		String sql = null;
		Object[] parameters = null;

		switch (type) {
		case PROJECT:
			sql = "select id, owner as user from project where id in (?)";
			parameters = new Object[] { ids };
			break;

		case PROJECT_MEMBER:
			sql = "select" + " pm.id," + " p.owner as user"

					+ " from" + " project p" + " join project_member pm on p.id = pm.project"

					+ " where" + " pm.id in (?)";

			parameters = new Object[] { ids };
			break;

		case PROJECT_DOCUMENT_CHAPTER:
		case PROJECT_MEETING_PARTICIPANT:
		case PROJECT_TICKET_MESSAGE:
			sql = "select" + " type.id," + " pm.member as user"

					+ " from" + " project_member pm"
					+ " right join # ptype on (pm.project = ptype.project and pm.member = ?)"
					+ " join # type on ptype.id = type.#"

					+ " where" + " type.id in (?)";
			parameters = new Object[] { getParentType(), user, type, getParentField(), ids };
			break;

		default:
			sql = "select" + " type.id," + " pm.member as user"

					+ " from" + " project_member pm"
					+ " right join # type on (pm.project = type.project and pm.member = ?)"

					+ " where" + " type.id in (?)";
			parameters = new Object[] { type, user, ids };
			break;
		}

		for (Tuple permission : nextNode.query(sql, parameters)) {
			if (!user.equals(permission.getString(Constants.USER))) {
				throw new UnauthorizedActionException(type, method);
			}
		}

	}

	protected void checkParentPermissions(String parent, String method) {
		if (type.equals(PROJECT)) {
			return;
		}

		if (parent == null) {
			return;
		}

		String parentType = getParentType();

		String sql = null;
		Object[] parameters = null;

		switch (type) {
		case PROJECT_MEMBER:
			sql = "select count(owner) = 1 from project where id = ? and owner = ?";
			parameters = new Object[] { parent, user };
			break;

		case PROJECT_DOCUMENT_CHAPTER:
		case PROJECT_MEETING_PARTICIPANT:
		case PROJECT_TICKET_MESSAGE:
			sql = "select" + " count(pm.member) = 1"

					+ " from" + " project_member pm" + " join # ptype on pm.project = ptype.project"

					+ " where" + " ptype.id = ?" + " and pm.member = ?";
			parameters = new Object[] { parentType, parent, user };
			break;

		default:
			sql = "select count(member) = 1 from project_member where project = ? and member = ?";
			parameters = new Object[] { parent, user };
			break;
		}

		if (!nextNode.getBoolean(sql, parameters)) {
			throw new UnauthorizedActionException(type, method);
		}
	}

	protected String getParentType() {
		String parentType = null;

		switch (type) {
		case PROJECT_DOCUMENT_CHAPTER:
			parentType = PROJECT_DOCUMENT;
			break;

		case PROJECT_MEETING_PARTICIPANT:
			parentType = PROJECT_MEETING;
			break;

		case PROJECT_TICKET_MESSAGE:
			parentType = PROJECT_TICKET;
			break;

		default:
			parentType = PROJECT;
		}

		return parentType;
	}

	protected String getParentField() {
		String parentField = null;

		switch (type) {
		case PROJECT_DOCUMENT_CHAPTER:
			parentField = DOCUMENT;
			break;

		case PROJECT_MEETING_PARTICIPANT:
			parentField = PROJECT_MEETING;
			break;

		case PROJECT_TICKET_MESSAGE:
			parentField = PROJECT_TICKET;
			break;

		default:
			parentField = PROJECT;
		}

		return parentField;
	}
}