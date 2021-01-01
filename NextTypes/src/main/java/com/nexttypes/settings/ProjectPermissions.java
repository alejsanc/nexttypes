/*
 * Copyright 2015-2021 Alejandro Sánchez <alex@nexttypes.com>
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

import java.util.ArrayList;
import java.util.Properties;

import com.nexttypes.controllers.ProjectController;
import com.nexttypes.datatypes.Auth;
import com.nexttypes.nodes.Node;
import com.nexttypes.system.Action;

public class ProjectPermissions extends Permissions {

	protected Node nextNode;
	
	public ProjectPermissions(ArrayList<Properties> settings, Auth auth, Node nextNode) {
		super(settings, auth, nextNode);
		
		this.nextNode = nextNode;
	}
	
	@Override
	public String[] isAllowed(String type, String[] objects, String action) {
						
		if ((auth.isGuest() || auth.isAdministrator()) && isAllowed(type, action)) {
			return new String[] {};
		}
		
		String user = auth.getUser();
		String sql = null;
		Object[] parameters = null;

		root:
		switch (type) {
		case ProjectController.PROJECT:
			
			switch (action) {
			case Action.UPDATE:
			case Action.UPDATE_FIELD:
			case Action.UPDATE_FORM:
			case Action.UPDATE_ID:
			case Action.UPDATE_ID_FORM:
			case Action.DELETE:
				sql = "select id from project where id in (?) and owner != ?";
				parameters = new Object[] { objects, user };
				break;
				
			default:
				sql = "select"
						+ " p.id"	
						
					+ " from"
						+ " project p"
						+ " left join project_member pm on (p.id = pm.project and pm.member = ?)"
				
					+ " where p.id in (?) and p.owner != ? and pm.member is null";
				
				parameters = new Object[] {user, objects, user};
			}
			
			break;

		
		case ProjectController.PROJECT_DOCUMENT_CHAPTER:
		case ProjectController.PROJECT_MEETING_PARTICIPANT:
		case ProjectController.PROJECT_TICKET_MESSAGE:
			sql = "select"
					+ " type.id"

				+ " from"
					+ " # type"
					+ " join # rtype on type.# = rtype.id "
					+ " join project p on rtype.project = p.id"
					+ " left join project_member pm on (rtype.project = pm.project and pm.member = ?)"

				+ " where"
					+ " type.id in (?) and p.owner != ? and pm.member is null";
			
			parameters = new Object[] { type, ProjectController.getReferencedType(type),
					ProjectController.getReferencingField(type), user, objects, user};
		
			break;

		case ProjectController.PROJECT_MEMBER:
			switch (action) {
			case Action.UPDATE:
			case Action.UPDATE_FIELD:
			case Action.UPDATE_FORM:
			case Action.UPDATE_ID:
			case Action.UPDATE_ID_FORM:
			case Action.DELETE:
			
				sql = "select" 
						+ " pm.id"

					+ " from"
						+ " project_member pm"
						+ " join project p on pm.project = p.id"

					+ " where" 
						+ " pm.id in (?) and p.owner != ?";

				parameters = new Object[] { objects, user };
			
				break root;
			}

		default:
			sql = "select"
					+ " type.id"

				+ " from"
					+ " # type"
					+ " join project p on (type.project = p.id)"
					+ " left join project_member pm on (type.project = pm.project and pm.member = ?)"

				+ " where"
					+ " type.id in (?) and p.owner != ? and pm.member is null";
			
			parameters = new Object[] { type, user, objects, user };
			break;
		}
		
		return nextNode.getStringArray(sql, parameters);
	}
	
	@Override
	public boolean isAllowedToMakeReference(String referencingType, String referencingId,
			String referencingfield, String referencedType, String referencedId) {
		
		if (auth.isGuest() || auth.isAdministrator()) {
			return true;
		}
		
		String user = auth.getUser();
		boolean allowed = true;
		String sql = null;
		Object[] parameters = null;		
		
		switch (referencedType) {
		case ProjectController.PROJECT:
			if (ProjectController.PROJECT_MEMBER.equals(referencingType)) {
				sql = "select count(*) = 1 from project where id = ? and owner = ?";
				parameters = new Object[] { referencedId, user };
			} else {
				sql = "select"
						+ " count(*) = 1"
						
					+ " from"
						+ " project p"
						+ " left join project_member pm on (p.id = pm.project and pm.member = ?)"
						
					+ " where"
						+ " p.id = ? and (p.owner = ? or pm.member = ?)";
				
				parameters = new Object[] { user, referencedId, user, user };
			}
			
			break;
			
		case ProjectController.PROJECT_MEETING:
		case ProjectController.PROJECT_DOCUMENT:
		case ProjectController.PROJECT_TICKET:
			sql = "select"
					+ " count(*) = 1"
			
				+ " from"
					+ " # type"
					+ " join project p on type.project = p.id"
					+ " left join project_member pm on (type.project = pm.project and pm.member = ?)"
			
				+ " where"
					+ " type.id = ? and (p.owner = ? or pm.member = ?)";
			
			parameters = new Object[] { referencedType, user, referencedId, user, user };
			
			break;
		}
		
		if (sql != null) {
			allowed = nextNode.getBoolean(sql, parameters);
		}
		
		return allowed;
	}
}