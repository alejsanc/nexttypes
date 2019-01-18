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

package com.nexttypes.system;

import com.nexttypes.nodes.Node;
import com.nexttypes.settings.Permissions;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;

public abstract class Module implements AutoCloseable {
	public abstract String getUser();

	public abstract String[] getGroups();

	public abstract void setUser(String user);

	public abstract void setGroups(String[] groups);

	public abstract Context getContext();

	public abstract Strings getStrings();

	public abstract TypeSettings getTypeSettings();
	
	public abstract Node getNextNode();
	
	public Permissions getPermissions() {
		return getContext().getPermissions(this);
	}
	
	public Permissions getPermissions(String type) {
		return getContext().getPermissions(type, this);
	}
}