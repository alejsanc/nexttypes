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

package com.nexttypes.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.exceptions.UnauthorizedActionException;
import com.nexttypes.exceptions.UnauthorizedReferenceException;
import com.nexttypes.nodes.Node;
import com.nexttypes.system.KeyWords;

public class Permissions extends TypeSettings {
	protected Auth auth;
	protected Node nextNode;
	
	public Permissions(ArrayList<Properties> settings, Auth auth, Node nextNode) {
		super(settings);
		
		this.auth = auth;
		this.nextNode = nextNode;
	}
	
	public Auth getAuth() {
		return auth;
	}

	public String[] getAllowedUsers(String type, String action) {
		return getTypeStringArray(type, new String[] { action + "." + KeyWords.USERS, KeyWords.USERS });
	}

	public String[] getAllowedGroups(String type, String action) {
		return getTypeStringArray(type, new String[] { action + "." + KeyWords.GROUPS, KeyWords.GROUPS });
	}

	public boolean isAllowed(String action) {
		return isAllowed(null, action);
	}
	
	public boolean isAllowed(String type, String action) {
		boolean allowed = false;
		String[] allowedUsers = getAllowedUsers(type, action);
		String[] allowedGroups = null;
		String[] groups = auth.getGroups();

		if (ArrayUtils.contains(allowedUsers, auth.getUser())) {
			allowed = true;
		} else if (groups != null && groups.length > 0) {
			allowedGroups = getAllowedGroups(type, action);

			for (String group : groups) {
				if (ArrayUtils.contains(allowedGroups, group)) {
					allowed = true;
					break;
				}
			}
		}

		return allowed;
	}
	
	public void checkPermissions(String action) {
		checkPermissions((String) null, action);
	}

	public void checkPermissions(String type, String action) {

		if (!isAllowed(type, action)) {
			throw new UnauthorizedActionException(type, action);
		}

	}

	public void checkPermissions(String[] types, String action) {
		if (types == null || types.length == 0) {
			checkPermissions((String) null, action);
		} else {
			for (String type : types) {
				checkPermissions(type, action);
			}
		}
	}
	
	public boolean isAllowed(String type, String id, String action) {
		boolean allowed = false;
		
		if (id == null) {
			allowed = isAllowed(type, action);
		} else {
			allowed = isAllowed(type, new String[] {id}, action).length == 0;
		}
		
		return allowed;
	}
	
	public String[] isAllowed(String type, String[] objects, String action) {
		return isAllowed(type, action) ? new String[] {} : objects;
	}
	
	public String[] isAllowed(String type, NXObject[] objects, String action) {
		return isAllowed(type, Arrays.stream(objects).map(object -> object.getId())
				.toArray(String[]::new), action);
	}
	
	public void checkPermissions(String type, String id, String action) {
		if (id == null) {
			checkPermissions(type, action);
		} else {
			checkPermissions(type, new String[] {id}, action);
		}
	}
	
	public void checkPermissions(String type, String[] objects, String action) {
		String[] disallowedObjects = isAllowed(type, objects, action);
		
		if (disallowedObjects == null || disallowedObjects.length > 0) {
			throw new UnauthorizedActionException(type, disallowedObjects, action);
		}
	}
	
	public boolean isAllowedToMakeReference(String referencingType, String referencingId, 
			FieldReference ref) {
		return isAllowedToMakeReference(referencingType, referencingId, ref.getReferencingField(),
				ref.getReferencedType(), ref.getReferencedId());
	}
	
	public boolean isAllowedToMakeReference(String referencingType, String referencingId,
			String referencingField, String referencedType, String referencedId) {
		return true;
	}
	
	public void checkReferencePermissions(String referencingType, String referencingId,
			String referencingField, String referencedType, String referencedId) {
		if (!isAllowedToMakeReference(referencingType, referencingId, referencingField,
				referencedType, referencedId)) {
			throw new UnauthorizedReferenceException(referencingType, referencingId,
					referencingField, referencedType, referencedId);
		}
	}
}