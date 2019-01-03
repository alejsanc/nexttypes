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

package com.nexttypes.datatypes;

public class Auth {
	public static final String GUEST = "guest";
	public static final String GUESTS = "guests";
	public static final String ADMIN = "admin";
	public static final String ADMINISTRATORS = "administrators";
	public static final String HTTP = "http";
	public static final String SMTP = "smtp";
	public static final String BACKUP = "backup";
	public static final String CONSOLE = "console";

	protected String user;
	protected String[] groups;
	protected boolean loginUser;

	public Auth() {
		user = GUEST;
		groups = new String[] { GUESTS };
		loginUser = false;
	}

	public Auth(String user, String[] groups, boolean loginUser) {
		this.user = user;
		this.groups = groups;
		this.loginUser = loginUser;
	}

	public String getUser() {
		return user;
	}

	public String[] getGroups() {
		return groups;
	}

	public boolean isLoginUser() {
		return loginUser;
	}
}
