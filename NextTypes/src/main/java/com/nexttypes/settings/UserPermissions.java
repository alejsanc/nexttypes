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

package com.nexttypes.settings;

import java.util.ArrayList;
import java.util.Properties;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.nodes.Node;
import com.nexttypes.system.Action;
import com.nexttypes.system.KeyWords;

public class UserPermissions extends Permissions {
	
	protected Node nextNode;
	
	public UserPermissions(ArrayList<Properties> settings, Auth auth, Node nextNode) {
		super(settings, auth, nextNode);
		
		this.nextNode = nextNode;
	}
	
	@Override
	public String[] isAllowed(String type, String[] objects, String action) {
		
		String user = auth.getUser();
		
		if ((auth.isGuest() || auth.isAdministrator()) && isAllowed(type, action)) {
			return new String[] {};
		}
		
		String[] disallowedObjects = objects;
		
		switch (type) {
		case KeyWords.USER:
			
			switch (action) {
			case Action.UPDATE:
			case Action.UPDATE_FORM:
			case Action.UPDATE_FIELD:
			case Action.UPDATE_PASSWORD:
			case Action.UPDATE_PASSWORD_FORM:
			
				ArrayList<String> disallowedUsers = new ArrayList<>();
				
				for (String object : objects) {
					if (!user.equals(object)) {
						disallowedUsers.add(object);
					}
				}
				
				disallowedObjects = disallowedUsers.toArray(new String[] {});
				
				break;
				
			default:
				disallowedObjects = super.isAllowed(type, objects, action);
			}
			
			break;
			
		case KeyWords.USER_CERTIFICATE:
			
			switch (action) {
			case Action.UPDATE:
			case Action.UPDATE_FORM:
			case Action.UPDATE_ID:
			case Action.UPDATE_FIELD:
			case Action.DELETE:
				disallowedObjects = nextNode.getStringArray("select id from user_certificate"
						+ " where id in (?) and \"user\" != ?", objects, user);
				break;
				
			default:
				disallowedObjects = super.isAllowed(type, objects, action);	
			}
			
			break;
		}
		
		return disallowedObjects;
	}	
		
	
	@Override
	public boolean isAllowedToMakeReference(String referencedType, String referencedId,
			String referencingType, String referencingId, String referencingfield) {
		
		if (auth.isGuest() || auth.isAdministrator()) {
			return true;
		}
		
		boolean result = false;
		
		switch (referencedType) {
		case KeyWords.USER:
			result = auth.getUser().equals(referencedId);
			break;
		}
		
		return result;
	}
}