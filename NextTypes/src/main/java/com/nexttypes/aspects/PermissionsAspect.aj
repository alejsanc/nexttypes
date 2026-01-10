/*
 * Copyright 2015-2026 Alejandro Sánchez <alex@nexttypes.com>
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

package com.nexttypes.aspects;

import com.nexttypes.security.Checks;
import com.nexttypes.settings.Permissions;

public aspect PermissionsAspect extends Checks {
	
	String[] around(String type, String[] objects, String action) :
		(execution(* Permissions.isAllowed(..))) && args(type, objects, action) {
		
		String[] disallowedObjects = null;
		
		if (objects == null || objects.length == 0) {
			Permissions permissions = (Permissions)thisJoinPoint.getTarget();
			disallowedObjects = permissions.isAllowed(type, action) ? new String[] {} : null;
		} else {
			disallowedObjects = proceed(type, objects, action);
		}
		
		return disallowedObjects;
    }
}