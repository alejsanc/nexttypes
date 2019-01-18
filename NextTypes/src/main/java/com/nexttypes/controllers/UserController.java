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

package com.nexttypes.controllers;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.ArrayUtils;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.exceptions.UnauthorizedActionException;
import com.nexttypes.nodes.Node;
import com.nexttypes.system.Action;
import com.nexttypes.system.Controller;

public class UserController extends Controller {

	public UserController(String type, String user, String[] groups, Node nextNode) {
		super(type, user, groups, nextNode);
	}

	@Override
	public ZonedDateTime update(NXObject object) {
		return update(object, null);
	}

	@Override
	public ZonedDateTime update(NXObject object, ZonedDateTime udate) {
		checkPermissions(object.getId(), Action.UPDATE);
		return nextNode.update(object, udate);
	}

	@Override
	public ZonedDateTime updateField(String id, String field, Object value) {
		checkPermissions(id, Action.UPDATE_FIELD);
		return nextNode.updateField(type, id, field, value);
	}

	@Override
	public ZonedDateTime updatePassword(String id, String field, String currentPassword,
			String newPassword, String newPasswordRepeat) {
		checkPermissions(id, Action.UPDATE_PASSWORD);
		return nextNode.updatePassword(type, id, field, currentPassword, newPassword, newPasswordRepeat);
	}

	protected void checkPermissions(String id, String method) {
		if (!user.equals(id) && !ArrayUtils.contains(groups, Auth.ADMINISTRATORS)) {
			throw new UnauthorizedActionException(type, method);
		}
	}
}