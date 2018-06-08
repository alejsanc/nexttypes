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

package com.nexttypes.settings;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;

import com.nexttypes.exceptions.UnauthorizedActionException;
import com.nexttypes.system.Constants;

public class PermissionSettings extends TypeSettings {
	public PermissionSettings(ArrayList<Properties> settings) {
		super(settings);
	}

	public String[] getAllowedUsers(String type, String method) {
		return getTypeStringArray(type, new String[] { method + "." + Constants.USERS, Constants.USERS });
	}

	public String[] getAllowedGroups(String type, String method) {
		return getTypeStringArray(type, new String[] { method + "." + Constants.GROUPS, Constants.GROUPS });
	}

	public boolean isAllowed(String type, String action, String user, String[] groups) {
		boolean allowed = false;
		String[] allowedUsers = getAllowedUsers(type, action);
		String[] allowedGroups = null;

		if (ArrayUtils.contains(allowedUsers, user)) {
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

	public void checkPermissions(String action, String user, String[] groups) {
		checkPermissions((String) null, action, user, groups);
	}

	public void checkPermissions(String type, String action, String user, String[] groups) {

		if (!isAllowed(type, action, user, groups)) {
			throw new UnauthorizedActionException(type, action);
		}

	}

	public void checkPermissions(String[] types, String action, String user, String[] groups) {
		if (types == null || types.length == 0) {
			checkPermissions((String) null, action, user, groups);
		} else {
			for (String type : types) {
				checkPermissions(type, action, user, groups);
			}
		}
	}
}