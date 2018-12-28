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

import java.time.ZonedDateTime;

import org.apache.commons.lang3.ArrayUtils;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.exceptions.UnauthorizedActionException;
import com.nexttypes.interfaces.Node;
import com.nexttypes.system.Action;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Controller;

public class UserCertificateController extends Controller {

	public UserCertificateController(String type, String user, String[] groups, Node nextNode) {
		super(type, user, groups, nextNode);
	}

	@Override
	public ZonedDateTime insert(NXObject object) {
		if (!object.containsKey(KeyWords.USER)) {
			object.put(KeyWords.USER, user);
		} else {
			checkUser(object.getString(KeyWords.USER), Action.INSERT);
		}
		return nextNode.insert(object);
	}

	@Override
	public ZonedDateTime update(NXObject object) {
		return update(object, null);
	}

	@Override
	public ZonedDateTime update(NXObject object, ZonedDateTime udate) {
		checkUser(object.getString(KeyWords.USER), Action.UPDATE);
		checkPermissions(object.getId(), Action.UPDATE);
		return nextNode.update(object, udate);
	}

	@Override
	public ZonedDateTime updateField(String id, String field, Object value) {
		if (KeyWords.USER.equals(field)) {
			checkUser((String) value, Action.UPDATE_FIELD);
		}
		checkPermissions(id, Action.UPDATE_FIELD);
		return nextNode.updateField(type, id, field, value);
	}

	@Override
	public ZonedDateTime updateId(String id, String newId) {
		checkPermissions(id, Action.UPDATE_ID);
		return nextNode.updateId(type, id, newId);
	}

	@Override
	public void delete(String... objects) {
		checkPermissions(objects, Action.DELETE);
		nextNode.delete(type, objects);
	}

	protected void checkPermissions(String[] objects, String method) {
		if (!ArrayUtils.contains(groups, Auth.ADMINISTRATORS)) {
			if (nextNode.getBoolean("select count(*) != 0 from # where id in(?) and \"user\" != ?",
					type, objects, user)) {
				throwException(method);
			}
		}
	}

	protected void checkPermissions(String id, String method) {
		if (!ArrayUtils.contains(groups, Auth.ADMINISTRATORS)) {
			String objectUser = getStringField(id, KeyWords.USER);

			if (!user.equals(objectUser)) {
				throwException(method);
			}
		}
	}

	protected void checkUser(String objectUser, String method) {
		if (objectUser != null && !user.equals(objectUser) && !ArrayUtils.contains(groups, Auth.ADMINISTRATORS)) {
			throwException(method);
		}
	}

	protected void throwException(String method) {
		throw new UnauthorizedActionException(type, method);
	}
}